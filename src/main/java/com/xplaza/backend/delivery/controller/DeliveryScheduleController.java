/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.delivery.controller;

import java.util.List;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.delivery.domain.entity.DeliverySchedule;
import com.xplaza.backend.delivery.dto.request.DeliveryScheduleRequest;
import com.xplaza.backend.delivery.dto.response.DeliveryScheduleResponse;
import com.xplaza.backend.delivery.mapper.DeliveryScheduleMapper;
import com.xplaza.backend.delivery.service.DeliveryScheduleService;

@RestController
@RequestMapping("/api/v1/delivery-schedules")
@RequiredArgsConstructor
@Tag(name = "Delivery Schedule Management", description = "APIs for managing delivery schedules")
public class DeliveryScheduleController {
  private final DeliveryScheduleService deliveryScheduleService;
  private final DeliveryScheduleMapper deliveryScheduleMapper;

  @GetMapping
  @Operation(summary = "List all delivery schedules")
  public ResponseEntity<ApiResponse<List<DeliveryScheduleResponse>>> listDeliverySchedules(
      @RequestParam(required = false) Long shopId,
      @RequestParam(required = false) Long dayId) {
    List<DeliverySchedule> schedules;
    if (shopId != null) {
      schedules = deliveryScheduleService.listDeliverySchedulesByShop(shopId);
    } else if (dayId != null) {
      schedules = deliveryScheduleService.listDeliverySchedulesByDay(dayId);
    } else {
      schedules = deliveryScheduleService.listDeliverySchedules();
    }
    List<DeliveryScheduleResponse> response = schedules.stream()
        .map(deliveryScheduleMapper::toResponse).toList();
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get delivery schedule by ID")
  public ResponseEntity<ApiResponse<DeliveryScheduleResponse>> getDeliverySchedule(@PathVariable Long id) {
    DeliverySchedule schedule = deliveryScheduleService.getDeliverySchedule(id);
    return ResponseEntity.ok(ApiResponse.ok(deliveryScheduleMapper.toResponse(schedule)));
  }

  @PostMapping
  @Operation(summary = "Create a new delivery schedule")
  public ResponseEntity<ApiResponse<DeliveryScheduleResponse>> createDeliverySchedule(
      @RequestBody @Valid DeliveryScheduleRequest request) {
    DeliverySchedule entity = deliveryScheduleMapper.toEntity(request);
    DeliverySchedule saved = deliveryScheduleService.createDeliverySchedule(entity,
        request.getShopId(), request.getDayId());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created(deliveryScheduleMapper.toResponse(saved)));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update an existing delivery schedule")
  public ResponseEntity<ApiResponse<DeliveryScheduleResponse>> updateDeliverySchedule(@PathVariable Long id,
      @RequestBody @Valid DeliveryScheduleRequest request) {
    DeliverySchedule details = deliveryScheduleMapper.toEntity(request);
    DeliverySchedule updated = deliveryScheduleService.updateDeliverySchedule(id, details);
    return ResponseEntity.ok(ApiResponse.ok(deliveryScheduleMapper.toResponse(updated)));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a delivery schedule")
  public ResponseEntity<ApiResponse<Void>> deleteDeliverySchedule(@PathVariable Long id) {
    deliveryScheduleService.deleteDeliverySchedule(id);
    return ResponseEntity.ok(ApiResponse.ok("Delivery schedule deleted"));
  }
}
