/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.inventory.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;

import lombok.*;

/**
 * Inventory stock item per warehouse/variant combination.
 */
@Entity
@Table(name = "inventory_items", indexes = {
    @Index(name = "idx_inventory_product", columnList = "product_id"),
    @Index(name = "idx_inventory_variant", columnList = "variant_id"),
    @Index(name = "idx_inventory_warehouse", columnList = "warehouse_id"),
    @Index(name = "idx_inventory_sku", columnList = "sku")
}, uniqueConstraints = {
    @UniqueConstraint(name = "uk_inventory_warehouse_sku", columnNames = { "warehouse_id", "sku" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryItem {

  @Id
  @Column(name = "inventory_id")
  @Builder.Default
  private UUID inventoryId = UUID.randomUUID();

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Column(name = "variant_id")
  private UUID variantId;

  @Column(name = "sku", nullable = false, length = 100)
  private String sku;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "warehouse_id", nullable = false)
  private Warehouse warehouse;

  // Stock quantities
  @Column(name = "quantity_on_hand", nullable = false)
  @Builder.Default
  private Integer quantityOnHand = 0;

  @Column(name = "quantity_reserved", nullable = false)
  @Builder.Default
  private Integer quantityReserved = 0;

  @Column(name = "quantity_incoming")
  @Builder.Default
  private Integer quantityIncoming = 0;

  @Column(name = "quantity_damaged")
  @Builder.Default
  private Integer quantityDamaged = 0;

  // Thresholds
  @Column(name = "reorder_point")
  @Builder.Default
  private Integer reorderPoint = 10;

  @Column(name = "reorder_quantity")
  @Builder.Default
  private Integer reorderQuantity = 100;

  @Column(name = "safety_stock")
  @Builder.Default
  private Integer safetyStock = 5;

  @Column(name = "max_stock")
  private Integer maxStock;

  // Cost tracking
  @Column(name = "unit_cost", precision = 15, scale = 4)
  private BigDecimal unitCost;

  @Column(name = "currency", length = 3)
  @Builder.Default
  private String currency = "EUR";

  // Location within warehouse
  @Column(name = "bin_location", length = 50)
  private String binLocation;

  @Column(name = "zone", length = 30)
  private String zone;

  @Column(name = "aisle", length = 20)
  private String aisle;

  @Column(name = "shelf", length = 20)
  private String shelf;

  // Status
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  @Builder.Default
  private InventoryStatus status = InventoryStatus.ACTIVE;

  // Dates
  @Column(name = "last_counted_at")
  private Instant lastCountedAt;

  @Column(name = "last_received_at")
  private Instant lastReceivedAt;

  @Column(name = "created_at")
  @Builder.Default
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at")
  @Builder.Default
  private Instant updatedAt = Instant.now();

  @OneToMany(mappedBy = "inventoryItem", fetch = FetchType.LAZY)
  @OrderBy("createdAt DESC")
  @Builder.Default
  private List<InventoryMovement> movements = new ArrayList<>();

  @OneToMany(mappedBy = "inventoryItem", fetch = FetchType.LAZY, cascade = CascadeType.ALL)
  @Builder.Default
  private List<StockReservation> reservations = new ArrayList<>();

  public enum InventoryStatus {
    ACTIVE,
    INACTIVE,
    DISCONTINUED,
    ON_HOLD
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = Instant.now();
  }

  /**
   * Calculate available quantity (on hand minus reserved).
   */
  public int getAvailableQuantity() {
    return quantityOnHand - quantityReserved;
  }

  /**
   * Check if item is in stock.
   */
  public boolean isInStock() {
    return getAvailableQuantity() > 0;
  }

  /**
   * Check if item needs reordering.
   */
  public boolean needsReorder() {
    return (quantityOnHand - quantityReserved + quantityIncoming) <= reorderPoint;
  }

  /**
   * Check if stock is below safety level.
   */
  public boolean isBelowSafetyStock() {
    return (quantityOnHand - quantityReserved) <= safetyStock;
  }

  /**
   * Reserve stock for an order.
   */
  public boolean reserve(int quantity) {
    if (getAvailableQuantity() >= quantity) {
      this.quantityReserved += quantity;
      return true;
    }
    return false;
  }

  /**
   * Release reserved stock.
   */
  public void releaseReservation(int quantity) {
    this.quantityReserved = Math.max(0, this.quantityReserved - quantity);
  }

  /**
   * Fulfill reserved stock (decrease on-hand and reserved).
   */
  public void fulfill(int quantity) {
    this.quantityOnHand -= quantity;
    this.quantityReserved -= quantity;
  }

  /**
   * Receive incoming stock.
   */
  public void receiveStock(int quantity) {
    this.quantityOnHand += quantity;
    this.quantityIncoming = Math.max(0, this.quantityIncoming - quantity);
    this.lastReceivedAt = Instant.now();
  }

  /**
   * Adjust stock (for inventory counts).
   */
  public void adjustStock(int newQuantity) {
    this.quantityOnHand = newQuantity;
    this.lastCountedAt = Instant.now();
  }

  /**
   * Calculate inventory value.
   */
  public BigDecimal getInventoryValue() {
    if (unitCost == null) {
      return BigDecimal.ZERO;
    }
    return unitCost.multiply(BigDecimal.valueOf(quantityOnHand));
  }
}
