/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.order.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import jakarta.persistence.*;

import lombok.*;

/**
 * Checkout session captures the checkout process before order confirmation.
 *
 * This allows customers to: - Review their cart - Select shipping address -
 * Choose delivery slot - Select payment method - Apply coupons
 *
 * The checkout session expires after a certain time if not completed.
 */
@Entity
@Table(name = "checkout_sessions", indexes = {
    @Index(name = "idx_checkout_cart", columnList = "cart_id"),
    @Index(name = "idx_checkout_customer", columnList = "customer_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CheckoutSession {

  @Id
  @Column(name = "checkout_id")
  @Builder.Default
  private UUID checkoutId = UUID.randomUUID();

  @Column(name = "cart_id", nullable = false)
  private UUID cartId;

  @Column(name = "customer_id", nullable = false)
  private Long customerId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 30)
  @Builder.Default
  private CheckoutStatus status = CheckoutStatus.STARTED;

  // Step tracking
  @Column(name = "current_step", length = 30)
  @Builder.Default
  private String currentStep = "SHIPPING";

  @Column(name = "shipping_completed")
  @Builder.Default
  private Boolean shippingCompleted = false;

  @Column(name = "payment_completed")
  @Builder.Default
  private Boolean paymentCompleted = false;

  @Column(name = "review_completed")
  @Builder.Default
  private Boolean reviewCompleted = false;

  // Shipping selection
  @Column(name = "shipping_address_id")
  private Long shippingAddressId;

  @Column(name = "shipping_method_id")
  private Long shippingMethodId;

  @Column(name = "shipping_method_name", length = 100)
  private String shippingMethodName;

  @Column(name = "shipping_cost", precision = 15, scale = 2)
  @Builder.Default
  private BigDecimal shippingCost = BigDecimal.ZERO;

  // Delivery scheduling
  @Column(name = "requested_delivery_date")
  private LocalDate requestedDeliveryDate;

  @Column(name = "delivery_slot_start")
  private LocalTime deliverySlotStart;

  @Column(name = "delivery_slot_end")
  private LocalTime deliverySlotEnd;

  @Column(name = "delivery_instructions", columnDefinition = "TEXT")
  private String deliveryInstructions;

  // Billing
  @Column(name = "billing_address_id")
  private Long billingAddressId;

  @Column(name = "billing_same_as_shipping")
  @Builder.Default
  private Boolean billingSameAsShipping = true;

  // Payment selection
  @Column(name = "payment_method_id")
  private Long paymentMethodId;

  @Column(name = "payment_method_type", length = 50)
  private String paymentMethodType;

  // Pricing calculated at checkout
  @Column(name = "subtotal", precision = 15, scale = 2)
  private BigDecimal subtotal;

  @Column(name = "discount_amount", precision = 15, scale = 2)
  @Builder.Default
  private BigDecimal discountAmount = BigDecimal.ZERO;

  @Column(name = "tax_amount", precision = 15, scale = 2)
  @Builder.Default
  private BigDecimal taxAmount = BigDecimal.ZERO;

  @Column(name = "grand_total", precision = 15, scale = 2)
  private BigDecimal grandTotal;

  @Column(name = "currency", length = 3)
  @Builder.Default
  private String currency = "USD";

  // Coupon applied
  @Column(name = "coupon_id")
  private Long couponId;

  @Column(name = "coupon_code", length = 50)
  private String couponCode;

  @Column(name = "coupon_discount_amount", precision = 15, scale = 2)
  private BigDecimal couponDiscountAmount;

  // Customer notes
  @Column(name = "customer_notes", columnDefinition = "TEXT")
  private String customerNotes;

  // Result
  @Column(name = "order_id")
  private UUID orderId;

  @Column(name = "failure_reason", length = 500)
  private String failureReason;

  // Session management
  @Column(name = "expires_at")
  private Instant expiresAt;

  @Column(name = "created_at")
  @Builder.Default
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at")
  @Builder.Default
  private Instant updatedAt = Instant.now();

  @Column(name = "completed_at")
  private Instant completedAt;

  public enum CheckoutStatus {
    /** Checkout started */
    STARTED,
    /** Shipping selected */
    SHIPPING_SELECTED,
    /** Payment selected */
    PAYMENT_SELECTED,
    /** Awaiting payment confirmation */
    AWAITING_PAYMENT,
    /** Checkout completed, order created */
    COMPLETED,
    /** Checkout abandoned/expired */
    ABANDONED,
    /** Checkout failed (e.g., payment failed) */
    FAILED
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = Instant.now();
  }

  /**
   * Calculate grand total based on components.
   */
  public void calculateGrandTotal() {
    BigDecimal total = subtotal != null ? subtotal : BigDecimal.ZERO;
    total = total.add(shippingCost != null ? shippingCost : BigDecimal.ZERO);
    total = total.add(taxAmount != null ? taxAmount : BigDecimal.ZERO);
    total = total.subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
    if (total.compareTo(BigDecimal.ZERO) < 0) {
      total = BigDecimal.ZERO;
    }
    this.grandTotal = total;
  }

  /**
   * Check if checkout is ready for order creation.
   */
  public boolean isReadyForOrder() {
    return shippingAddressId != null
        && paymentMethodId != null
        && shippingCompleted
        && paymentCompleted
        && status != CheckoutStatus.COMPLETED
        && status != CheckoutStatus.FAILED
        && status != CheckoutStatus.ABANDONED;
  }

  /**
   * Check if checkout session has expired.
   */
  public boolean isExpired() {
    return expiresAt != null && Instant.now().isAfter(expiresAt);
  }

  /**
   * Mark checkout as completed.
   */
  public void complete(UUID orderId) {
    this.orderId = orderId;
    this.status = CheckoutStatus.COMPLETED;
    this.completedAt = Instant.now();
  }

  /**
   * Mark checkout as failed.
   */
  public void fail(String reason) {
    this.status = CheckoutStatus.FAILED;
    this.failureReason = reason;
  }

  /**
   * Mark checkout as abandoned.
   */
  public void abandon() {
    this.status = CheckoutStatus.ABANDONED;
  }

  /**
   * Set default expiration (30 minutes from now).
   */
  public void setDefaultExpiration() {
    this.expiresAt = Instant.now().plusSeconds(30 * 60);
  }
}
