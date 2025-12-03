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
 * Coupon applied to a cart.
 */
@Entity
@Table(name = "cart_coupons")
@IdClass(CartCouponId.class)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CartCoupon {

  @Id
  @Column(name = "cart_id")
  private UUID cartId;

  @Id
  @Column(name = "coupon_id")
  private Long couponId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "cart_id", insertable = false, updatable = false)
  private Cart cart;

  @Column(name = "code", nullable = false, length = 50)
  private String code;

  @Column(name = "discount_amount", precision = 15, scale = 2)
  @Builder.Default
  private BigDecimal discountAmount = BigDecimal.ZERO;

  @Column(name = "applied_at")
  @Builder.Default
  private Instant appliedAt = Instant.now();
}
