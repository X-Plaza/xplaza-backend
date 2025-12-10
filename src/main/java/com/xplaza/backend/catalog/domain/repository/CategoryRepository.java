/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.catalog.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.xplaza.backend.catalog.domain.entity.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

  @Query("SELECT c.categoryName FROM Category c WHERE c.categoryId = :id")
  String getName(@Param("id") Long id);

  boolean existsByCategoryName(String categoryName);

  // Legacy support
  default boolean existsByName(String name) {
    return existsByCategoryName(name);
  }

  @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c WHERE c.parentCategory.categoryId = :id")
  boolean hasChildCategory(@Param("id") Long id);

  Page<Category> findByCategoryNameContainingIgnoreCase(String categoryName, Pageable pageable);

  Page<Category> findByParentCategoryCategoryId(Long parentId, Pageable pageable);
}
