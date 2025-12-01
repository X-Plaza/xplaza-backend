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
public class ProductImage {
  private Long productImageId;
  private Product product;
  private String productImageName;
  private String productImagePath;
  private Integer createdBy;
  private Date createdAt;
  private Integer lastUpdatedBy;
  private Date lastUpdatedAt;
}
