/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.catalog.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductRequest {
  private Long productId;
  @NotBlank(message = "Product name is required")
  private String productName;
  private String productDescription;
  @NotNull(message = "Product price is required")
  @Positive(message = "Product price must be positive")
  private Double productPrice;
  @NotNull(message = "Quantity is required")
  @Min(value = 0, message = "Quantity must be zero or positive")
  private Integer quantity;
  private Boolean isTrending;
  @NotNull(message = "Shop ID is required")
  private Long shopId;
  @NotNull(message = "Category ID is required")
  private Long categoryId;
  @NotNull(message = "Brand ID is required")
  private Long brandId;
}
