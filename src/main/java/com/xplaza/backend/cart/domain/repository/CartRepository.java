/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.cart.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.xplaza.backend.cart.domain.entity.Cart;
import com.xplaza.backend.cart.domain.entity.Cart.CartStatus;

/**
 * Repository for Cart aggregate root.
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

  /**
   * Find active cart by customer ID.
   */
  Optional<Cart> findByCustomerIdAndStatus(Long customerId, CartStatus status);

  /**
   * Find active cart by customer ID (convenience method).
   */
  default Optional<Cart> findActiveCartByCustomerId(Long customerId) {
    return findByCustomerIdAndStatus(customerId, CartStatus.ACTIVE);
  }

  /**
   * Find active cart by session ID (for guest carts).
   */
  Optional<Cart> findBySessionIdAndStatus(String sessionId, CartStatus status);

  /**
   * Find active cart by session ID (convenience method).
   */
  default Optional<Cart> findActiveCartBySessionId(String sessionId) {
    return findBySessionIdAndStatus(sessionId, CartStatus.ACTIVE);
  }

  /**
   * Find cart with items eagerly loaded.
   */
  @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.id = :cartId")
  Optional<Cart> findByIdWithItems(@Param("cartId") UUID cartId);

  /**
   * Find active cart by customer with items.
   */
  @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.customerId = :customerId AND c.status = 'ACTIVE'")
  Optional<Cart> findActiveCartByCustomerIdWithItems(@Param("customerId") Long customerId);

  /**
   * Find active cart by session with items.
   */
  @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.sessionId = :sessionId AND c.status = 'ACTIVE'")
  Optional<Cart> findActiveCartBySessionIdWithItems(@Param("sessionId") String sessionId);

  /**
   * Find all carts for a customer.
   */
  List<Cart> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

  /**
   * Find abandoned carts (expired and still active).
   */
  @Query("SELECT c FROM Cart c WHERE c.status = 'ACTIVE' AND c.expiresAt < :now")
  List<Cart> findAbandonedCarts(@Param("now") Instant now);

  /**
   * Find carts inactive for a period.
   */
  @Query("SELECT c FROM Cart c WHERE c.status = 'ACTIVE' AND c.lastActivityAt < :since")
  List<Cart> findInactiveCarts(@Param("since") Instant since);

  /**
   * Count active carts for a customer.
   */
  long countByCustomerIdAndStatus(Long customerId, CartStatus status);

  /**
   * Mark expired carts as abandoned.
   */
  @Modifying
  @Query("UPDATE Cart c SET c.status = 'ABANDONED' WHERE c.status = 'ACTIVE' AND c.expiresAt < :now")
  int markAbandonedCarts(@Param("now") Instant now);

  /**
   * Delete old abandoned carts.
   */
  @Modifying
  @Query("DELETE FROM Cart c WHERE c.status = 'ABANDONED' AND c.updatedAt < :before")
  int deleteOldAbandonedCarts(@Param("before") Instant before);
}
