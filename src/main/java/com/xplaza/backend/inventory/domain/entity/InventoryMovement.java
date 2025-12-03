/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.inventory.domain.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

import lombok.*;

/**
 * Inventory movement/transaction record.
 */
@Entity
@Table(name = "inventory_movements", indexes = {
    @Index(name = "idx_movement_inventory", columnList = "inventory_id"),
    @Index(name = "idx_movement_type", columnList = "type"),
    @Index(name = "idx_movement_reference", columnList = "reference_type, reference_id"),
    @Index(name = "idx_movement_created", columnList = "created_at")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class InventoryMovement {

  @Id
  @Column(name = "movement_id")
  @Builder.Default
  private UUID movementId = UUID.randomUUID();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "inventory_id", nullable = false)
  private InventoryItem inventoryItem;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 30)
  private MovementType type;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @Column(name = "quantity_before", nullable = false)
  private Integer quantityBefore;

  @Column(name = "quantity_after", nullable = false)
  private Integer quantityAfter;

  // Reference to source document
  @Enumerated(EnumType.STRING)
  @Column(name = "reference_type", length = 30)
  private ReferenceType referenceType;

  @Column(name = "reference_id", length = 50)
  private String referenceId;

  @Column(name = "reason", columnDefinition = "TEXT")
  private String reason;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  // Who made the change
  @Column(name = "created_by")
  private Long createdBy;

  @Column(name = "created_at")
  @Builder.Default
  private Instant createdAt = Instant.now();

  public enum MovementType {
    /** Stock received from supplier */
    RECEIVE,
    /** Stock shipped for order */
    SHIP,
    /** Stock reserved for order */
    RESERVE,
    /** Reserved stock released */
    RELEASE,
    /** Stock returned from customer */
    RETURN,
    /** Stock adjustment (count correction) */
    ADJUSTMENT,
    /** Stock damaged */
    DAMAGE,
    /** Stock transferred between warehouses */
    TRANSFER_OUT,
    /** Stock received from transfer */
    TRANSFER_IN,
    /** Stock written off */
    WRITE_OFF,
    /** Stock moved for quality hold */
    QUALITY_HOLD,
    /** Stock released from quality hold */
    QUALITY_RELEASE
  }

  public enum ReferenceType {
    ORDER,
    PURCHASE_ORDER,
    RETURN_REQUEST,
    TRANSFER,
    INVENTORY_COUNT,
    MANUAL_ADJUSTMENT
  }

  /**
   * Create a receive movement.
   */
  public static InventoryMovement createReceive(InventoryItem item, int quantity, String purchaseOrderId,
      Long userId) {
    int before = item.getQuantityOnHand();
    return InventoryMovement.builder()
        .inventoryItem(item)
        .type(MovementType.RECEIVE)
        .quantity(quantity)
        .quantityBefore(before)
        .quantityAfter(before + quantity)
        .referenceType(ReferenceType.PURCHASE_ORDER)
        .referenceId(purchaseOrderId)
        .createdBy(userId)
        .build();
  }

  /**
   * Create a ship movement.
   */
  public static InventoryMovement createShip(InventoryItem item, int quantity, Long orderId, Long userId) {
    int before = item.getQuantityOnHand();
    return InventoryMovement.builder()
        .inventoryItem(item)
        .type(MovementType.SHIP)
        .quantity(-quantity)
        .quantityBefore(before)
        .quantityAfter(before - quantity)
        .referenceType(ReferenceType.ORDER)
        .referenceId(String.valueOf(orderId))
        .createdBy(userId)
        .build();
  }

  /**
   * Create an adjustment movement.
   */
  public static InventoryMovement createAdjustment(InventoryItem item, int newQuantity, String reason, Long userId) {
    int before = item.getQuantityOnHand();
    int diff = newQuantity - before;
    return InventoryMovement.builder()
        .inventoryItem(item)
        .type(MovementType.ADJUSTMENT)
        .quantity(diff)
        .quantityBefore(before)
        .quantityAfter(newQuantity)
        .referenceType(ReferenceType.INVENTORY_COUNT)
        .reason(reason)
        .createdBy(userId)
        .build();
  }
}
