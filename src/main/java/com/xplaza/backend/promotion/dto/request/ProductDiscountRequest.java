/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.promotion.dto.request;

import java.util.Date;

import lombok.Data;

@Data
public class ProductDiscountRequest {
  private Long productId;
  private Long discountTypeId;
  private Double discountAmount;
  private Long currencyId;
  private Date startDate;
  private Date endDate;
}
