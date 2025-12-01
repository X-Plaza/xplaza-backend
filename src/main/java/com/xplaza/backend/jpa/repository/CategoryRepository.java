/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.jpa.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.xplaza.backend.jpa.dao.CategoryDao;

public interface CategoryRepository extends JpaRepository<CategoryDao, Long> {

  // JPQL query - more portable than native SQL
  @Query("SELECT c.categoryName FROM CategoryDao c WHERE c.categoryId = :id")
  String getName(@Param("id") Long id);

  // Use Spring Data derived query
  boolean existsByCategoryName(String categoryName);

  // Legacy method for backward compatibility
  default boolean existsByName(String name) {
    return existsByCategoryName(name);
  }

  // JPQL to check for child categories
  @Query("SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM CategoryDao c WHERE c.parentCategory.categoryId = :id")
  boolean hasChildCategory(@Param("id") Long id);

  // V2: Search by name (case-insensitive)
  Page<CategoryDao> findByCategoryNameContainingIgnoreCase(String categoryName, Pageable pageable);

  // V2: Find by parent category
  Page<CategoryDao> findByParentCategoryCategoryId(Long parentId, Pageable pageable);
}
