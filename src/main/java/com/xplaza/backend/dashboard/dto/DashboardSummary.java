/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.dashboard.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummary {
  private Long totalOrders;
  private Long pendingOrders;
  private Long completedOrders;
  private Long cancelledOrders;

  private BigDecimal totalRevenue;
  private BigDecimal todayRevenue;
  private BigDecimal thisWeekRevenue;
  private BigDecimal thisMonthRevenue;

  private Long totalCustomers;
  private Long newCustomersThisMonth;

  private Long totalProducts;
  private Long activeProducts;
  private Long outOfStockProducts;

  private Long totalShops;
  private Long activeShops;
}
