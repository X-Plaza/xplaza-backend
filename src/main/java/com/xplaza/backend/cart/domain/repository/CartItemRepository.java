/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.cart.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.xplaza.backend.cart.domain.entity.CartItem;
import com.xplaza.backend.cart.domain.entity.CartItem.ItemStatus;

/**
 * Repository for CartItem entities.
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

  /**
   * Find items by cart ID.
   */
  List<CartItem> findByCartId(UUID cartId);

  /**
   * Find active items by cart ID.
   */
  List<CartItem> findByCartIdAndStatus(UUID cartId, ItemStatus status);

  /**
   * Find item by cart and product.
   */
  @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.productId = :productId AND ci.variantId = :variantId")
  CartItem findByCartIdAndProductAndVariant(@Param("cartId") UUID cartId, @Param("productId") Long productId,
      @Param("variantId") UUID variantId);

  /**
   * Find item by cart and product (without variant).
   */
  @Query("SELECT ci FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.productId = :productId AND ci.variantId IS NULL")
  CartItem findByCartIdAndProductWithoutVariant(@Param("cartId") UUID cartId, @Param("productId") Long productId);

  /**
   * Count items in cart.
   */
  @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.status = 'ACTIVE'")
  long countActiveItemsInCart(@Param("cartId") UUID cartId);

  /**
   * Get total quantity of items in cart.
   */
  @Query("SELECT COALESCE(SUM(ci.quantity), 0) FROM CartItem ci WHERE ci.cart.id = :cartId AND ci.status = 'ACTIVE'")
  int getTotalQuantityInCart(@Param("cartId") UUID cartId);

  /**
   * Delete items by cart ID.
   */
  @Modifying
  @Query("DELETE FROM CartItem ci WHERE ci.cart.id = :cartId")
  void deleteByCartId(@Param("cartId") UUID cartId);

  /**
   * Update item status.
   */
  @Modifying
  @Query("UPDATE CartItem ci SET ci.status = :status WHERE ci.id = :itemId")
  void updateStatus(@Param("itemId") UUID itemId, @Param("status") ItemStatus status);

  /**
   * Find items with out-of-stock products.
   */
  default List<CartItem> findOutOfStockItems(UUID cartId) {
    return findByCartIdAndStatus(cartId, CartItem.ItemStatus.OUT_OF_STOCK);
  }

  /**
   * Find items by product ID (for inventory updates).
   */
  List<CartItem> findByProductId(Long productId);

  /**
   * Find items by variant ID (for inventory updates).
   */
  List<CartItem> findByVariantId(UUID variantId);
}
