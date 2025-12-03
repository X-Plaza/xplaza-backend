/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.payment.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

import lombok.*;

/**
 * Individual line item in a refund request.
 */
@Entity
@Table(name = "refund_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RefundItem {

  @Id
  @Column(name = "refund_item_id")
  @Builder.Default
  private UUID refundItemId = UUID.randomUUID();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "refund_id", nullable = false)
  private Refund refund;

  @Column(name = "order_item_id", nullable = false)
  private Long orderItemId;

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

  @Column(name = "amount", nullable = false, precision = 15, scale = 2)
  private BigDecimal amount;

  @Enumerated(EnumType.STRING)
  @Column(name = "condition_at_return", length = 30)
  private ItemCondition conditionAtReturn;

  @Column(name = "notes", columnDefinition = "TEXT")
  private String notes;

  @Column(name = "created_at")
  @Builder.Default
  private Instant createdAt = Instant.now();

  public enum ItemCondition {
    NEW,
    OPENED,
    DAMAGED,
    DEFECTIVE,
    USED
  }

  /**
   * Calculate amount from quantity and unit price.
   */
  public void calculateAmount() {
    this.amount = unitPrice.multiply(BigDecimal.valueOf(quantity));
  }
}
