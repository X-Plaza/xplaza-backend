/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.catalog.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.xplaza.backend.catalog.domain.entity.Attribute;

/**
 * Repository for Attribute entity.
 */
@Repository
public interface AttributeRepository extends JpaRepository<Attribute, Long> {

  Optional<Attribute> findByCode(String code);

  List<Attribute> findByIsActiveTrue();

  List<Attribute> findByIsFilterableTrue();

  @Query("SELECT a FROM Attribute a WHERE a.categoryId = :categoryId AND a.isActive = true")
  List<Attribute> findByCategoryId(@Param("categoryId") Long categoryId);

  @Query("SELECT a FROM Attribute a WHERE a.categoryId IS NULL AND a.isActive = true")
  List<Attribute> findGlobalAttributes();

  @Query("SELECT DISTINCT a FROM Attribute a " +
      "JOIN a.values v " +
      "JOIN VariantAttribute va ON va.attributeValue.valueId = v.valueId " +
      "JOIN ProductVariant pv ON pv.variantId = va.variantId " +
      "WHERE pv.productId IN :productIds")
  List<Attribute> findAttributesByProductIds(@Param("productIds") List<Long> productIds);

  boolean existsByCode(String code);

  @Query("SELECT a FROM Attribute a WHERE a.type = :type AND a.isActive = true")
  List<Attribute> findByType(@Param("type") Attribute.AttributeType type);
}
