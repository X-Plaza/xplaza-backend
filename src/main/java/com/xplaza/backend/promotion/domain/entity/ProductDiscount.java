/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.promotion.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;

import com.xplaza.backend.catalog.domain.entity.Product;

@Table(name = "product_discounts")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDiscount {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long productDiscountId;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "fk_product_id", nullable = false)
  private Product product;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "fk_discount_type_id")
  private DiscountType discountType;

  private Double discountValue;

  private LocalDateTime startDate;

  private LocalDateTime endDate;

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
