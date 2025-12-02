/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.inventory.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.xplaza.backend.inventory.domain.entity.InventoryItem;

/**
 * Repository for InventoryItem entity.
 */
@Repository
public interface InventoryItemRepository extends JpaRepository<InventoryItem, UUID> {

  List<InventoryItem> findByProductId(Long productId);

  List<InventoryItem> findByVariantId(UUID variantId);

  Optional<InventoryItem> findBySku(String sku);

  @Query("SELECT ii FROM InventoryItem ii WHERE ii.productId = :productId AND ii.warehouse.warehouseId = :warehouseId")
  Optional<InventoryItem> findByProductIdAndWarehouseId(
      @Param("productId") Long productId,
      @Param("warehouseId") Long warehouseId);

  @Query("SELECT ii FROM InventoryItem ii WHERE ii.variantId = :variantId AND ii.warehouse.warehouseId = :warehouseId")
  Optional<InventoryItem> findByVariantIdAndWarehouseId(
      @Param("variantId") UUID variantId,
      @Param("warehouseId") Long warehouseId);

  @Query("SELECT ii FROM InventoryItem ii WHERE ii.warehouse.warehouseId = :warehouseId AND ii.status = 'ACTIVE'")
  List<InventoryItem> findByWarehouseId(@Param("warehouseId") Long warehouseId);

  @Query("SELECT ii FROM InventoryItem ii WHERE (ii.quantityOnHand - ii.quantityReserved) <= ii.reorderPoint AND ii.status = 'ACTIVE'")
  List<InventoryItem> findItemsNeedingReorder();

  @Query("SELECT ii FROM InventoryItem ii WHERE (ii.quantityOnHand - ii.quantityReserved) <= ii.safetyStock AND ii.status = 'ACTIVE'")
  List<InventoryItem> findItemsBelowSafetyStock();

  @Query("SELECT ii FROM InventoryItem ii WHERE ii.quantityOnHand - ii.quantityReserved > 0 AND ii.productId = :productId AND ii.status = 'ACTIVE'")
  List<InventoryItem> findAvailableInventoryByProductId(@Param("productId") Long productId);

  @Query("SELECT SUM(ii.quantityOnHand - ii.quantityReserved) FROM InventoryItem ii WHERE ii.productId = :productId AND ii.status = 'ACTIVE'")
  Integer sumAvailableQuantityByProductId(@Param("productId") Long productId);

  @Query("SELECT SUM(ii.quantityOnHand - ii.quantityReserved) FROM InventoryItem ii WHERE ii.variantId = :variantId AND ii.status = 'ACTIVE'")
  Integer sumAvailableQuantityByVariantId(@Param("variantId") UUID variantId);
}
