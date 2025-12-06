/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.payment.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.xplaza.backend.payment.domain.entity.Refund;

/**
 * Repository for Refund entity.
 */
@Repository
public interface RefundRepository extends JpaRepository<Refund, UUID> {

  List<Refund> findByOrderId(UUID orderId);

  Optional<Refund> findByGatewayRefundId(String gatewayRefundId);

  @Query("SELECT r FROM Refund r WHERE r.status = 'PENDING' ORDER BY r.createdAt ASC")
  Page<Refund> findPendingRefunds(Pageable pageable);

  @Query("SELECT r FROM Refund r WHERE r.status = :status ORDER BY r.createdAt DESC")
  Page<Refund> findByStatus(@Param("status") Refund.RefundStatus status, Pageable pageable);

  @Query("SELECT r FROM Refund r WHERE r.requestedBy = :customerId ORDER BY r.createdAt DESC")
  Page<Refund> findByRequestedBy(@Param("customerId") Long customerId, Pageable pageable);

  @Query("SELECT SUM(r.totalAmount) FROM Refund r WHERE r.orderId = :orderId AND r.status = 'COMPLETED'")
  java.math.BigDecimal sumCompletedRefundsByOrderId(@Param("orderId") UUID orderId);

  @Query("SELECT COUNT(r) FROM Refund r WHERE r.orderId = :orderId AND r.status NOT IN ('REJECTED')")
  long countActiveRefundsByOrderId(@Param("orderId") UUID orderId);
}
