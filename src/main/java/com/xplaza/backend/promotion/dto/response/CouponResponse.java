/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.promotion.dto.response;

import java.util.Date;
import java.util.List;

import lombok.Data;

@Data
public class CouponResponse {
  private Long id;
  private Double amount;
  private Long currencyId;
  private Date startDate;
  private Date endDate;
  private Double maxAmount;
  private Long discountTypeId;
  private List<Long> shopIds;
  // add other fields as needed
}
