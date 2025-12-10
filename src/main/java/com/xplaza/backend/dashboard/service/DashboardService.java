/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.dashboard.service;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.time.temporal.TemporalAdjusters;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import com.xplaza.backend.catalog.domain.repository.ProductRepository;
import com.xplaza.backend.dashboard.dto.DashboardSummary;
import com.xplaza.backend.dashboard.dto.RevenueData;
import com.xplaza.backend.dashboard.dto.TopCustomer;
import com.xplaza.backend.dashboard.dto.TopProduct;
import com.xplaza.backend.order.domain.entity.CustomerOrder;
import com.xplaza.backend.order.domain.repository.CustomerOrderItemRepository;
import com.xplaza.backend.order.domain.repository.CustomerOrderRepository;
import com.xplaza.backend.shop.domain.repository.ShopRepository;

@Service
@RequiredArgsConstructor
public class DashboardService {
  private final CustomerOrderRepository orderRepository;
  private final ProductRepository productRepository;
  private final ShopRepository shopRepository;
  private final CustomerOrderItemRepository orderItemRepository;

  public DashboardSummary getDashboardSummary() {
    long totalOrders = orderRepository.count();
    long pendingOrders = orderRepository.countByStatus(CustomerOrder.OrderStatus.PENDING);
    long completedOrders = orderRepository.countByStatus(CustomerOrder.OrderStatus.DELIVERED);
    long cancelledOrders = orderRepository.countByStatus(CustomerOrder.OrderStatus.CANCELLED);

    Instant now = Instant.now();
    Instant startOfDay = LocalDate.now().atStartOfDay(ZoneId.systemDefault()).toInstant();
    Instant startOfWeek = LocalDate.now().with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        .atStartOfDay(ZoneId.systemDefault()).toInstant();
    Instant startOfMonth = LocalDate.now().with(TemporalAdjusters.firstDayOfMonth())
        .atStartOfDay(ZoneId.systemDefault()).toInstant();

    List<CustomerOrder.OrderStatus> revenueStatuses = List.of(CustomerOrder.OrderStatus.DELIVERED);

    BigDecimal totalRevenue = orderRepository.sumGrandTotalByStatusInAndCreatedAtBetween(revenueStatuses, Instant.EPOCH,
        now);
    BigDecimal todayRevenue = orderRepository.sumGrandTotalByStatusInAndCreatedAtBetween(revenueStatuses, startOfDay,
        now);
    BigDecimal thisWeekRevenue = orderRepository.sumGrandTotalByStatusInAndCreatedAtBetween(revenueStatuses,
        startOfWeek, now);
    BigDecimal thisMonthRevenue = orderRepository.sumGrandTotalByStatusInAndCreatedAtBetween(revenueStatuses,
        startOfMonth, now);

    totalRevenue = totalRevenue != null ? totalRevenue : BigDecimal.ZERO;
    todayRevenue = todayRevenue != null ? todayRevenue : BigDecimal.ZERO;
    thisWeekRevenue = thisWeekRevenue != null ? thisWeekRevenue : BigDecimal.ZERO;
    thisMonthRevenue = thisMonthRevenue != null ? thisMonthRevenue : BigDecimal.ZERO;

    long totalCustomers = orderRepository.countDistinctCustomers();
    long newCustomersThisMonth = orderRepository.countDistinctCustomersSince(startOfMonth);

    long totalProducts = productRepository.count();
    long outOfStockProducts = productRepository.countByQuantityLessThanEqual(0);
    long activeProducts = totalProducts - outOfStockProducts;

    long totalShops = shopRepository.count();
    long activeShops = totalShops;

    return DashboardSummary.builder()
        .totalOrders(totalOrders)
        .pendingOrders(pendingOrders)
        .completedOrders(completedOrders)
        .cancelledOrders(cancelledOrders)
        .totalRevenue(totalRevenue)
        .todayRevenue(todayRevenue)
        .thisWeekRevenue(thisWeekRevenue)
        .thisMonthRevenue(thisMonthRevenue)
        .totalCustomers(totalCustomers)
        .newCustomersThisMonth(newCustomersThisMonth)
        .totalProducts(totalProducts)
        .activeProducts(activeProducts)
        .outOfStockProducts(outOfStockProducts)
        .totalShops(totalShops)
        .activeShops(activeShops)
        .build();
  }

  public List<TopProduct> getTopProducts(int limit) {
    return orderItemRepository.findTopSellingProducts(PageRequest.of(0, limit));
  }

  public List<TopCustomer> getTopCustomers(int limit) {
    return orderRepository.findTopCustomers(PageRequest.of(0, limit));
  }

  public List<RevenueData> getRevenueData(int days) {
    Instant start = LocalDate.now().minusDays(days).atStartOfDay(ZoneId.systemDefault()).toInstant();
    return orderRepository.findRevenueData(start);
  }
}
