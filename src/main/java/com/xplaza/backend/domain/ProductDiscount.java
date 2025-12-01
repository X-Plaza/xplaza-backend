/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.domain;

import java.util.Date;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDiscount {
  private Long productDiscountId;
  private DiscountType discountType;
  private Product product;
  private Currency currency;
  private Double discountAmount;
  private Date validFrom;
  private Date validTo;
}
