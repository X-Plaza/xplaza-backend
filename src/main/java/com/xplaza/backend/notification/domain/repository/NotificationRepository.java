/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.notification.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.xplaza.backend.notification.domain.entity.Notification;

/**
 * Repository for Notification entity.
 */
@Repository
public interface NotificationRepository extends JpaRepository<Notification, UUID> {

  Page<Notification> findByCustomerId(Long customerId, Pageable pageable);

  @Query("SELECT n FROM Notification n WHERE n.customerId = :customerId AND n.isRead = false ORDER BY n.createdAt DESC")
  Page<Notification> findUnreadByCustomerId(@Param("customerId") Long customerId, Pageable pageable);

  @Query("SELECT n FROM Notification n WHERE n.customerId = :customerId AND n.channel = :channel ORDER BY n.createdAt DESC")
  Page<Notification> findByCustomerIdAndChannel(
      @Param("customerId") Long customerId,
      @Param("channel") Notification.NotificationChannel channel,
      Pageable pageable);

  @Query("SELECT COUNT(n) FROM Notification n WHERE n.customerId = :customerId AND n.isRead = false")
  long countUnreadByCustomerId(@Param("customerId") Long customerId);

  @Query("SELECT n FROM Notification n WHERE n.isSent = false AND (n.scheduledAt IS NULL OR n.scheduledAt <= :now) AND (n.expiresAt IS NULL OR n.expiresAt > :now)")
  List<Notification> findReadyToSend(@Param("now") Instant now);

  @Modifying
  @Query("UPDATE Notification n SET n.isRead = true, n.readAt = :now WHERE n.customerId = :customerId AND n.isRead = false")
  int markAllAsReadByCustomerId(@Param("customerId") Long customerId, @Param("now") Instant now);

  @Modifying
  @Query("DELETE FROM Notification n WHERE n.createdAt < :cutoff")
  int deleteOlderThan(@Param("cutoff") Instant cutoff);

  @Query("SELECT n FROM Notification n WHERE n.referenceType = :refType AND n.referenceId = :refId ORDER BY n.createdAt DESC")
  List<Notification> findByReference(
      @Param("refType") Notification.ReferenceType refType,
      @Param("refId") String refId);
}
