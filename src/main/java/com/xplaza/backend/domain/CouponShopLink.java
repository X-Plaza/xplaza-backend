/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.domain;

import lombok.*;

import com.xplaza.backend.jpa.dao.CouponDao;
import com.xplaza.backend.jpa.dao.ShopDao;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CouponShopLink {
  ShopDao shop;
  CouponDao coupon;
}