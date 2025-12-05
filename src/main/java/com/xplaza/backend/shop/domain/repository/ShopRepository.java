/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.shop.domain.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.xplaza.backend.shop.domain.entity.Shop;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {
  Page<Shop> findByShopNameContainingIgnoreCase(String shopName, Pageable pageable);

  Page<Shop> findByLocationId(Long locationId, Pageable pageable);

  @Query(value = "SELECT * FROM shops WHERE shop_owner = :ownerId", countQuery = "SELECT COUNT(*) FROM shops WHERE shop_owner = :ownerId", nativeQuery = true)
  Page<Shop> findByShopOwnerIdPaginated(@Param("ownerId") Long ownerId, Pageable pageable);
}
