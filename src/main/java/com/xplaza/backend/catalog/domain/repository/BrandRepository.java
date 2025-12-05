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

import com.xplaza.backend.catalog.domain.entity.Brand;

public interface BrandRepository extends JpaRepository<Brand, Long> {

  @Query("SELECT b.brandName FROM Brand b WHERE b.brandId = :id")
  String getName(@Param("id") Long id);

  boolean existsByBrandName(String brandName);

  Page<Brand> findByBrandNameContainingIgnoreCase(String brandName, Pageable pageable);
}
