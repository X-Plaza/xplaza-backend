/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.payment.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;

import lombok.*;

/**
 * Refund request for an order.
 * 
 * Handles both full and partial refunds with detailed tracking.
 */
@Entity
@Table(name = "refunds")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Refund {

  @Id
  @Column(name = "refund_id")
  @Builder.Default
  private UUID refundId = UUID.randomUUID();

  @Column(name = "order_id", nullable = false)
  private UUID orderId;

  /**
   * Original payment transaction being refunded.
   */
  @Column(name = "transaction_id")
  private UUID transactionId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  @Builder.Default
  private RefundStatus status = RefundStatus.PENDING;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 20)
  @Builder.Default
  private RefundType type = RefundType.FULL;

  // Amounts
  @Column(name = "items_amount", precision = 15, scale = 2)
  @Builder.Default
  private BigDecimal itemsAmount = BigDecimal.ZERO;

  @Column(name = "shipping_amount", precision = 15, scale = 2)
  @Builder.Default
  private BigDecimal shippingAmount = BigDecimal.ZERO;

  @Column(name = "tax_amount", precision = 15, scale = 2)
  @Builder.Default
  private BigDecimal taxAmount = BigDecimal.ZERO;

  @Column(name = "total_amount", nullable = false, precision = 15, scale = 2)
  private BigDecimal totalAmount;

  @Column(name = "currency", nullable = false, length = 3)
  private String currency;

  // Reason
  @Enumerated(EnumType.STRING)
  @Column(name = "reason", nullable = false, length = 50)
  private RefundReason reason;

  @Column(name = "reason_detail", columnDefinition = "TEXT")
  private String reasonDetail;

  @Column(name = "internal_note", columnDefinition = "TEXT")
  private String internalNote;

  // Actors
  @Column(name = "requested_by", nullable = false)
  private Long requestedBy;

  @Enumerated(EnumType.STRING)
  @Column(name = "requested_by_type", nullable = false, length = 20)
  private RequesterType requestedByType;

  @Column(name = "approved_by")
  private Long approvedBy;

  // Gateway info
  @Column(name = "gateway_refund_id", length = 255)
  private String gatewayRefundId;

  // Timestamps
  @Column(name = "approved_at")
  private Instant approvedAt;

  @Column(name = "processed_at")
  private Instant processedAt;

  @Column(name = "created_at")
  @Builder.Default
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at")
  @Builder.Default
  private Instant updatedAt = Instant.now();

  @OneToMany(mappedBy = "refund", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @Builder.Default
  private List<RefundItem> items = new ArrayList<>();

  public enum RefundStatus {
    /** Refund requested */
    PENDING,
    /** Approved by admin */
    APPROVED,
    /** Being processed with payment gateway */
    PROCESSING,
    /** Successfully refunded */
    COMPLETED,
    /** Refund rejected */
    REJECTED
  }

  public enum RefundType {
    /** Full order refund */
    FULL,
    /** Partial refund */
    PARTIAL,
    /** Store credit instead of payment refund */
    EXCHANGE_CREDIT
  }

  public enum RefundReason {
    DAMAGED,
    WRONG_ITEM,
    NOT_AS_DESCRIBED,
    DEFECTIVE,
    CHANGED_MIND,
    DUPLICATE_ORDER,
    NEVER_ARRIVED,
    BETTER_PRICE_ELSEWHERE,
    OTHER
  }

  public enum RequesterType {
    CUSTOMER,
    ADMIN
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = Instant.now();
  }

  /**
   * Approve the refund request.
   */
  public void approve(Long adminId) {
    this.status = RefundStatus.APPROVED;
    this.approvedBy = adminId;
    this.approvedAt = Instant.now();
  }

  /**
   * Reject the refund request.
   */
  public void reject(Long adminId, String reason) {
    this.status = RefundStatus.REJECTED;
    this.approvedBy = adminId;
    this.internalNote = reason;
  }

  /**
   * Mark as processing.
   */
  public void startProcessing() {
    this.status = RefundStatus.PROCESSING;
  }

  /**
   * Complete the refund.
   */
  public void complete(String gatewayRefundId) {
    this.status = RefundStatus.COMPLETED;
    this.gatewayRefundId = gatewayRefundId;
    this.processedAt = Instant.now();
  }

  public void addItem(RefundItem item) {
    items.add(item);
    item.setRefund(this);
  }

  /**
   * Calculate total from items if not set.
   */
  public void calculateTotal() {
    if (items != null && !items.isEmpty()) {
      this.itemsAmount = items.stream()
          .map(RefundItem::getAmount)
          .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
    this.totalAmount = itemsAmount
        .add(shippingAmount != null ? shippingAmount : BigDecimal.ZERO)
        .add(taxAmount != null ? taxAmount : BigDecimal.ZERO);
  }
}
