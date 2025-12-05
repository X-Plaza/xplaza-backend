/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.inventory.controller;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.inventory.domain.entity.InventoryItem;
import com.xplaza.backend.inventory.domain.entity.StockReservation;
import com.xplaza.backend.inventory.domain.entity.Warehouse;
import com.xplaza.backend.inventory.service.InventoryService;

/**
 * REST controller for inventory management operations.
 */
@RestController
@RequestMapping("/api/v1/inventory")
@RequiredArgsConstructor
@Tag(name = "Inventory", description = "Inventory and stock management APIs")
public class InventoryController {

  private final InventoryService inventoryService;

  // ==================== Stock Queries ====================

  @Operation(summary = "Get available quantity for a product")
  @GetMapping("/products/{productId}/available")
  public ResponseEntity<Integer> getAvailableQuantity(@PathVariable Long productId) {
    return ResponseEntity.ok(inventoryService.getAvailableQuantity(productId));
  }

  @Operation(summary = "Get available quantity for a variant")
  @GetMapping("/variants/{variantId}/available")
  public ResponseEntity<Integer> getVariantAvailableQuantity(@PathVariable UUID variantId) {
    return ResponseEntity.ok(inventoryService.getAvailableQuantityByVariant(variantId));
  }

  @Operation(summary = "Check if product is in stock")
  @GetMapping("/products/{productId}/in-stock")
  public ResponseEntity<Boolean> isInStock(@PathVariable Long productId) {
    return ResponseEntity.ok(inventoryService.isInStock(productId));
  }

  @Operation(summary = "Check if variant is in stock")
  @GetMapping("/variants/{variantId}/in-stock")
  public ResponseEntity<Boolean> isVariantInStock(@PathVariable UUID variantId) {
    return ResponseEntity.ok(inventoryService.isVariantInStock(variantId));
  }

  @Operation(summary = "Get inventory for product at warehouse")
  @GetMapping("/products/{productId}/warehouses/{warehouseId}")
  public ResponseEntity<InventoryItem> getInventory(
      @PathVariable Long productId,
      @PathVariable Long warehouseId) {
    return inventoryService.getInventory(productId, warehouseId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  // ==================== Stock Operations ====================

  @Operation(summary = "Create inventory item")
  @PostMapping
  public ResponseEntity<InventoryItem> createInventoryItem(@RequestBody CreateInventoryRequest request) {
    InventoryItem item = inventoryService.createInventoryItem(
        request.productId(),
        request.variantId(),
        request.sku(),
        request.warehouseId(),
        request.initialQuantity());
    return ResponseEntity.ok(item);
  }

  @Operation(summary = "Reserve stock for order/cart")
  @PostMapping("/reserve")
  public ResponseEntity<StockReservation> reserveStock(@RequestBody ReserveStockRequest request) {
    StockReservation reservation = inventoryService.reserveStock(
        request.variantId(),
        request.warehouseId(),
        request.quantity(),
        request.orderId(),
        request.cartId());
    return ResponseEntity.ok(reservation);
  }

  @Operation(summary = "Release stock reservation")
  @PostMapping("/reservations/{reservationId}/release")
  public ResponseEntity<Void> releaseReservation(@PathVariable UUID reservationId) {
    inventoryService.releaseReservation(reservationId);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Fulfill stock reservation (ship order)")
  @PostMapping("/reservations/{reservationId}/fulfill")
  public ResponseEntity<Void> fulfillReservation(@PathVariable UUID reservationId) {
    inventoryService.fulfillReservation(reservationId);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Receive stock from supplier")
  @PostMapping("/receive")
  public ResponseEntity<InventoryItem> receiveStock(@RequestBody ReceiveStockRequest request) {
    InventoryItem item = inventoryService.receiveStock(
        request.sku(),
        request.warehouseId(),
        request.quantity(),
        request.userId());
    return ResponseEntity.ok(item);
  }

  @Operation(summary = "Adjust stock quantity")
  @PostMapping("/{inventoryId}/adjust")
  public ResponseEntity<InventoryItem> adjustStock(
      @PathVariable UUID inventoryId,
      @RequestBody AdjustStockRequest request) {
    InventoryItem item = inventoryService.adjustStock(
        inventoryId,
        request.newQuantity(),
        request.reason(),
        request.userId());
    return ResponseEntity.ok(item);
  }

  // ==================== Alerts ====================

  @Operation(summary = "Get items needing reorder")
  @GetMapping("/alerts/reorder")
  public ResponseEntity<List<InventoryItem>> getItemsNeedingReorder() {
    return ResponseEntity.ok(inventoryService.getItemsNeedingReorder());
  }

  @Operation(summary = "Get items below safety stock")
  @GetMapping("/alerts/low-stock")
  public ResponseEntity<List<InventoryItem>> getItemsBelowSafetyStock() {
    return ResponseEntity.ok(inventoryService.getItemsBelowSafetyStock());
  }

  // ==================== Warehouses ====================

  @Operation(summary = "Get active warehouses")
  @GetMapping("/warehouses")
  public ResponseEntity<List<Warehouse>> getActiveWarehouses() {
    return ResponseEntity.ok(inventoryService.getActiveWarehouses());
  }

  @Operation(summary = "Find best warehouse for fulfillment")
  @GetMapping("/products/{productId}/best-warehouse")
  public ResponseEntity<Warehouse> findBestWarehouse(
      @PathVariable Long productId,
      @RequestParam String countryCode) {
    return inventoryService.findBestWarehouseForProduct(productId, countryCode)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  // ==================== Request DTOs ====================

  public record CreateInventoryRequest(
      Long productId,
      UUID variantId,
      String sku,
      Long warehouseId,
      int initialQuantity
  ) {
  }

  public record ReserveStockRequest(
      UUID variantId,
      Long warehouseId,
      int quantity,
      Long orderId,
      UUID cartId
  ) {
  }

  public record ReceiveStockRequest(
      String sku,
      Long warehouseId,
      int quantity,
      Long userId
  ) {
  }

  public record AdjustStockRequest(
      int newQuantity,
      String reason,
      Long userId
  ) {
  }
}
