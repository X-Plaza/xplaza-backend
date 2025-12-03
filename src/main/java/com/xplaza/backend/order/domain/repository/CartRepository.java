/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.order.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.xplaza.backend.order.domain.entity.Cart;

/**
 * Repository for Cart entity.
 */
@Repository
public interface CartRepository extends JpaRepository<Cart, UUID> {

  Optional<Cart> findByCustomerIdAndStatus(Long customerId, Cart.CartStatus status);

  Optional<Cart> findBySessionIdAndStatus(String sessionId, Cart.CartStatus status);

  @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.cartId = :cartId")
  Optional<Cart> findByIdWithItems(@Param("cartId") UUID cartId);

  @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.customerId = :customerId AND c.status = 'ACTIVE'")
  Optional<Cart> findActiveCartByCustomerId(@Param("customerId") Long customerId);

  @Query("SELECT c FROM Cart c LEFT JOIN FETCH c.items WHERE c.sessionId = :sessionId AND c.status = 'ACTIVE'")
  Optional<Cart> findActiveCartBySessionId(@Param("sessionId") String sessionId);

  @Query("SELECT c FROM Cart c WHERE c.status = 'ACTIVE' AND c.expiresAt < :now")
  List<Cart> findExpiredCarts(@Param("now") Instant now);

  @Query("SELECT c FROM Cart c WHERE c.status = 'ABANDONED' AND c.updatedAt < :cutoff")
  List<Cart> findAbandonedCartsOlderThan(@Param("cutoff") Instant cutoff);

  @Modifying
  @Query("UPDATE Cart c SET c.status = 'EXPIRED' WHERE c.status = 'ACTIVE' AND c.expiresAt < :now")
  int expireCarts(@Param("now") Instant now);

  @Modifying
  @Query("UPDATE Cart c SET c.status = 'ABANDONED' WHERE c.status = 'ACTIVE' AND c.updatedAt < :cutoff")
  int markCartsAsAbandoned(@Param("cutoff") Instant cutoff);

  @Query("SELECT COUNT(c) FROM Cart c WHERE c.customerId = :customerId AND c.status = 'CONVERTED'")
  long countConvertedCartsByCustomerId(@Param("customerId") Long customerId);
}
