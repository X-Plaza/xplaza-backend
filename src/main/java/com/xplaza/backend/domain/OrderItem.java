/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.domain;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {
  private Long orderItemId;
  private Long orderId;
  private Long productId;
  private Double productSellingPrice;
  private Double productBuyingPrice;
  private String itemName;
  private String itemVarTypeName;
  private Long itemVarTypeValue;
  private String itemCategory;
  private Long quantity;
  private String quantityType;
  private Double unitPrice;
  private Double itemTotalPrice;
  private String itemImage;
  private Long currencyId;
  private OrderPlace orderPlace;
}
