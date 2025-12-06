/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.fulfillment.domain.repository;

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

import com.xplaza.backend.fulfillment.domain.entity.Shipment;

/**
 * Repository for Shipment entity.
 */
@Repository
public interface ShipmentRepository extends JpaRepository<Shipment, UUID> {

  List<Shipment> findByOrderId(UUID orderId);

  Optional<Shipment> findByTrackingNumber(String trackingNumber);

  @Query("SELECT s FROM Shipment s WHERE s.status = :status ORDER BY s.createdAt ASC")
  Page<Shipment> findByStatus(@Param("status") Shipment.ShipmentStatus status, Pageable pageable);

  @Query("SELECT s FROM Shipment s WHERE s.warehouseId = :warehouseId AND s.status IN ('PENDING', 'LABEL_CREATED', 'PICKED', 'PACKED') ORDER BY s.createdAt ASC")
  List<Shipment> findPendingShipmentsByWarehouse(@Param("warehouseId") Long warehouseId);

  @Query("SELECT s FROM Shipment s WHERE s.status = 'SHIPPED' AND s.estimatedDeliveryDate < :now")
  List<Shipment> findOverdueShipments(@Param("now") Instant now);

  @Query("SELECT s FROM Shipment s WHERE s.carrier.carrierId = :carrierId AND s.status = 'IN_TRANSIT' ORDER BY s.shippedAt DESC")
  Page<Shipment> findInTransitByCarrier(@Param("carrierId") Long carrierId, Pageable pageable);

  @Query("SELECT COUNT(s) FROM Shipment s WHERE s.orderId = :orderId AND s.status = 'DELIVERED'")
  long countDeliveredByOrderId(@Param("orderId") UUID orderId);

  @Query("SELECT s FROM Shipment s LEFT JOIN FETCH s.items LEFT JOIN FETCH s.trackingEvents WHERE s.shipmentId = :shipmentId")
  Optional<Shipment> findByIdWithDetails(@Param("shipmentId") UUID shipmentId);
}
