/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.catalog.dto.response;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductResponse {
  private Long productId;
  private String productName;
  private String productDescription;
  private Double productPrice;
  private Integer quantity;
  private Boolean isTrending;
  private Long shopId;
  private String shopName;
  private Long categoryId;
  private String categoryName;
  private Long brandId;
  private String brandName;
  private List<ProductImageResponse> images;
}
