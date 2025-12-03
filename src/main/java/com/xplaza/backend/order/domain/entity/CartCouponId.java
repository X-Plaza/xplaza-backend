/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.order.domain.entity;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

import lombok.*;

/**
 * Composite primary key for CartCoupon.
 */
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CartCouponId implements Serializable {

  private static final long serialVersionUID = 1L;

  private UUID cartId;
  private Long couponId;

  @Override
  public boolean equals(Object o) {
    if (this == o)
      return true;
    if (o == null || getClass() != o.getClass())
      return false;
    CartCouponId that = (CartCouponId) o;
    return Objects.equals(cartId, that.cartId) &&
        Objects.equals(couponId, that.couponId);
  }

  @Override
  public int hashCode() {
    return Objects.hash(cartId, couponId);
  }
}
