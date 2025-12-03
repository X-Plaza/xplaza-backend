/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.cart.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import jakarta.persistence.*;

import lombok.*;

/**
 * Aggregate root representing a shopping cart. Supports both guest and
 * authenticated customers.
 */
@Entity
@Table(name = "shopping_carts", indexes = {
    @Index(name = "idx_cart_customer_id", columnList = "customer_id"),
    @Index(name = "idx_cart_session_id", columnList = "session_id"),
    @Index(name = "idx_cart_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Cart {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  /** Customer ID for authenticated users (nullable for guests) */
  @Column(name = "customer_id")
  private Long customerId;

  /** Session ID for guest carts */
  @Column(name = "session_id")
  private String sessionId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private CartStatus status = CartStatus.ACTIVE;

  /** Currency code (ISO 4217) */
  @Column(name = "currency_code", length = 3)
  @Builder.Default
  private String currencyCode = "USD";

  @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @Builder.Default
  private List<CartItem> items = new ArrayList<>();

  /** Applied coupon code */
  @Column(name = "coupon_code", length = 50)
  private String couponCode;

  /** Discount amount from coupon */
  @Column(name = "coupon_discount", precision = 10, scale = 2)
  @Builder.Default
  private BigDecimal couponDiscount = BigDecimal.ZERO;

  /** Notes or special instructions */
  @Column(columnDefinition = "TEXT")
  private String notes;

  @Column(name = "created_at", nullable = false, updatable = false)
  @Builder.Default
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  @Builder.Default
  private Instant updatedAt = Instant.now();

  /** When the cart expires (for cleanup of abandoned carts) */
  @Column(name = "expires_at")
  private Instant expiresAt;

  /** Last activity timestamp */
  @Column(name = "last_activity_at")
  @Builder.Default
  private Instant lastActivityAt = Instant.now();

  // ==================== Status enum ====================

  public enum CartStatus {
    /** Active shopping cart */
    ACTIVE,
    /** Merged with another cart (e.g., guest to authenticated) */
    MERGED,
    /** Converted to order */
    CONVERTED,
    /** Cart abandoned and expired */
    ABANDONED,
    /** Cart cleared by user */
    CLEARED
  }

  // ==================== Lifecycle ====================

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = Instant.now();
    this.lastActivityAt = Instant.now();
  }

  // ==================== Business methods ====================

  /**
   * Check if the cart is empty.
   */
  public boolean isEmpty() {
    return items == null || items.isEmpty() || getActiveItems().isEmpty();
  }

  /**
   * Get active (non-removed) items.
   */
  public List<CartItem> getActiveItems() {
    if (items == null)
      return List.of();
    return items.stream()
        .filter(item -> item.getStatus() == CartItem.ItemStatus.ACTIVE)
        .collect(Collectors.toList());
  }

  /**
   * Get items saved for later.
   */
  public List<CartItem> getSavedItems() {
    if (items == null)
      return List.of();
    return items.stream()
        .filter(item -> item.getStatus() == CartItem.ItemStatus.SAVED_FOR_LATER)
        .collect(Collectors.toList());
  }

  /**
   * Add an item to the cart.
   */
  public CartItem addItem(Long productId, Long variantId, Long shopId, int quantity, BigDecimal unitPrice) {
    // Check if item already exists
    CartItem existingItem = findItem(productId, variantId);
    if (existingItem != null && existingItem.getStatus() == CartItem.ItemStatus.ACTIVE) {
      existingItem.setQuantity(existingItem.getQuantity() + quantity);
      return existingItem;
    }

    // Create new item
    CartItem newItem = CartItem.builder()
        .cart(this)
        .productId(productId)
        .variantId(variantId)
        .shopId(shopId)
        .quantity(quantity)
        .unitPrice(unitPrice)
        .build();

    if (this.items == null) {
      this.items = new ArrayList<>();
    }
    this.items.add(newItem);
    return newItem;
  }

  /**
   * Remove an item from the cart.
   */
  public boolean removeItem(UUID itemId) {
    if (items == null)
      return false;
    return items.removeIf(item -> item.getId().equals(itemId));
  }

  /**
   * Find an item by product and variant.
   */
  public CartItem findItem(Long productId, Long variantId) {
    if (items == null)
      return null;
    return items.stream()
        .filter(item -> item.getProductId().equals(productId) &&
            ((variantId == null && item.getVariantId() == null) ||
                (variantId != null && variantId.equals(item.getVariantId()))))
        .findFirst()
        .orElse(null);
  }

  /**
   * Get item by ID.
   */
  public CartItem getItem(UUID itemId) {
    if (items == null)
      return null;
    return items.stream()
        .filter(item -> item.getId().equals(itemId))
        .findFirst()
        .orElse(null);
  }

  /**
   * Update item quantity.
   */
  public boolean updateItemQuantity(UUID itemId, int newQuantity) {
    CartItem item = getItem(itemId);
    if (item == null)
      return false;

    if (newQuantity <= 0) {
      return removeItem(itemId);
    }

    item.setQuantity(newQuantity);
    return true;
  }

  /**
   * Save an item for later.
   */
  public boolean saveItemForLater(UUID itemId) {
    CartItem item = getItem(itemId);
    if (item == null)
      return false;

    item.saveForLater();
    return true;
  }

  /**
   * Move item back to cart from saved for later.
   */
  public boolean moveToCart(UUID itemId) {
    CartItem item = getItem(itemId);
    if (item == null)
      return false;

    item.moveToCart();
    return true;
  }

  /**
   * Calculate subtotal (sum of all active items).
   */
  public BigDecimal getSubtotal() {
    return getActiveItems().stream()
        .map(CartItem::getLineTotal)
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }

  /**
   * Get total item count.
   */
  public int getTotalItemCount() {
    return getActiveItems().stream()
        .mapToInt(CartItem::getQuantity)
        .sum();
  }

  /**
   * Get unique item count.
   */
  public int getUniqueItemCount() {
    return getActiveItems().size();
  }

  /**
   * Calculate total after discounts.
   */
  public BigDecimal getTotal() {
    BigDecimal subtotal = getSubtotal();
    if (couponDiscount != null && couponDiscount.compareTo(BigDecimal.ZERO) > 0) {
      subtotal = subtotal.subtract(couponDiscount);
    }
    return subtotal.max(BigDecimal.ZERO);
  }

  /**
   * Apply a coupon code.
   */
  public void applyCoupon(String code, BigDecimal discount) {
    this.couponCode = code;
    this.couponDiscount = discount;
  }

  /**
   * Remove applied coupon.
   */
  public void removeCoupon() {
    this.couponCode = null;
    this.couponDiscount = BigDecimal.ZERO;
  }

  /**
   * Clear the cart (remove all items).
   */
  public void clear() {
    if (items != null) {
      items.clear();
    }
    removeCoupon();
    this.status = CartStatus.CLEARED;
  }

  /**
   * Mark the cart as converted to order.
   */
  public void markConverted() {
    this.status = CartStatus.CONVERTED;
  }

  /**
   * Mark the cart as merged.
   */
  public void markMerged() {
    this.status = CartStatus.MERGED;
  }

  /**
   * Check if cart is owned by customer.
   */
  public boolean isOwnedBy(Long customerId) {
    return this.customerId != null && this.customerId.equals(customerId);
  }

  /**
   * Check if cart is a guest cart.
   */
  public boolean isGuestCart() {
    return this.customerId == null && this.sessionId != null;
  }

  /**
   * Assign cart to a customer (for guest cart merge).
   */
  public void assignToCustomer(Long customerId) {
    this.customerId = customerId;
  }

  /**
   * Check if cart is still active.
   */
  public boolean isActive() {
    return this.status == CartStatus.ACTIVE;
  }

  /**
   * Check if cart has expired.
   */
  public boolean isExpired() {
    return this.expiresAt != null && Instant.now().isAfter(this.expiresAt);
  }
}
