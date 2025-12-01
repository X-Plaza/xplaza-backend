/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.jpa.dao.RevenueDao;
import com.xplaza.backend.service.DashboardService;

@RestController
@RequestMapping("/api/v1/dashboard")
public class DashboardController extends BaseController {
  private final DashboardService dashboardService;

  @Autowired
  public DashboardController(DashboardService dashboardService) {
    this.dashboardService = dashboardService;
  }

  @PostMapping
  public ResponseEntity<ApiResponse> dashboardDetails(@RequestParam(value = "shop_id") @Valid Long shop_id)
      throws JsonProcessingException {
    long start = System.currentTimeMillis();
    RevenueDao dto = dashboardService.getDashboardDetails(shop_id);
    long responseTime = System.currentTimeMillis() - start;
    String data = new ObjectMapper().writeValueAsString(dto);
    ApiResponse response = new ApiResponse(responseTime, "Dashboard Details", HttpStatus.OK.value(), "Success", "",
        data);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/monthly-profits")
  public ResponseEntity<ApiResponse> getMonthlyProfit(@RequestParam(value = "shop_id") @Valid Long shop_id,
      @RequestParam(value = "month") @Valid int month) throws JsonProcessingException {
    long start = System.currentTimeMillis();
    Double dto = dashboardService.getMonthlyProfit(shop_id, month);
    long responseTime = System.currentTimeMillis() - start;
    String data = new ObjectMapper().writeValueAsString(dto);
    ApiResponse response = new ApiResponse(responseTime, "Monthly Profit", HttpStatus.OK.value(), "Success", "", data);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/monthly-sales")
  public ResponseEntity<ApiResponse> getMonthlySales(@RequestParam(value = "shop_id") @Valid Long shop_id,
      @RequestParam(value = "month") @Valid int month) throws JsonProcessingException {
    long start = System.currentTimeMillis();
    Double dto = dashboardService.getMonthlySales(shop_id, month);
    long responseTime = System.currentTimeMillis() - start;
    String data = new ObjectMapper().writeValueAsString(dto);
    ApiResponse response = new ApiResponse(responseTime, "Monthly Sales", HttpStatus.OK.value(), "Success", "", data);
    return ResponseEntity.ok(response);
  }
}
