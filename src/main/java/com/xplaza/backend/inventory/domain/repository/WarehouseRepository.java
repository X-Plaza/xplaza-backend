/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.inventory.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.xplaza.backend.inventory.domain.entity.Warehouse;

/**
 * Repository for Warehouse entity.
 */
@Repository
public interface WarehouseRepository extends JpaRepository<Warehouse, Long> {

  Optional<Warehouse> findByCode(String code);

  List<Warehouse> findByIsActiveTrue();

  @Query("SELECT w FROM Warehouse w WHERE w.isActive = true AND w.countryCode = :countryCode ORDER BY w.priority DESC")
  List<Warehouse> findActiveByCountry(@Param("countryCode") String countryCode);

  @Query("SELECT w FROM Warehouse w WHERE w.isActive = true AND w.acceptsReturns = true")
  List<Warehouse> findReturnsAcceptingWarehouses();

  @Query("SELECT w FROM Warehouse w WHERE w.isActive = true AND w.type = :type")
  List<Warehouse> findByType(@Param("type") Warehouse.WarehouseType type);

  @Query("SELECT w FROM Warehouse w WHERE w.isActive = true ORDER BY w.priority DESC, w.warehouseId ASC")
  List<Warehouse> findByPriority();

  boolean existsByCode(String code);
}
