/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.catalog.dto.response;

import lombok.Data;

@Data
public class ProductListResponse {
  private Long id;
  private String name;
  private String description;
  private Long brandId;
  private String brandName;
  private Long categoryId;
  private String categoryName;
  private Long productVarTypeId;
  private String productVarTypeName;
  private Double productVarTypeValue;
  private Double buyingPrice;
  private Double sellingPrice;
  private Double discountedPrice;
  // add other fields as needed
}
