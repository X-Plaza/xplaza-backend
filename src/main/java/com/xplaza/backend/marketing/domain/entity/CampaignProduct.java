/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.marketing.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.*;

import lombok.*;

/**
 * Product included in a campaign with specific discount.
 */
@Entity
@Table(name = "campaign_products", indexes = {
    @Index(name = "idx_campaign_product_campaign", columnList = "campaign_id"),
    @Index(name = "idx_campaign_product_product", columnList = "product_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CampaignProduct {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "campaign_product_id")
  private Long campaignProductId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "campaign_id", nullable = false)
  private Campaign campaign;

  @Column(name = "product_id", nullable = false)
  private Long productId;

  // Override campaign discount for this product
  @Enumerated(EnumType.STRING)
  @Column(name = "discount_type", length = 20)
  private Campaign.DiscountType discountType;

  @Column(name = "discount_value", precision = 15, scale = 2)
  private BigDecimal discountValue;

  // Final sale price (optional - for flash sales)
  @Column(name = "sale_price", precision = 15, scale = 2)
  private BigDecimal salePrice;

  @Column(name = "max_quantity_per_order")
  private Integer maxQuantityPerOrder;

  @Column(name = "available_quantity")
  private Integer availableQuantity;

  @Column(name = "sold_quantity")
  @Builder.Default
  private Integer soldQuantity = 0;

  @Column(name = "is_featured")
  @Builder.Default
  private Boolean isFeatured = false;

  @Column(name = "display_order")
  @Builder.Default
  private Integer displayOrder = 0;

  @Column(name = "created_at")
  @Builder.Default
  private Instant createdAt = Instant.now();

  /**
   * Check if product has available quantity.
   */
  public boolean hasAvailableQuantity(int requestedQuantity) {
    if (availableQuantity == null) {
      return true;
    }
    return (availableQuantity - soldQuantity) >= requestedQuantity;
  }

  /**
   * Record sale.
   */
  public void recordSale(int quantity) {
    this.soldQuantity += quantity;
  }

  /**
   * Check if within max per order limit.
   */
  public boolean withinMaxPerOrder(int quantity) {
    if (maxQuantityPerOrder == null) {
      return true;
    }
    return quantity <= maxQuantityPerOrder;
  }
}
