/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.payment.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.xplaza.backend.payment.domain.entity.PaymentTransaction;

/**
 * Repository for PaymentTransaction entity.
 */
@Repository
public interface PaymentTransactionRepository extends JpaRepository<PaymentTransaction, UUID> {

  List<PaymentTransaction> findByOrderId(UUID orderId);

  Optional<PaymentTransaction> findByGatewayTransactionId(String gatewayTransactionId);

  @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.orderId = :orderId AND pt.type = :type ORDER BY pt.createdAt DESC")
  List<PaymentTransaction> findByOrderIdAndType(@Param("orderId") UUID orderId,
      @Param("type") PaymentTransaction.TransactionType type);

  @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.orderId = :orderId AND pt.status = 'SUCCESS' AND pt.type = 'SALE' ORDER BY pt.createdAt DESC")
  Optional<PaymentTransaction> findCompletedSaleByOrderId(@Param("orderId") UUID orderId);

  @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.status = 'PENDING' AND pt.createdAt < :cutoff")
  List<PaymentTransaction> findStalePendingTransactions(@Param("cutoff") Instant cutoff);

  @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.customerId = :customerId ORDER BY pt.createdAt DESC")
  Page<PaymentTransaction> findByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

  @Query("SELECT SUM(pt.amount) FROM PaymentTransaction pt WHERE pt.orderId = :orderId AND pt.status = 'SUCCESS' AND pt.type = 'SALE'")
  java.math.BigDecimal sumCompletedAmountByOrderId(@Param("orderId") UUID orderId);

  @Query("SELECT SUM(pt.amount) FROM PaymentTransaction pt WHERE pt.orderId = :orderId AND pt.status = 'SUCCESS' AND pt.type = 'REFUND'")
  java.math.BigDecimal sumRefundedAmountByOrderId(@Param("orderId") UUID orderId);

  @Query("SELECT pt FROM PaymentTransaction pt WHERE pt.status = 'PENDING' AND pt.type = 'AUTHORIZATION' AND pt.createdAt < :expiry")
  List<PaymentTransaction> findExpiredAuthorizations(@Param("expiry") Instant expiry);
}
