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
 * An item within a customer order.
 *
 * This is a snapshot of the product/variant at the time of purchase. All
 * product details are copied so that the order remains accurate even if the
 * product changes later.
 */
@Entity
@Table(name = "customer_order_items", indexes = {
    @Index(name = "idx_cust_order_items_order", columnList = "order_id"),
    @Index(name = "idx_cust_order_items_product", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "order")
public class CustomerOrderItem {

  @Id
  @Column(name = "order_item_id")
  @Builder.Default
  private UUID orderItemId = UUID.randomUUID();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  private CustomerOrder order;

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Column(name = "variant_id")
  private UUID variantId;

  @Column(name = "shop_id", nullable = false)
  private Long shopId;

  // Snapshot of product details at time of order
  @Column(name = "product_name", nullable = false, length = 255)
  private String productName;

  @Column(name = "variant_name", length = 255)
  private String variantName;

  @Column(name = "sku", length = 100)
  private String sku;

  @Column(name = "product_image_url", length = 500)
  private String productImageUrl;

  @Column(name = "category_name", length = 255)
  private String categoryName;

  // Quantity and pricing
  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @Column(name = "unit_price", nullable = false, precision = 15, scale = 2)
  private BigDecimal unitPrice;

  @Column(name = "cost_price", precision = 15, scale = 2)
  private BigDecimal costPrice;

  @Column(name = "discount_amount", precision = 15, scale = 2)
  @Builder.Default
  private BigDecimal discountAmount = BigDecimal.ZERO;

  @Column(name = "tax_amount", precision = 15, scale = 2)
  @Builder.Default
  private BigDecimal taxAmount = BigDecimal.ZERO;

  @Column(name = "total_price", nullable = false, precision = 15, scale = 2)
  private BigDecimal totalPrice;

  // Item-level status (for partial fulfillment)
  @Enumerated(EnumType.STRING)
  @Column(name = "status", length = 30)
  @Builder.Default
  private ItemStatus status = ItemStatus.PENDING;

  @Column(name = "quantity_shipped")
  @Builder.Default
  private Integer quantityShipped = 0;

  @Column(name = "quantity_returned")
  @Builder.Default
  private Integer quantityReturned = 0;

  @Column(name = "quantity_refunded")
  @Builder.Default
  private Integer quantityRefunded = 0;

  // Notes
  @Column(name = "notes", length = 500)
  private String notes;

  @Column(name = "created_at")
  @Builder.Default
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at")
  @Builder.Default
  private Instant updatedAt = Instant.now();

  public enum ItemStatus {
    PENDING,
    PROCESSING,
    SHIPPED,
    DELIVERED,
    CANCELLED,
    RETURNED,
    PARTIALLY_RETURNED
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = Instant.now();
  }

  @PrePersist
  protected void calculateTotal() {
    if (unitPrice != null && quantity != null) {
      BigDecimal gross = unitPrice.multiply(BigDecimal.valueOf(quantity));
      BigDecimal discount = discountAmount != null ? discountAmount : BigDecimal.ZERO;
      BigDecimal tax = taxAmount != null ? taxAmount : BigDecimal.ZERO;
      this.totalPrice = gross.subtract(discount).add(tax);
    }
  }

  public UUID getOrderId() {
    return order != null ? order.getOrderId() : null;
  }

  /**
   * Get quantity still pending shipment.
   */
  public int getQuantityPending() {
    return quantity - quantityShipped;
  }

  /**
   * Check if item is fully shipped.
   */
  public boolean isFullyShipped() {
    return quantityShipped >= quantity;
  }

  /**
   * Check if item is fully returned.
   */
  public boolean isFullyReturned() {
    return quantityReturned >= quantity;
  }

  /**
   * Calculate profit margin for this item.
   */
  public BigDecimal getProfitMargin() {
    if (costPrice == null || costPrice.compareTo(BigDecimal.ZERO) == 0) {
      return null;
    }
    BigDecimal profit = unitPrice.subtract(costPrice);
    return profit.divide(costPrice, 4, java.math.RoundingMode.HALF_UP)
        .multiply(BigDecimal.valueOf(100));
  }
}
