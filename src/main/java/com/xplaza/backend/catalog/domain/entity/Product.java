/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.catalog.domain.entity;

import java.util.*;

import jakarta.persistence.*;

import lombok.*;

import com.xplaza.backend.shop.domain.entity.Shop;

@Table(name = "products")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Product {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long productId;

  private String productName;

  private String productDescription;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "fk_brand_id")
  private Brand brand;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "fk_category_id")
  private Category category;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "fk_product_var_type_id")
  private ProductVariationType productVariationType;

  private Integer productVarTypeValue;

  private Double productBuyingPrice;

  private Double productSellingPrice;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "fk_currency_id")
  private Currency currency;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "fk_shop_id")
  private Shop shop;

  private Integer quantity;

  private Integer createdBy;

  private Date createdAt;

  private Integer lastUpdatedBy;

  private Date lastUpdatedAt;

  @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
  private List<ProductImage> images;
}
