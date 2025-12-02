/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.order.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

import lombok.*;

/**
 * An item in a shopping cart.
 */
@Entity
@Table(name = "cart_items", uniqueConstraints = {
    @UniqueConstraint(columnNames = { "cart_id", "variant_id" })
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartItem {

  @Id
  @Column(name = "cart_item_id")
  @Builder.Default
  private UUID cartItemId = UUID.randomUUID();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cart_id", nullable = false)
  private Cart cart;

  @Column(name = "product_id", nullable = false)
  private Long productId;

  /**
   * The specific variant being purchased.
   */
  @Column(name = "variant_id")
  private UUID variantId;

  @Column(name = "shop_id", nullable = false)
  private Long shopId;

  @Column(name = "quantity", nullable = false)
  @Builder.Default
  private Integer quantity = 1;

  /**
   * Current price per unit.
   */
  @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
  private BigDecimal unitPrice;

  /**
   * Price when item was added (for detecting price changes).
   */
  @Column(name = "price_at_add", nullable = false, precision = 15, scale = 2)
  private BigDecimal priceAtAdd;

  /**
   * Discount applied to this item.
   */
  @Column(name = "discount_amount", precision = 15, scale = 2)
  @Builder.Default
  private BigDecimal discountAmount = BigDecimal.ZERO;

  /**
   * Total price for this line item (unitPrice * quantity - discountAmount).
   */
  @Column(name = "total_price", nullable = false, precision = 15, scale = 2)
  private BigDecimal totalPrice;

  /**
   * Whether this item is saved for later purchase.
   */
  @Column(name = "saved_for_later")
  @Builder.Default
  private Boolean savedForLater = false;

  @Column(name = "added_at")
  @Builder.Default
  private Instant addedAt = Instant.now();

  @Column(name = "updated_at")
  @Builder.Default
  private Instant updatedAt = Instant.now();

  @PrePersist
  @PreUpdate
  protected void calculateTotalPrice() {
    this.updatedAt = Instant.now();
    if (unitPrice != null && quantity != null) {
      BigDecimal gross = unitPrice.multiply(BigDecimal.valueOf(quantity));
      BigDecimal discount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
      this.totalPrice = gross.subtract(discount);
      if (this.totalPrice.compareTo(BigDecimal.ZERO) < 0) {
        this.totalPrice = BigDecimal.ZERO;
      }
    }
  }

  public UUID getCartId() {
    return cart != null ? cart.getCartId() : null;
  }

  /**
   * Check if the price has changed since the item was added.
   */
  public boolean hasPriceChanged() {
    if (priceAtAdd == null || unitPrice == null) {
      return false;
    }
    return priceAtAdd.compareTo(unitPrice) != 0;
  }

  /**
   * Get the price difference (positive if price increased, negative if
   * decreased).
   */
  public BigDecimal getPriceDifference() {
    if (priceAtAdd == null || unitPrice == null) {
      return BigDecimal.ZERO;
    }
    return unitPrice.subtract(priceAtAdd);
  }

  /**
   * Update quantity and recalculate total.
   */
  public void updateQuantity(int newQuantity) {
    this.quantity = newQuantity;
    calculateTotalPrice();
  }

  /**
   * Move item to saved for later.
   */
  public void saveForLater() {
    this.savedForLater = true;
  }

  /**
   * Move item back to cart.
   */
  public void moveToCart() {
    this.savedForLater = false;
  }
}
