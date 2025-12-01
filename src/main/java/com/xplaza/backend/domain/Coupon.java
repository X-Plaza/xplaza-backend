/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.domain;

import java.util.Date;
import java.util.List;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Coupon {
  private Long couponId;
  private String couponCode;
  private Double amount;
  private Double maxAmount;
  private Long currencyId;
  private Long discountTypeId;
  private Date startDate;
  private Date endDate;
  private Boolean isActive;
  private Double minShoppingAmount;
  private List<CouponShopLink> couponShopLinks;
}
