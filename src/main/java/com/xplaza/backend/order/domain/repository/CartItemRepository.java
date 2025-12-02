/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.order.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.xplaza.backend.order.domain.entity.CartItem;

/**
 * Repository for CartItem entity.
 */
@Repository
public interface CartItemRepository extends JpaRepository<CartItem, UUID> {

  List<CartItem> findByCartCartId(UUID cartId);

  @Query("SELECT ci FROM CartItem ci WHERE ci.cart.cartId = :cartId AND ci.productId = :productId")
  Optional<CartItem> findByCartIdAndProductId(@Param("cartId") UUID cartId, @Param("productId") Long productId);

  @Query("SELECT ci FROM CartItem ci WHERE ci.cart.cartId = :cartId AND ci.productId = :productId AND ci.variantId = :variantId")
  Optional<CartItem> findByCartIdAndProductIdAndVariantId(
      @Param("cartId") UUID cartId,
      @Param("productId") Long productId,
      @Param("variantId") UUID variantId);

  @Modifying
  @Query("DELETE FROM CartItem ci WHERE ci.cart.cartId = :cartId")
  void deleteByCartId(@Param("cartId") UUID cartId);

  @Query("SELECT COUNT(ci) FROM CartItem ci WHERE ci.cart.cartId = :cartId")
  long countByCartId(@Param("cartId") UUID cartId);

  @Query("SELECT SUM(ci.quantity) FROM CartItem ci WHERE ci.cart.cartId = :cartId")
  Integer sumQuantityByCartId(@Param("cartId") UUID cartId);

  @Query("SELECT ci FROM CartItem ci WHERE ci.productId = :productId")
  List<CartItem> findByProductId(@Param("productId") Long productId);
}
