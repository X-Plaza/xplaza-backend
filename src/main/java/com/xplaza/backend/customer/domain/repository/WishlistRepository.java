/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.customer.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.xplaza.backend.customer.domain.entity.Wishlist;

/**
 * Repository for Wishlist entity.
 */
@Repository
public interface WishlistRepository extends JpaRepository<Wishlist, UUID> {

  List<Wishlist> findByCustomerId(Long customerId);

  Optional<Wishlist> findByCustomerIdAndIsDefaultTrue(Long customerId);

  @Query("SELECT w FROM Wishlist w WHERE w.customerId = :customerId AND w.name = :name")
  Optional<Wishlist> findByCustomerIdAndName(@Param("customerId") Long customerId, @Param("name") String name);

  @Query("SELECT w FROM Wishlist w WHERE w.shareToken = :shareToken AND w.visibility = 'PUBLIC'")
  Optional<Wishlist> findByShareToken(@Param("shareToken") String shareToken);

  @Query("SELECT COUNT(w) FROM Wishlist w WHERE w.customerId = :customerId")
  long countByCustomerId(@Param("customerId") Long customerId);

  @Query("SELECT w FROM Wishlist w LEFT JOIN FETCH w.items WHERE w.wishlistId = :wishlistId")
  Optional<Wishlist> findByIdWithItems(@Param("wishlistId") UUID wishlistId);
}
