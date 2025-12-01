/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.jpa.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.xplaza.backend.jpa.dao.ShopDao;

public interface ShopRepository extends JpaRepository<ShopDao, Long> {

  // JPQL query - more portable than native SQL
  @Query("SELECT s.shopName FROM ShopDao s WHERE s.shopId = :id")
  String getName(@Param("id") Long id);

  // Use Spring Data derived queries for location
  List<ShopDao> findByLocationLocationId(Long locationId);

  // Paginated version
  Page<ShopDao> findByLocationLocationId(Long locationId, Pageable pageable);

  // For shop owner - use native query since shop_owner is stored as Long in DB
  // but mapped as String
  @Query(value = "SELECT * FROM shops WHERE shop_owner = :ownerId", nativeQuery = true)
  List<ShopDao> findByShopOwnerId(@Param("ownerId") Long ownerId);

  // Paginated version for owner
  @Query(value = "SELECT * FROM shops WHERE shop_owner = :ownerId", countQuery = "SELECT COUNT(*) FROM shops WHERE shop_owner = :ownerId", nativeQuery = true)
  Page<ShopDao> findByShopOwnerIdPaginated(@Param("ownerId") Long ownerId, Pageable pageable);

  // Search by name (case-insensitive)
  Page<ShopDao> findByShopNameContainingIgnoreCase(String shopName, Pageable pageable);

  // Legacy methods for backward compatibility
  default List<ShopDao> findByLocationId(Long locationId) {
    return findByLocationLocationId(locationId);
  }

  default List<ShopDao> findByShopOwner(Long ownerId) {
    return findByShopOwnerId(ownerId);
  }
}
