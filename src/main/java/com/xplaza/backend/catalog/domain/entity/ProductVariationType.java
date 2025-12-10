/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.catalog.domain.entity;

import jakarta.persistence.*;

import lombok.*;

import org.hibernate.annotations.Immutable;

@Entity
@Table(name = "product_variation_types")
@Immutable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductVariationType {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long productVarTypeId;

  private String varTypeName;

  private String varTypeDescription;
}
