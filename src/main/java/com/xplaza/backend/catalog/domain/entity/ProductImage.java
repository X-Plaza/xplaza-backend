/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.catalog.domain.entity;

import java.util.*;

import jakarta.persistence.*;

import lombok.*;

@Table(name = "product_images")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long productImagesId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "fk_product_id")
  private Product product;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "fk_product_variant_id")
  private ProductVariant productVariant;

  private String productImageName;

  private String productImagePath;

  private Integer createdBy;

  private Date createdAt;

  private Integer lastUpdatedBy;

  private Date lastUpdatedAt;
}
