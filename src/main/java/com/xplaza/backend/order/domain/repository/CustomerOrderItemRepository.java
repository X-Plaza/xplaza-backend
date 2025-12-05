/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.order.domain.repository;

import java.util.List;
import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.xplaza.backend.order.domain.entity.CustomerOrderItem;

/**
 * Repository for Customer OrderItem entity (UUID-based).
 */
@Repository
public interface CustomerOrderItemRepository extends JpaRepository<CustomerOrderItem, UUID> {

  List<CustomerOrderItem> findByOrderOrderId(UUID orderId);

  @Query("SELECT oi FROM CustomerOrderItem oi WHERE oi.order.orderId = :orderId AND oi.productId = :productId")
  List<CustomerOrderItem> findByOrderIdAndProductId(
      @Param("orderId") UUID orderId,
      @Param("productId") Long productId);

  @Query("SELECT oi FROM CustomerOrderItem oi WHERE oi.order.orderId = :orderId AND oi.status = :status")
  List<CustomerOrderItem> findByOrderIdAndStatus(
      @Param("orderId") UUID orderId,
      @Param("status") CustomerOrderItem.ItemStatus status);

  @Modifying
  @Query("UPDATE CustomerOrderItem oi SET oi.status = :status WHERE oi.order.orderId = :orderId")
  int updateStatusForOrder(@Param("orderId") UUID orderId, @Param("status") CustomerOrderItem.ItemStatus status);

  @Query("SELECT SUM(oi.quantity) FROM CustomerOrderItem oi WHERE oi.productId = :productId AND oi.order.status IN ('CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED')")
  Long countTotalSoldByProductId(@Param("productId") Long productId);

  @Query("SELECT oi.productId, SUM(oi.quantity) as sold FROM CustomerOrderItem oi WHERE oi.shopId = :shopId AND oi.order.status = 'DELIVERED' GROUP BY oi.productId ORDER BY sold DESC")
  List<Object[]> findTopSellingProductsByShop(@Param("shopId") Long shopId);
}
