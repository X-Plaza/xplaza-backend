/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.inventory.service;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.inventory.domain.entity.InventoryItem;
import com.xplaza.backend.inventory.domain.entity.InventoryMovement;
import com.xplaza.backend.inventory.domain.entity.StockReservation;
import com.xplaza.backend.inventory.domain.entity.Warehouse;
import com.xplaza.backend.inventory.domain.repository.InventoryItemRepository;
import com.xplaza.backend.inventory.domain.repository.WarehouseRepository;

/**
 * Service for inventory management operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InventoryService {

  private final InventoryItemRepository inventoryRepository;
  private final WarehouseRepository warehouseRepository;

  /**
   * Get available quantity for a product across all warehouses.
   */
  @Transactional(readOnly = true)
  public int getAvailableQuantity(Long productId) {
    Integer total = inventoryRepository.sumAvailableQuantityByProductId(productId);
    return total != null ? total : 0;
  }

  /**
   * Get available quantity for a variant across all warehouses.
   */
  @Transactional(readOnly = true)
  public int getAvailableQuantityByVariant(UUID variantId) {
    Integer total = inventoryRepository.sumAvailableQuantityByVariantId(variantId);
    return total != null ? total : 0;
  }

  /**
   * Check if product is in stock.
   */
  @Transactional(readOnly = true)
  public boolean isInStock(Long productId) {
    return getAvailableQuantity(productId) > 0;
  }

  /**
   * Check if variant is in stock.
   */
  @Transactional(readOnly = true)
  public boolean isVariantInStock(UUID variantId) {
    return getAvailableQuantityByVariant(variantId) > 0;
  }

  /**
   * Reserve stock for an order.
   */
  public StockReservation reserveStock(Long productId, UUID variantId, Long warehouseId, int quantity,
      UUID orderId, UUID cartId) {
    InventoryItem item;
    if (variantId != null) {
      item = inventoryRepository.findByVariantIdAndWarehouseId(variantId, warehouseId)
          .orElseThrow(() -> new IllegalArgumentException(
              "Inventory not found for variant: " + variantId + " at warehouse: " + warehouseId));
    } else {
      item = inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId)
          .orElseThrow(() -> new IllegalArgumentException(
              "Inventory not found for product: " + productId + " at warehouse: " + warehouseId));
    }

    if (!item.reserve(quantity)) {
      throw new IllegalStateException("Insufficient stock for product: " + productId);
    }

    StockReservation reservation = StockReservation.builder()
        .inventoryItem(item)
        .orderId(orderId)
        .cartId(cartId)
        .quantity(quantity)
        .type(orderId != null ? StockReservation.ReservationType.ORDER : StockReservation.ReservationType.CART)
        .build();

    item.getReservations().add(reservation);
    inventoryRepository.save(item);

    log.info("Reserved {} units of product {} at warehouse {}", quantity, productId, warehouseId);
    return reservation;
  }

  /**
   * Reserve stock from any available warehouse.
   */
  public StockReservation reserveStockAnyWarehouse(Long productId, UUID variantId, int quantity, UUID orderId) {
    List<InventoryItem> items;
    if (variantId != null) {
      items = inventoryRepository.findByVariantId(variantId);
    } else {
      items = inventoryRepository.findByProductId(productId);
    }

    for (InventoryItem item : items) {
      if (item.getAvailableQuantity() >= quantity) {
        return reserveStock(productId, variantId, item.getWarehouse().getWarehouseId(), quantity, orderId, null);
      }
    }

    throw new IllegalStateException("Insufficient stock for product: " + productId);
  }

  /**
   * Release a reservation.
   */
  public void releaseReservation(UUID reservationId) {
    // Find reservation and release
    List<InventoryItem> items = inventoryRepository.findAll();
    for (InventoryItem item : items) {
      Optional<StockReservation> reservation = item.getReservations().stream()
          .filter(r -> r.getReservationId().equals(reservationId))
          .findFirst();

      if (reservation.isPresent()) {
        StockReservation res = reservation.get();
        item.releaseReservation(res.getQuantity());
        res.release();
        inventoryRepository.save(item);
        log.info("Released reservation: {}", reservationId);
        return;
      }
    }
    throw new IllegalArgumentException("Reservation not found: " + reservationId);
  }

  /**
   * Fulfill reserved stock (when order is shipped).
   */
  public void fulfillReservation(UUID reservationId) {
    List<InventoryItem> items = inventoryRepository.findAll();
    for (InventoryItem item : items) {
      Optional<StockReservation> reservation = item.getReservations().stream()
          .filter(r -> r.getReservationId().equals(reservationId))
          .findFirst();

      if (reservation.isPresent()) {
        StockReservation res = reservation.get();
        item.fulfill(res.getQuantity());
        res.fulfill();
        inventoryRepository.save(item);

        log.info("Fulfilled reservation: {}", reservationId);
        return;
      }
    }
    throw new IllegalArgumentException("Reservation not found: " + reservationId);
  }

  /**
   * Receive stock from supplier.
   */
  public InventoryItem receiveStock(String sku, Long warehouseId, int quantity, Long userId) {
    InventoryItem item = inventoryRepository.findBySku(sku)
        .filter(i -> i.getWarehouse().getWarehouseId().equals(warehouseId))
        .orElseThrow(() -> new IllegalArgumentException("Inventory not found for SKU: " + sku));

    // Create movement record
    InventoryMovement movement = InventoryMovement.createReceive(item, quantity, null, userId);
    item.getMovements().add(movement);

    // Update stock
    item.receiveStock(quantity);
    item = inventoryRepository.save(item);

    log.info("Received {} units of SKU {} at warehouse {}", quantity, sku, warehouseId);
    return item;
  }

  /**
   * Adjust stock (for inventory counts).
   */
  public InventoryItem adjustStock(UUID inventoryId, int newQuantity, String reason, Long userId) {
    InventoryItem item = inventoryRepository.findById(inventoryId)
        .orElseThrow(() -> new IllegalArgumentException("Inventory not found: " + inventoryId));

    // Create movement record
    InventoryMovement movement = InventoryMovement.createAdjustment(item, newQuantity, reason, userId);
    item.getMovements().add(movement);

    // Adjust stock
    item.adjustStock(newQuantity);
    item = inventoryRepository.save(item);

    log.info("Adjusted stock for inventory {}: new quantity = {}", inventoryId, newQuantity);
    return item;
  }

  /**
   * Get items that need reordering.
   */
  @Transactional(readOnly = true)
  public List<InventoryItem> getItemsNeedingReorder() {
    return inventoryRepository.findItemsNeedingReorder();
  }

  /**
   * Get items below safety stock.
   */
  @Transactional(readOnly = true)
  public List<InventoryItem> getItemsBelowSafetyStock() {
    return inventoryRepository.findItemsBelowSafetyStock();
  }

  /**
   * Get active warehouses.
   */
  @Transactional(readOnly = true)
  public List<Warehouse> getActiveWarehouses() {
    return warehouseRepository.findByIsActiveTrue();
  }

  /**
   * Find best warehouse for fulfillment based on stock and location.
   */
  @Transactional(readOnly = true)
  public Optional<Warehouse> findBestWarehouseForProduct(Long productId, String countryCode) {
    List<InventoryItem> available = inventoryRepository.findAvailableInventoryByProductId(productId);
    if (available.isEmpty()) {
      return Optional.empty();
    }

    // Filter by country and sort by priority
    return available.stream()
        .map(InventoryItem::getWarehouse)
        .filter(w -> w.getIsActive() && w.getCountryCode().equals(countryCode))
        .max((w1, w2) -> Integer.compare(w1.getPriority(), w2.getPriority()));
  }

  /**
   * Get inventory for a product at a specific warehouse.
   */
  @Transactional(readOnly = true)
  public Optional<InventoryItem> getInventory(Long productId, Long warehouseId) {
    return inventoryRepository.findByProductIdAndWarehouseId(productId, warehouseId);
  }

  /**
   * Create inventory item for a new product/variant at a warehouse.
   */
  public InventoryItem createInventoryItem(Long productId, UUID variantId, String sku,
      Long warehouseId, int initialQuantity) {
    Warehouse warehouse = warehouseRepository.findById(warehouseId)
        .orElseThrow(() -> new IllegalArgumentException("Warehouse not found: " + warehouseId));

    InventoryItem item = InventoryItem.builder()
        .productId(productId)
        .variantId(variantId)
        .sku(sku)
        .warehouse(warehouse)
        .quantityOnHand(initialQuantity)
        .build();

    item = inventoryRepository.save(item);
    log.info("Created inventory item for SKU {} at warehouse {}", sku, warehouseId);
    return item;
  }
}
