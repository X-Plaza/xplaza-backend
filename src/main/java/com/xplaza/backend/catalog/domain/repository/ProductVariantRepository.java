/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.catalog.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.xplaza.backend.catalog.domain.entity.ProductVariant;
import com.xplaza.backend.catalog.domain.entity.ProductVariant.VariantStatus;

/**
 * Repository for ProductVariant entity.
 */
@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, UUID> {

  List<ProductVariant> findByProductIdAndStatus(Long productId, VariantStatus status);

  List<ProductVariant> findByProductId(Long productId);

  Optional<ProductVariant> findBySku(String sku);

  Optional<ProductVariant> findByBarcode(String barcode);

  @Query("SELECT pv FROM ProductVariant pv WHERE pv.productId = :productId AND pv.status = 'ACTIVE'")
  List<ProductVariant> findActiveByProductId(@Param("productId") Long productId);

  @Query("SELECT pv FROM ProductVariant pv WHERE pv.productId IN :productIds AND pv.status = 'ACTIVE'")
  List<ProductVariant> findByProductIds(@Param("productIds") List<Long> productIds);

  boolean existsBySku(String sku);

  @Query("SELECT COUNT(pv) FROM ProductVariant pv WHERE pv.productId = :productId AND pv.status = 'ACTIVE'")
  long countActiveByProductId(@Param("productId") Long productId);
}
