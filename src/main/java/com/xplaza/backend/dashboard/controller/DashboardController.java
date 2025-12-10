/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.dashboard.controller;

import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.dashboard.dto.DashboardSummary;
import com.xplaza.backend.dashboard.dto.RevenueData;
import com.xplaza.backend.dashboard.dto.TopCustomer;
import com.xplaza.backend.dashboard.dto.TopProduct;
import com.xplaza.backend.dashboard.service.DashboardService;

@RestController
@RequestMapping("/api/v1/dashboard")
@RequiredArgsConstructor
@Tag(name = "Dashboard", description = "APIs for dashboard statistics and analytics")
public class DashboardController {
  private final DashboardService dashboardService;

  @GetMapping("/summary")
  @Operation(summary = "Get dashboard summary", description = "Returns aggregated statistics for the dashboard")
  public ResponseEntity<ApiResponse<DashboardSummary>> getDashboardSummary() {
    DashboardSummary summary = dashboardService.getDashboardSummary();
    return ResponseEntity.ok(ApiResponse.ok(summary));
  }

  @GetMapping("/top-products")
  @Operation(summary = "Get top products", description = "Returns top selling products")
  public ResponseEntity<ApiResponse<List<TopProduct>>> getTopProducts(
      @RequestParam(defaultValue = "10") int limit) {
    List<TopProduct> topProducts = dashboardService.getTopProducts(limit);
    return ResponseEntity.ok(ApiResponse.ok(topProducts));
  }

  @GetMapping("/top-customers")
  @Operation(summary = "Get top customers", description = "Returns top customers by total spent")
  public ResponseEntity<ApiResponse<List<TopCustomer>>> getTopCustomers(
      @RequestParam(defaultValue = "10") int limit) {
    List<TopCustomer> topCustomers = dashboardService.getTopCustomers(limit);
    return ResponseEntity.ok(ApiResponse.ok(topCustomers));
  }

  @GetMapping("/revenue")
  @Operation(summary = "Get revenue data", description = "Returns daily revenue for the specified number of days")
  public ResponseEntity<ApiResponse<List<RevenueData>>> getRevenueData(
      @RequestParam(defaultValue = "30") int days) {
    List<RevenueData> revenueData = dashboardService.getRevenueData(days);
    return ResponseEntity.ok(ApiResponse.ok(revenueData));
  }
}
