/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.fulfillment.domain.entity;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;

import lombok.*;

/**
 * Return merchandise authorization (RMA).
 */
@Entity
@Table(name = "returns", indexes = {
    @Index(name = "idx_returns_order", columnList = "order_id"),
    @Index(name = "idx_returns_customer", columnList = "customer_id"),
    @Index(name = "idx_returns_status", columnList = "status"),
    @Index(name = "idx_returns_rma", columnList = "rma_number")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Return {

  @Id
  @Column(name = "return_id")
  @Builder.Default
  private UUID returnId = UUID.randomUUID();

  @Column(name = "rma_number", nullable = false, unique = true, length = 50)
  private String rmaNumber;

  @Column(name = "order_id", nullable = false)
  private Long orderId;

  @Column(name = "customer_id", nullable = false)
  private Long customerId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  @Builder.Default
  private ReturnStatus status = ReturnStatus.REQUESTED;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 20)
  @Builder.Default
  private ReturnType type = ReturnType.RETURN;

  @Enumerated(EnumType.STRING)
  @Column(name = "reason", nullable = false, length = 50)
  private ReturnReason reason;

  @Column(name = "reason_detail", columnDefinition = "TEXT")
  private String reasonDetail;

  @Enumerated(EnumType.STRING)
  @Column(name = "resolution", length = 30)
  private Resolution resolution;

  // Return shipping
  @Column(name = "return_label_url", length = 500)
  private String returnLabelUrl;

  @Column(name = "return_tracking_number", length = 100)
  private String returnTrackingNumber;

  @Column(name = "return_carrier", length = 50)
  private String returnCarrier;

  // Return address
  @Column(name = "return_address_line1", length = 255)
  private String returnAddressLine1;

  @Column(name = "return_address_line2", length = 255)
  private String returnAddressLine2;

  @Column(name = "return_city", length = 100)
  private String returnCity;

  @Column(name = "return_state", length = 100)
  private String returnState;

  @Column(name = "return_postal_code", length = 20)
  private String returnPostalCode;

  @Column(name = "return_country_code", length = 2)
  private String returnCountryCode;

  // Processing
  @Column(name = "approved_by")
  private Long approvedBy;

  @Column(name = "internal_notes", columnDefinition = "TEXT")
  private String internalNotes;

  @Column(name = "customer_notes", columnDefinition = "TEXT")
  private String customerNotes;

  // Exchange info (if type = EXCHANGE)
  @Column(name = "exchange_order_id")
  private Long exchangeOrderId;

  // Store credit (if resolution = STORE_CREDIT)
  @Column(name = "store_credit_code", length = 50)
  private String storeCreditCode;

  // Dates
  @Column(name = "expires_at")
  private Instant expiresAt;

  @Column(name = "shipped_at")
  private Instant shippedAt;

  @Column(name = "received_at")
  private Instant receivedAt;

  @Column(name = "inspected_at")
  private Instant inspectedAt;

  @Column(name = "completed_at")
  private Instant completedAt;

  @Column(name = "created_at")
  @Builder.Default
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at")
  @Builder.Default
  private Instant updatedAt = Instant.now();

  @OneToMany(mappedBy = "returnRequest", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @Builder.Default
  private List<ReturnItem> items = new ArrayList<>();

  public enum ReturnStatus {
    /** Return requested */
    REQUESTED,
    /** Approved by admin */
    APPROVED,
    /** Return label generated */
    LABEL_GENERATED,
    /** Item shipped by customer */
    SHIPPED,
    /** Item received at warehouse */
    RECEIVED,
    /** Item being inspected */
    INSPECTING,
    /** Inspection passed */
    INSPECTION_PASSED,
    /** Inspection failed */
    INSPECTION_FAILED,
    /** Refund/exchange processing */
    PROCESSING,
    /** Return completed */
    COMPLETED,
    /** Return rejected */
    REJECTED,
    /** Return cancelled */
    CANCELLED
  }

  public enum ReturnType {
    /** Full return for refund */
    RETURN,
    /** Exchange for different item */
    EXCHANGE,
    /** Repair warranty claim */
    WARRANTY_REPAIR,
    /** Replace defective item */
    WARRANTY_REPLACE
  }

  public enum ReturnReason {
    DAMAGED,
    WRONG_ITEM,
    NOT_AS_DESCRIBED,
    DEFECTIVE,
    CHANGED_MIND,
    SIZE_ISSUE,
    COLOR_DIFFERENCE,
    QUALITY_ISSUE,
    LATE_DELIVERY,
    OTHER
  }

  public enum Resolution {
    REFUND,
    EXCHANGE,
    STORE_CREDIT,
    REPAIR,
    REPLACEMENT,
    REJECTED
  }

  @PrePersist
  protected void onCreate() {
    if (rmaNumber == null) {
      rmaNumber = generateRmaNumber();
    }
    if (expiresAt == null) {
      expiresAt = Instant.now().plus(30, ChronoUnit.DAYS);
    }
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = Instant.now();
  }

  private String generateRmaNumber() {
    return "RMA-" + System.currentTimeMillis() + "-" + (int) (Math.random() * 1000);
  }

  public void addItem(ReturnItem item) {
    items.add(item);
    item.setReturnRequest(this);
  }

  /**
   * Approve the return request.
   */
  public void approve(Long adminId) {
    this.status = ReturnStatus.APPROVED;
    this.approvedBy = adminId;
  }

  /**
   * Reject the return request.
   */
  public void reject(Long adminId, String reason) {
    this.status = ReturnStatus.REJECTED;
    this.approvedBy = adminId;
    this.internalNotes = reason;
    this.resolution = Resolution.REJECTED;
  }

  /**
   * Mark as shipped by customer.
   */
  public void markShipped(String trackingNumber) {
    this.status = ReturnStatus.SHIPPED;
    this.returnTrackingNumber = trackingNumber;
    this.shippedAt = Instant.now();
  }

  /**
   * Mark as received at warehouse.
   */
  public void markReceived() {
    this.status = ReturnStatus.RECEIVED;
    this.receivedAt = Instant.now();
  }

  /**
   * Complete inspection.
   */
  public void completeInspection(boolean passed) {
    this.status = passed ? ReturnStatus.INSPECTION_PASSED : ReturnStatus.INSPECTION_FAILED;
    this.inspectedAt = Instant.now();
  }

  /**
   * Complete the return process.
   */
  public void complete(Resolution resolution) {
    this.status = ReturnStatus.COMPLETED;
    this.resolution = resolution;
    this.completedAt = Instant.now();
  }

  /**
   * Cancel the return.
   */
  public void cancel() {
    this.status = ReturnStatus.CANCELLED;
  }

  public boolean isExpired() {
    return expiresAt != null && Instant.now().isAfter(expiresAt);
  }
}
