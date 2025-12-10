/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.marketing.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import lombok.*;

/**
 * Marketing campaign.
 */
@Entity
@Table(name = "campaigns", indexes = {
    @Index(name = "idx_campaign_status", columnList = "status"),
    @Index(name = "idx_campaign_dates", columnList = "start_date, end_date")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Campaign {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "campaign_id")
  private Long campaignId;

  @Column(name = "name", nullable = false, length = 255)
  private String name;

  @Column(name = "code", nullable = false, unique = true, length = 50)
  private String code;

  @Column(name = "description", columnDefinition = "TEXT")
  private String description;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 30)
  private CampaignType type;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  @Builder.Default
  private CampaignStatus status = CampaignStatus.DRAFT;

  // Discount configuration
  @Enumerated(EnumType.STRING)
  @Column(name = "discount_type", length = 20)
  private DiscountType discountType;

  @Column(name = "discount_value", precision = 15, scale = 2)
  private BigDecimal discountValue;

  @Column(name = "max_discount", precision = 15, scale = 2)
  private BigDecimal maxDiscount;

  @Column(name = "min_purchase", precision = 15, scale = 2)
  private BigDecimal minPurchase;

  // Usage limits
  @Column(name = "total_uses_limit")
  private Integer totalUsesLimit;

  @Column(name = "per_customer_limit")
  @Builder.Default
  private Integer perCustomerLimit = 1;

  @Column(name = "current_uses")
  @Builder.Default
  private Integer currentUses = 0;

  // Dates
  @Column(name = "start_date", nullable = false)
  private Instant startDate;

  @Column(name = "end_date", nullable = false)
  private Instant endDate;

  // Targeting
  @Column(name = "target_customer_segments", columnDefinition = "TEXT")
  private String targetCustomerSegments;

  @Column(name = "target_categories", columnDefinition = "TEXT")
  private String targetCategories;

  @Column(name = "target_products", columnDefinition = "TEXT")
  private String targetProducts;

  @Column(name = "excluded_products", columnDefinition = "TEXT")
  private String excludedProducts;

  @Column(name = "target_brands", columnDefinition = "TEXT")
  private String targetBrands;

  // Display
  @Column(name = "banner_image_url", length = 500)
  private String bannerImageUrl;

  @Column(name = "thumbnail_url", length = 500)
  private String thumbnailUrl;

  @Column(name = "display_on_homepage")
  @Builder.Default
  private Boolean displayOnHomepage = false;

  @Column(name = "display_priority")
  @Builder.Default
  private Integer displayPriority = 0;

  // Stacking rules
  @Column(name = "stackable")
  @Builder.Default
  private Boolean stackable = false;

  @Column(name = "exclusive")
  @Builder.Default
  private Boolean exclusive = false;

  // Tracking
  @Column(name = "created_by")
  private Long createdBy;

  @Column(name = "created_at")
  @Builder.Default
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at")
  @Builder.Default
  private Instant updatedAt = Instant.now();

  @OneToMany(mappedBy = "campaign", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @Builder.Default
  private List<CampaignProduct> products = new ArrayList<>();

  public enum CampaignType {
    /** Percentage discount */
    PERCENTAGE_DISCOUNT,
    /** Fixed amount discount */
    FIXED_DISCOUNT,
    /** Buy X get Y free */
    BUY_X_GET_Y,
    /** Bundle deal */
    BUNDLE,
    /** Free shipping */
    FREE_SHIPPING,
    /** Flash sale */
    FLASH_SALE,
    /** Seasonal sale */
    SEASONAL,
    /** Clearance */
    CLEARANCE,
    /** Loyalty reward */
    LOYALTY,
    /** First purchase */
    FIRST_PURCHASE
  }

  public enum CampaignStatus {
    DRAFT,
    SCHEDULED,
    ACTIVE,
    PAUSED,
    ENDED,
    CANCELLED
  }

  public enum DiscountType {
    PERCENTAGE,
    FIXED_AMOUNT,
    FREE_ITEM,
    FREE_SHIPPING
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = Instant.now();
  }

  /**
   * Check if campaign is currently active.
   */
  public boolean isActive() {
    if (status != CampaignStatus.ACTIVE) {
      return false;
    }
    Instant now = Instant.now();
    return now.isAfter(startDate) && now.isBefore(endDate);
  }

  /**
   * Check if campaign has available uses.
   */
  public boolean hasAvailableUses() {
    if (totalUsesLimit == null) {
      return true;
    }
    return currentUses < totalUsesLimit;
  }

  /**
   * Check if customer can use campaign.
   */
  public boolean canCustomerUse(int customerUseCount) {
    if (perCustomerLimit == null) {
      return true;
    }
    return customerUseCount < perCustomerLimit;
  }

  /**
   * Record campaign use.
   */
  public void recordUse() {
    this.currentUses++;
  }

  /**
   * Calculate discount for a given subtotal.
   */
  public BigDecimal calculateDiscount(BigDecimal subtotal) {
    if (minPurchase != null && subtotal.compareTo(minPurchase) < 0) {
      return BigDecimal.ZERO;
    }

    BigDecimal discount;
    if (discountType == DiscountType.PERCENTAGE) {
      discount = subtotal.multiply(discountValue).divide(BigDecimal.valueOf(100));
    } else if (discountType == DiscountType.FIXED_AMOUNT) {
      discount = discountValue;
    } else {
      return BigDecimal.ZERO;
    }

    if (maxDiscount != null && discount.compareTo(maxDiscount) > 0) {
      discount = maxDiscount;
    }

    return discount;
  }

  /**
   * Activate the campaign.
   */
  public void activate() {
    this.status = CampaignStatus.ACTIVE;
  }

  /**
   * Pause the campaign.
   */
  public void pause() {
    this.status = CampaignStatus.PAUSED;
  }

  /**
   * End the campaign.
   */
  public void end() {
    this.status = CampaignStatus.ENDED;
  }
}
