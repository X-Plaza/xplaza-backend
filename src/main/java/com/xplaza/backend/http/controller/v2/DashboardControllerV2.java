/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller.v2;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.common.util.ApiResponseV2;
import com.xplaza.backend.jpa.dao.RevenueDao;
import com.xplaza.backend.service.DashboardService;

/**
 * V2 Dashboard Controller - Clean REST API design.
 */
@RestController
@RequestMapping("/api/v2/dashboard")
@RequiredArgsConstructor
@Validated
@Tag(name = "Dashboard Analytics V2", description = "V2 APIs for dashboard analytics")
public class DashboardControllerV2 {

  private final DashboardService dashboardService;

  @GetMapping
  @Operation(summary = "Get dashboard details", description = "Get dashboard analytics for a shop")
  public ResponseEntity<ApiResponseV2<RevenueDao>> getDashboard(
      @RequestParam @Positive Long shopId) {
    RevenueDao revenueDao = dashboardService.getDashboardDetails(shopId);
    return ResponseEntity.ok(ApiResponseV2.ok(revenueDao));
  }

  @GetMapping("/monthly-profit")
  @Operation(summary = "Get monthly profit", description = "Get monthly profit for a shop")
  public ResponseEntity<ApiResponseV2<Double>> getMonthlyProfit(
      @RequestParam @Positive Long shopId,
      @RequestParam @Min(1) @Max(12) int month) {
    Double profit = dashboardService.getMonthlyProfit(shopId, month);
    return ResponseEntity.ok(ApiResponseV2.ok(profit != null ? profit : 0.0));
  }

  @GetMapping("/monthly-sales")
  @Operation(summary = "Get monthly sales", description = "Get monthly sales for a shop")
  public ResponseEntity<ApiResponseV2<Double>> getMonthlySales(
      @RequestParam @Positive Long shopId,
      @RequestParam @Min(1) @Max(12) int month) {
    Double sales = dashboardService.getMonthlySales(shopId, month);
    return ResponseEntity.ok(ApiResponseV2.ok(sales != null ? sales : 0.0));
  }
}
