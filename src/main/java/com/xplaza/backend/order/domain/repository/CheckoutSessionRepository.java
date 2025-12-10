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

import com.xplaza.backend.order.domain.entity.CheckoutSession;

/**
 * Repository for CheckoutSession entity.
 */
@Repository
public interface CheckoutSessionRepository extends JpaRepository<CheckoutSession, UUID> {

  Optional<CheckoutSession> findByCartId(UUID cartId);

  Optional<CheckoutSession> findByCustomerIdAndStatus(Long customerId, CheckoutSession.CheckoutStatus status);

  @Query("SELECT cs FROM CheckoutSession cs WHERE cs.customerId = :customerId AND cs.status = 'STARTED' ORDER BY cs.createdAt DESC")
  Optional<CheckoutSession> findActiveCheckout(@Param("customerId") Long customerId);

  @Query("SELECT cs FROM CheckoutSession cs WHERE cs.cartId = :cartId AND cs.status NOT IN ('COMPLETED', 'ABANDONED', 'FAILED')")
  Optional<CheckoutSession> findActiveCheckoutByCartId(@Param("cartId") UUID cartId);

  List<CheckoutSession> findByCustomerId(Long customerId);

  @Query("SELECT cs FROM CheckoutSession cs WHERE cs.status = 'STARTED' AND cs.expiresAt < :now")
  List<CheckoutSession> findExpiredCheckouts(@Param("now") Instant now);

  @Modifying
  @Query("UPDATE CheckoutSession cs SET cs.status = 'ABANDONED' WHERE cs.status = 'STARTED' AND cs.expiresAt < :now")
  int abandonExpiredCheckouts(@Param("now") Instant now);

  @Query("SELECT COUNT(cs) FROM CheckoutSession cs WHERE cs.customerId = :customerId AND cs.status = 'COMPLETED'")
  long countCompletedByCustomerId(@Param("customerId") Long customerId);

  @Query("SELECT COUNT(cs) FROM CheckoutSession cs WHERE cs.customerId = :customerId AND cs.status = 'ABANDONED'")
  long countAbandonedByCustomerId(@Param("customerId") Long customerId);
}
