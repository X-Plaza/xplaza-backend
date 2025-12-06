/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.fulfillment.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

import lombok.*;

/**
 * Individual item in a return request.
 */
@Entity
@Table(name = "return_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReturnItem {

  @Id
  @Column(name = "return_item_id")
  @Builder.Default
  private UUID returnItemId = UUID.randomUUID();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "return_id", nullable = false)
  private Return returnRequest;

  @Column(name = "order_item_id", nullable = false)
  private UUID orderItemId;

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Column(name = "variant_id")
  private UUID variantId;

  @Column(name = "product_name", nullable = false, length = 255)
  private String productName;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
  private BigDecimal unitPrice;

  @Column(name = "total_price", nullable = false, precision = 15, scale = 2)
  private BigDecimal totalPrice;

  // Inspection results
  @Enumerated(EnumType.STRING)
  @Column(name = "received_condition", length = 30)
  private Return.ReturnReason receivedCondition;

  @Enumerated(EnumType.STRING)
  @Column(name = "inspection_result", length = 30)
  private InspectionResult inspectionResult;

  @Column(name = "inspection_notes", columnDefinition = "TEXT")
  private String inspectionNotes;

  @Column(name = "photos", columnDefinition = "TEXT")
  private String photos;

  // Exchange info
  @Column(name = "exchange_product_id")
  private Long exchangeProductId;

  @Column(name = "exchange_variant_id")
  private UUID exchangeVariantId;

  @Column(name = "created_at")
  @Builder.Default
  private Instant createdAt = Instant.now();

  public enum InspectionResult {
    PENDING,
    PASSED,
    FAILED_DAMAGED,
    FAILED_WRONG_ITEM,
    FAILED_MISSING_PARTS,
    FAILED_USED
  }

  /**
   * Calculate total price from quantity and unit price.
   */
  public void calculateTotalPrice() {
    this.totalPrice = unitPrice.multiply(BigDecimal.valueOf(quantity));
  }

  /**
   * Complete inspection.
   */
  public void completeInspection(InspectionResult result, String notes) {
    this.inspectionResult = result;
    this.inspectionNotes = notes;
  }
}
