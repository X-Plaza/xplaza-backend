/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.jpa.repository;

import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.xplaza.backend.jpa.dao.BrandDao;

public interface BrandRepository extends JpaRepository<BrandDao, Long> {

  // JPQL query - more portable than native SQL
  @Query("SELECT b.brandName FROM BrandDao b WHERE b.brandId = :id")
  String getName(@Param("id") Long id);

  // Use Spring Data derived query instead of native
  Optional<BrandDao> findByBrandId(Long brandId);

  // Check existence by name
  boolean existsByBrandName(String brandName);

  // Legacy method for backward compatibility
  default boolean existsByName(String name) {
    return existsByBrandName(name);
  }

  // Search by name (case-insensitive)
  Page<BrandDao> findByBrandNameContainingIgnoreCase(String brandName, Pageable pageable);
}
