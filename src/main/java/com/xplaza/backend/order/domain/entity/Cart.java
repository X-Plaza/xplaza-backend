/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.order.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;

import lombok.*;

/**
 * Shopping Cart represents a customer's collection of items they intend to
 * purchase.
 * 
 * Features: - Supports both guest (session-based) and logged-in
 * (customer-based) carts - Cart expiration for abandoned cart tracking -
 * Automatic price recalculation - Coupon application - Save for later
 * functionality
 */
@Entity
@Table(name = "carts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

  @Id
  @Column(name = "cart_id")
  @Builder.Default
  private UUID cartId = UUID.randomUUID();

  /**
   * Customer ID for logged-in users. Null for guest carts.
   */
  @Column(name = "customer_id")
  private Long customerId;

  /**
   * Session ID for guest carts. Used to identify the cart before login.
   */
  @Column(name = "session_id", length = 100)
  private String sessionId;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 20)
  @Builder.Default
  private CartStatus status = CartStatus.ACTIVE;

  /**
   * Currency code (ISO 4217).
   */
  @Column(name = "currency", length = 3)
  @Builder.Default
  private String currency = "USD";

  // Calculated totals (denormalized for performance)
  @Column(name = "subtotal", precision = 15, scale = 2)
  @Builder.Default
  private BigDecimal subtotal = BigDecimal.ZERO;

  @Column(name = "discount_total", precision = 15, scale = 2)
  @Builder.Default
  private BigDecimal discountTotal = BigDecimal.ZERO;

  @Column(name = "shipping_estimate", precision = 15, scale = 2)
  @Builder.Default
  private BigDecimal shippingEstimate = BigDecimal.ZERO;

  @Column(name = "tax_estimate", precision = 15, scale = 2)
  @Builder.Default
  private BigDecimal taxEstimate = BigDecimal.ZERO;

  @Column(name = "total_estimate", precision = 15, scale = 2)
  @Builder.Default
  private BigDecimal totalEstimate = BigDecimal.ZERO;

  @Column(name = "item_count")
  @Builder.Default
  private Integer itemCount = 0;

  /**
   * If this cart was converted to an order, the order ID.
   */
  @Column(name = "converted_order_id")
  private UUID convertedOrderId;

  /**
   * When this cart expires and becomes abandoned.
   */
  @Column(name = "expires_at")
  private Instant expiresAt;

  @Column(name = "created_at")
  @Builder.Default
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at")
  @Builder.Default
  private Instant updatedAt = Instant.now();

  @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @Builder.Default
  private List<CartItem> items = new ArrayList<>();

  @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @Builder.Default
  private List<CartCoupon> appliedCoupons = new ArrayList<>();

  public enum CartStatus {
    /** Active shopping cart */
    ACTIVE,
    /** Merged with another cart (e.g., on login) */
    MERGED,
    /** Converted to an order */
    CONVERTED,
    /** Abandoned (expired) */
    ABANDONED
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = Instant.now();
  }

  /**
   * Add an item to the cart.
   */
  public void addItem(CartItem item) {
    items.add(item);
    item.setCart(this);
    recalculateTotals();
  }

  /**
   * Remove an item from the cart.
   */
  public void removeItem(CartItem item) {
    items.remove(item);
    item.setCart(null);
    recalculateTotals();
  }

  /**
   * Find an item by variant ID.
   */
  public CartItem findItemByVariant(UUID variantId) {
    return items.stream()
        .filter(item -> item.getVariantId() != null && item.getVariantId().equals(variantId))
        .findFirst()
        .orElse(null);
  }

  /**
   * Apply a coupon to the cart.
   */
  public void applyCoupon(CartCoupon coupon) {
    appliedCoupons.add(coupon);
    coupon.setCart(this);
    recalculateTotals();
  }

  /**
   * Remove a coupon from the cart.
   */
  public void removeCoupon(CartCoupon coupon) {
    appliedCoupons.remove(coupon);
    coupon.setCart(null);
    recalculateTotals();
  }

  /**
   * Recalculate all totals based on current items and coupons.
   */
  public void recalculateTotals() {
    // Calculate subtotal from active items (not saved for later)
    this.subtotal = items.stream()
        .filter(item -> !Boolean.TRUE.equals(item.getSavedForLater()))
        .map(CartItem::getTotalPrice)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    // Calculate discount total from items and coupons
    BigDecimal itemDiscounts = items.stream()
        .filter(item -> !Boolean.TRUE.equals(item.getSavedForLater()))
        .map(item -> item.getDiscountAmount() != null ? item.getDiscountAmount() : BigDecimal.ZERO)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    BigDecimal couponDiscounts = appliedCoupons.stream()
        .map(coupon -> coupon.getDiscountAmount() != null ? coupon.getDiscountAmount() : BigDecimal.ZERO)
        .reduce(BigDecimal.ZERO, BigDecimal::add);

    this.discountTotal = itemDiscounts.add(couponDiscounts);

    // Calculate total estimate
    this.totalEstimate = subtotal
        .subtract(discountTotal)
        .add(shippingEstimate)
        .add(taxEstimate);

    // Don't let total go negative
    if (this.totalEstimate.compareTo(BigDecimal.ZERO) < 0) {
      this.totalEstimate = BigDecimal.ZERO;
    }

    // Update item count (active items only)
    this.itemCount = items.stream()
        .filter(item -> !Boolean.TRUE.equals(item.getSavedForLater()))
        .mapToInt(CartItem::getQuantity)
        .sum();
  }

  /**
   * Get items that are in the cart (not saved for later).
   */
  public List<CartItem> getActiveItems() {
    return items.stream()
        .filter(item -> !Boolean.TRUE.equals(item.getSavedForLater()))
        .toList();
  }

  /**
   * Get items saved for later.
   */
  public List<CartItem> getSavedItems() {
    return items.stream()
        .filter(item -> Boolean.TRUE.equals(item.getSavedForLater()))
        .toList();
  }

  /**
   * Check if the cart is empty (no active items).
   */
  public boolean isEmpty() {
    return getActiveItems().isEmpty();
  }

  /**
   * Check if this is a guest cart.
   */
  public boolean isGuestCart() {
    return customerId == null;
  }

  /**
   * Mark this cart as converted to an order.
   */
  public void markAsConverted(UUID orderId) {
    this.status = CartStatus.CONVERTED;
    this.convertedOrderId = orderId;
  }

  /**
   * Mark this cart as abandoned.
   */
  public void markAsAbandoned() {
    this.status = CartStatus.ABANDONED;
  }

  /**
   * Mark this cart as merged into another cart.
   */
  public void markAsMerged() {
    this.status = CartStatus.MERGED;
  }

  /**
   * Set expiration time (default: 30 days from now).
   */
  public void setDefaultExpiration() {
    this.expiresAt = Instant.now().plusSeconds(30 * 24 * 60 * 60); // 30 days
  }
}
