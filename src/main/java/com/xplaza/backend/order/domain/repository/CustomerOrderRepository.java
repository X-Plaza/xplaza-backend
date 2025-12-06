/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.order.domain.repository;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.xplaza.backend.dashboard.dto.RevenueData;
import com.xplaza.backend.dashboard.dto.TopCustomer;
import com.xplaza.backend.order.domain.entity.CustomerOrder;

/**
 * Repository for Customer Order entity (UUID-based).
 */
@Repository
public interface CustomerOrderRepository extends JpaRepository<CustomerOrder, UUID> {

  Optional<CustomerOrder> findByOrderNumber(String orderNumber);

  @Query("SELECT o FROM CustomerOrder o LEFT JOIN FETCH o.items WHERE o.orderId = :orderId")
  Optional<CustomerOrder> findByIdWithItems(@Param("orderId") UUID orderId);

  @Query("SELECT o FROM CustomerOrder o LEFT JOIN FETCH o.items LEFT JOIN FETCH o.statusHistory WHERE o.orderId = :orderId")
  Optional<CustomerOrder> findByIdWithDetails(@Param("orderId") UUID orderId);

  List<CustomerOrder> findByCustomerIdOrderByCreatedAtDesc(Long customerId);

  Page<CustomerOrder> findByCustomerId(Long customerId, Pageable pageable);

  List<CustomerOrder> findByShopIdOrderByCreatedAtDesc(Long shopId);

  Page<CustomerOrder> findByShopId(Long shopId, Pageable pageable);

  List<CustomerOrder> findByStatus(CustomerOrder.OrderStatus status);

  Page<CustomerOrder> findByStatus(CustomerOrder.OrderStatus status, Pageable pageable);

  long countByCouponCode(String couponCode);

  @Query("SELECT o FROM CustomerOrder o WHERE o.customerId = :customerId AND o.status = :status")
  List<CustomerOrder> findByCustomerIdAndStatus(
      @Param("customerId") Long customerId,
      @Param("status") CustomerOrder.OrderStatus status);

  @Query("SELECT o FROM CustomerOrder o WHERE o.shopId = :shopId AND o.status = :status")
  List<CustomerOrder> findByShopIdAndStatus(
      @Param("shopId") Long shopId,
      @Param("status") CustomerOrder.OrderStatus status);

  @Query("SELECT o FROM CustomerOrder o WHERE o.shopId = :shopId AND o.status IN :statuses")
  List<CustomerOrder> findByShopIdAndStatusIn(
      @Param("shopId") Long shopId,
      @Param("statuses") List<CustomerOrder.OrderStatus> statuses);

  @Query("SELECT o FROM CustomerOrder o WHERE o.createdAt BETWEEN :start AND :end")
  List<CustomerOrder> findOrdersInDateRange(
      @Param("start") Instant start,
      @Param("end") Instant end);

  @Query("SELECT o FROM CustomerOrder o WHERE o.shopId = :shopId AND o.createdAt BETWEEN :start AND :end")
  List<CustomerOrder> findShopOrdersInDateRange(
      @Param("shopId") Long shopId,
      @Param("start") Instant start,
      @Param("end") Instant end);

  @Query("SELECT o FROM CustomerOrder o WHERE o.requestedDeliveryDate = :date AND o.status NOT IN ('CANCELLED', 'DELIVERED', 'RETURNED')")
  List<CustomerOrder> findOrdersForDeliveryDate(@Param("date") LocalDate date);

  @Query("SELECT COUNT(o) FROM CustomerOrder o WHERE o.customerId = :customerId")
  long countByCustomerId(@Param("customerId") Long customerId);

  @Query("SELECT COUNT(o) FROM CustomerOrder o WHERE o.shopId = :shopId AND o.status = :status")
  long countByShopIdAndStatus(
      @Param("shopId") Long shopId,
      @Param("status") CustomerOrder.OrderStatus status);

