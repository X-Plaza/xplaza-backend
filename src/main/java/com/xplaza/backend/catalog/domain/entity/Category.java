/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.catalog.domain.entity;

import jakarta.persistence.*;

import lombok.*;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Category {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long categoryId;

  private String categoryName;

  private String categoryDescription;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "parent_category")
  private Category parentCategory;

  // Omitted products list for now to avoid circular dependency with unmigrated
  // Product entity
}
