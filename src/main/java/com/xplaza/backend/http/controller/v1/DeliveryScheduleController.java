/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller.v1;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.common.util.ApiResponse.PageMeta;
import com.xplaza.backend.domain.DeliverySchedule;
import com.xplaza.backend.http.dto.request.DeliveryScheduleRequest;
import com.xplaza.backend.http.dto.response.DeliveryScheduleResponse;
import com.xplaza.backend.mapper.DeliveryScheduleMapper;
import com.xplaza.backend.service.DeliveryScheduleService;

/**
 * Delivery Schedule Controller - Clean REST API design.
 */
@RestController
@RequestMapping("/api/v1/delivery-schedules")
@RequiredArgsConstructor
@Validated
@Tag(name = "Delivery Schedule Management", description = "APIs for managing delivery schedules")
public class DeliveryScheduleController {

  private final DeliveryScheduleService deliveryScheduleService;
  private final DeliveryScheduleMapper deliveryScheduleMapper;

  @GetMapping
  @Operation(summary = "List delivery schedules", description = "Get paginated list of delivery schedules")
  public ResponseEntity<ApiResponse<List<DeliveryScheduleResponse>>> getDeliverySchedules(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) int size,
      @RequestParam(defaultValue = "deliveryScheduleId") String sort,
      @RequestParam(defaultValue = "ASC") Sort.Direction direction) {

    size = Math.min(size, 100);
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

    List<DeliverySchedule> allSchedules = deliveryScheduleService.listDeliverySchedules();

    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), allSchedules.size());
    List<DeliverySchedule> pageContent = start < allSchedules.size() ? allSchedules.subList(start, end) : List.of();

    List<DeliveryScheduleResponse> dtos = pageContent.stream()
        .map(deliveryScheduleMapper::toResponse)
        .toList();

    Page<DeliveryScheduleResponse> responsePage = new PageImpl<>(dtos, pageable, allSchedules.size());
    PageMeta pageMeta = PageMeta.from(responsePage);

    return ResponseEntity.ok(ApiResponse.ok(dtos, pageMeta));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get delivery schedule by ID", description = "Retrieve a specific delivery schedule")
  public ResponseEntity<ApiResponse<DeliveryScheduleResponse>> getDeliverySchedule(@PathVariable @Positive Long id) {
    DeliverySchedule schedule = deliveryScheduleService.listDeliverySchedule(id);
    DeliveryScheduleResponse dto = deliveryScheduleMapper.toResponse(schedule);
    return ResponseEntity.ok(ApiResponse.ok(dto));
  }

  @PostMapping
  @Operation(summary = "Create delivery schedule", description = "Create a new delivery schedule")
  public ResponseEntity<ApiResponse<Void>> createDeliverySchedule(
      @RequestBody @Valid DeliveryScheduleRequest request) {
    DeliverySchedule entity = deliveryScheduleMapper.toEntity(request);
    deliveryScheduleService.addSchedule(entity);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Delivery schedule created successfully"));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update delivery schedule", description = "Update an existing delivery schedule")
  public ResponseEntity<ApiResponse<Void>> updateDeliverySchedule(
      @PathVariable @Positive Long id,
      @RequestBody @Valid DeliveryScheduleRequest request) {
    DeliverySchedule entity = deliveryScheduleMapper.toEntity(request);
    entity.setDeliveryScheduleId(id);
    deliveryScheduleService.updateSchedule(entity);
    return ResponseEntity.ok(ApiResponse.ok("Delivery schedule updated successfully"));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete delivery schedule", description = "Delete a delivery schedule")
  public ResponseEntity<ApiResponse<Void>> deleteDeliverySchedule(@PathVariable @Positive Long id) {
    deliveryScheduleService.deleteSchedule(id);
    return ResponseEntity.ok(ApiResponse.ok("Delivery schedule deleted successfully"));
  }
}