  @Query("SELECT SUM(o.grandTotal) FROM CustomerOrder o WHERE o.shopId = :shopId AND o.status = 'DELIVERED' AND o.createdAt BETWEEN :start AND :end")
  BigDecimal calculateShopRevenue(
      @Param("shopId") Long shopId,
      @Param("start") Instant start,
      @Param("end") Instant end);

  @Query("SELECT SUM(o.grandTotal) FROM CustomerOrder o WHERE o.customerId = :customerId AND o.status IN ('CONFIRMED', 'PROCESSING', 'SHIPPED', 'DELIVERED')")
  BigDecimal calculateCustomerLifetimeValue(@Param("customerId") Long customerId);

  @Modifying
  @Query("UPDATE CustomerOrder o SET o.status = :newStatus, o.updatedAt = :now WHERE o.orderId = :orderId")
  int updateStatus(
      @Param("orderId") UUID orderId,
      @Param("newStatus") CustomerOrder.OrderStatus newStatus,
      @Param("now") Instant now);

  @Query("SELECT o FROM CustomerOrder o WHERE o.status = 'PENDING' AND o.createdAt < :cutoff")
  List<CustomerOrder> findStalePendingOrders(@Param("cutoff") Instant cutoff);

  @Query("SELECT o FROM CustomerOrder o WHERE o.paymentStatus = 'PENDING' AND o.createdAt < :cutoff")
  List<CustomerOrder> findOrdersWithPendingPayment(@Param("cutoff") Instant cutoff);

  boolean existsByOrderNumber(String orderNumber);

  @Query(value = "SELECT nextval('order_number_seq')", nativeQuery = true)
  Long getNextOrderSequence();

  @Query("SELECT COUNT(o) FROM CustomerOrder o WHERE o.status = :status")
  long countByStatus(@Param("status") CustomerOrder.OrderStatus status);

  @Query("SELECT COUNT(o) FROM CustomerOrder o WHERE o.status IN :statuses")
  long countByStatusIn(@Param("statuses") List<CustomerOrder.OrderStatus> statuses);

  @Query("SELECT SUM(o.grandTotal) FROM CustomerOrder o WHERE o.status IN :statuses AND o.createdAt BETWEEN :start AND :end")
  BigDecimal sumGrandTotalByStatusInAndCreatedAtBetween(
      @Param("statuses") List<CustomerOrder.OrderStatus> statuses,
      @Param("start") Instant start,
      @Param("end") Instant end);

  @Query("SELECT COUNT(o) FROM CustomerOrder o WHERE o.createdAt BETWEEN :start AND :end")
  long countByCreatedAtBetween(@Param("start") Instant start, @Param("end") Instant end);

  @Query("SELECT new com.xplaza.backend.dashboard.dto.TopCustomer(o.customerId, CONCAT(o.shippingFirstName, ' ', o.shippingLastName), COUNT(o), SUM(o.grandTotal)) "
      +
      "FROM CustomerOrder o " +
      "GROUP BY o.customerId, o.shippingFirstName, o.shippingLastName " +
      "ORDER BY SUM(o.grandTotal) DESC")
  List<TopCustomer> findTopCustomers(Pageable pageable);

  @Query("SELECT new com.xplaza.backend.dashboard.dto.RevenueData(CAST(o.createdAt AS LocalDate), SUM(o.grandTotal), COUNT(o)) "
      +
      "FROM CustomerOrder o " +
      "WHERE o.createdAt >= :start " +
      "GROUP BY CAST(o.createdAt AS LocalDate) " +
      "ORDER BY CAST(o.createdAt AS LocalDate) ASC")
  List<RevenueData> findRevenueData(@Param("start") Instant start);

  @Query("SELECT COUNT(DISTINCT o.customerId) FROM CustomerOrder o")
  long countDistinctCustomers();

  @Query("SELECT COUNT(DISTINCT o.customerId) FROM CustomerOrder o WHERE o.createdAt >= :start")
  long countDistinctCustomersSince(@Param("start") Instant start);
}
