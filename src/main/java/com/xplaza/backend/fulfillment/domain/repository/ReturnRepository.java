/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.fulfillment.domain.repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.xplaza.backend.fulfillment.domain.entity.Return;

/**
 * Repository for Return entity.
 */
@Repository
public interface ReturnRepository extends JpaRepository<Return, UUID> {

  List<Return> findByOrderId(Long orderId);

  Optional<Return> findByRmaNumber(String rmaNumber);

  Page<Return> findByCustomerId(Long customerId, Pageable pageable);

  @Query("SELECT r FROM Return r WHERE r.status = 'REQUESTED' ORDER BY r.createdAt ASC")
  Page<Return> findPendingReturns(Pageable pageable);

  @Query("SELECT r FROM Return r WHERE r.status = :status ORDER BY r.createdAt DESC")
  Page<Return> findByStatus(@Param("status") Return.ReturnStatus status, Pageable pageable);

  @Query("SELECT r FROM Return r WHERE r.status = 'APPROVED' AND r.returnTrackingNumber IS NULL ORDER BY r.createdAt ASC")
  List<Return> findApprovedAwaitingLabel();

  @Query("SELECT r FROM Return r WHERE r.status = 'RECEIVED' ORDER BY r.receivedAt ASC")
  List<Return> findReceivedAwaitingInspection();

  @Query("SELECT COUNT(r) FROM Return r WHERE r.orderId = :orderId AND r.status NOT IN ('CANCELLED', 'REJECTED')")
  long countActiveReturnsByOrderId(@Param("orderId") Long orderId);

  @Query("SELECT r FROM Return r LEFT JOIN FETCH r.items WHERE r.returnId = :returnId")
  Optional<Return> findByIdWithItems(@Param("returnId") UUID returnId);
}
