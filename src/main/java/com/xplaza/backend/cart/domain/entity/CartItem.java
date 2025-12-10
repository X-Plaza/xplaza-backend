/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.cart.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

import lombok.*;

/**
 * Entity representing an item in a shopping cart.
 */
@Entity
@Table(name = "cart_items", indexes = {
    @Index(name = "idx_cart_item_cart_id", columnList = "cart_id"),
    @Index(name = "idx_cart_item_product_id", columnList = "product_id"),
    @Index(name = "idx_cart_item_variant_id", columnList = "variant_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "cart")
public class CartItem {

  @Id
  @GeneratedValue(strategy = GenerationType.UUID)
  private UUID id;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cart_id", nullable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private Cart cart;

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Column(name = "variant_id")
  private UUID variantId;

  @Column(name = "shop_id", nullable = false)
  private Long shopId;

  /** Quantity in cart */
  @Column(nullable = false)
  @Builder.Default
  private Integer quantity = 1;

  /**
   * Unit price at time of adding to cart (may differ from current product price)
   */
  @Column(name = "unit_price", precision = 10, scale = 2, nullable = false)
  private BigDecimal unitPrice;

  /** Original price before any item-level discount */
  @Column(name = "original_price", precision = 10, scale = 2)
  private BigDecimal originalPrice;

  /** Item-level discount amount */
  @Column(name = "discount_amount", precision = 10, scale = 2)
  @Builder.Default
  private BigDecimal discountAmount = BigDecimal.ZERO;

  /** Discount percentage (if applicable) */
  @Column(name = "discount_percentage", precision = 5, scale = 2)
  private BigDecimal discountPercentage;

  /** Product name snapshot */
  @Column(name = "product_name")
  private String productName;

  /** Variant name snapshot (e.g., "Red / Large") */
  @Column(name = "variant_name")
  private String variantName;

  /** Product SKU snapshot */
  @Column(name = "sku", length = 50)
  private String sku;

  /** Product image URL snapshot */
  @Column(name = "image_url")
  private String imageUrl;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false)
  @Builder.Default
  private ItemStatus status = ItemStatus.ACTIVE;

  @Column(name = "added_at", nullable = false, updatable = false)
  @Builder.Default
  private Instant addedAt = Instant.now();

  @Column(name = "updated_at", nullable = false)
  @Builder.Default
  private Instant updatedAt = Instant.now();

  /** Custom attributes as JSON */
  @Column(name = "custom_attributes", columnDefinition = "TEXT")
  private String customAttributes;

  // ==================== Status enum ====================

  public enum ItemStatus {
    /** Item is active in cart */
    ACTIVE,
    /** Item saved for later */
    SAVED_FOR_LATER,
    /** Item removed from cart */
    REMOVED,
    /** Item out of stock */
    OUT_OF_STOCK
  }

  // ==================== Lifecycle ====================

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = Instant.now();
  }

  // ==================== Business methods ====================

  /**
   * Calculate line total (unit price * quantity - discount).
   */
  public BigDecimal getLineTotal() {
    BigDecimal total = unitPrice.multiply(BigDecimal.valueOf(quantity));
    if (discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0) {
      total = total.subtract(discountAmount);
    }
    return total.max(BigDecimal.ZERO);
  }

  /**
   * Calculate line total before discount.
   */
  public BigDecimal getLineTotalBeforeDiscount() {
    BigDecimal price = originalPrice != null ? originalPrice : unitPrice;
    return price.multiply(BigDecimal.valueOf(quantity));
  }

  /**
   * Get total savings on this line item.
   */
  public BigDecimal getTotalSavings() {
    return getLineTotalBeforeDiscount().subtract(getLineTotal());
  }

  /**
   * Check if item has a discount.
   */
  public boolean hasDiscount() {
    return (discountAmount != null && discountAmount.compareTo(BigDecimal.ZERO) > 0) ||
        (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0);
  }

  /**
   * Save this item for later.
   */
  public void saveForLater() {
    this.status = ItemStatus.SAVED_FOR_LATER;
  }

  /**
   * Move this item back to active cart.
   */
  public void moveToCart() {
    this.status = ItemStatus.ACTIVE;
  }

  /**
   * Mark item as removed.
   */
  public void markRemoved() {
    this.status = ItemStatus.REMOVED;
  }

  /**
   * Mark item as out of stock.
   */
  public void markOutOfStock() {
    this.status = ItemStatus.OUT_OF_STOCK;
  }

  /**
   * Update price (when product price changes).
   */
  public void updatePrice(BigDecimal newUnitPrice, BigDecimal newOriginalPrice) {
    this.unitPrice = newUnitPrice;
    this.originalPrice = newOriginalPrice;
  }

  /**
   * Apply a discount.
   */
  public void applyDiscount(BigDecimal amount, BigDecimal percentage) {
    this.discountAmount = amount;
    this.discountPercentage = percentage;
  }

  /**
   * Remove discount.
   */
  public void removeDiscount() {
    this.discountAmount = BigDecimal.ZERO;
    this.discountPercentage = null;
  }

  /**
   * Increment quantity.
   */
  public void incrementQuantity(int amount) {
    this.quantity = this.quantity + amount;
  }

  /**
   * Decrement quantity.
   */
  public boolean decrementQuantity(int amount) {
    if (this.quantity - amount < 1) {
      return false;
    }
    this.quantity = this.quantity - amount;
    return true;
  }

  /**
   * Check if item is active.
   */
  public boolean isActive() {
    return this.status == ItemStatus.ACTIVE;
  }

  /**
   * Check if item is saved for later.
   */
  public boolean isSavedForLater() {
    return this.status == ItemStatus.SAVED_FOR_LATER;
  }
}
