/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.customer.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.xplaza.backend.customer.domain.entity.WishlistItem;

/**
 * Repository for WishlistItem entity.
 */
@Repository
public interface WishlistItemRepository extends JpaRepository<WishlistItem, UUID> {

  List<WishlistItem> findByWishlistWishlistId(UUID wishlistId);

  @Query("SELECT wi FROM WishlistItem wi WHERE wi.wishlist.wishlistId = :wishlistId AND wi.productId = :productId")
  Optional<WishlistItem> findByWishlistIdAndProductId(
      @Param("wishlistId") UUID wishlistId,
      @Param("productId") Long productId);

  @Query("SELECT wi FROM WishlistItem wi WHERE wi.wishlist.wishlistId = :wishlistId AND wi.productId = :productId AND wi.variantId = :variantId")
  Optional<WishlistItem> findByWishlistIdAndProductIdAndVariantId(
      @Param("wishlistId") UUID wishlistId,
      @Param("productId") Long productId,
      @Param("variantId") UUID variantId);

  @Query("SELECT wi FROM WishlistItem wi WHERE wi.productId = :productId AND wi.notifyPriceDrop = true")
  List<WishlistItem> findByProductIdWithPriceDropNotification(@Param("productId") Long productId);

  @Query("SELECT wi FROM WishlistItem wi WHERE wi.productId = :productId AND wi.notifyBackInStock = true")
  List<WishlistItem> findByProductIdWithBackInStockNotification(@Param("productId") Long productId);

  @Modifying
  @Query("DELETE FROM WishlistItem wi WHERE wi.wishlist.wishlistId = :wishlistId")
  void deleteByWishlistId(@Param("wishlistId") UUID wishlistId);

  @Query("SELECT COUNT(wi) FROM WishlistItem wi WHERE wi.wishlist.wishlistId = :wishlistId")
  long countByWishlistId(@Param("wishlistId") UUID wishlistId);

  @Query("SELECT COUNT(wi) FROM WishlistItem wi WHERE wi.productId = :productId")
  long countByProductId(@Param("productId") Long productId);
}
