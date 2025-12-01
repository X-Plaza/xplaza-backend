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
public class Product {
  private Long productId;
  private String productName;
  private String productDescription;
  private Brand brand;
  private Category category;
  private ProductVariationType productVariationType;
  private Integer productVarTypeValue;
  private Double productBuyingPrice;
  private Double productSellingPrice;
  private Double productDiscountedPrice;
  private Currency currency;
  private Shop shop;
  private Integer quantity;
  private Integer createdBy;
  private Date createdAt;
  private Integer lastUpdatedBy;
  private Date lastUpdatedAt;
  private List<ProductImage> images;
}
