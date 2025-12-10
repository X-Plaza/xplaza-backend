/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.promotion.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;

@Table(name = "coupons")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long couponId;

  @Column(nullable = false, unique = true)
  private String couponCode;

  private String couponDescription;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "fk_discount_type_id")
  private DiscountType discountType;

  private Double discountValue;

  private Double minimumOrderAmount;

  private Double maximumDiscountAmount;

  private LocalDateTime startDate;

  private LocalDateTime endDate;

  @Builder.Default
  private Integer usageLimit = 0;

  @Builder.Default
  private Integer usedCount = 0;

  @Builder.Default
  private Boolean isActive = true;

  private Integer createdBy;
  private LocalDateTime createdAt;
  private Integer lastUpdatedBy;
  private LocalDateTime lastUpdatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    lastUpdatedAt = LocalDateTime.now();
  }
}
