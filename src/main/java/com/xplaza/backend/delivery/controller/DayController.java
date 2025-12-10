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
import com.xplaza.backend.delivery.domain.entity.Day;
import com.xplaza.backend.delivery.dto.request.DayRequest;
import com.xplaza.backend.delivery.dto.response.DayResponse;
import com.xplaza.backend.delivery.mapper.DayMapper;
import com.xplaza.backend.delivery.service.DayService;

@RestController
@RequestMapping("/api/v1/days")
@RequiredArgsConstructor
@Tag(name = "Day Management", description = "APIs for managing days of the week")
public class DayController {
  private final DayService dayService;
  private final DayMapper dayMapper;

  @GetMapping
  @Operation(summary = "List all days")
  public ResponseEntity<ApiResponse<List<DayResponse>>> listDays() {
    List<DayResponse> response = dayService.listDays().stream().map(dayMapper::toResponse).toList();
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get day by ID")
  public ResponseEntity<ApiResponse<DayResponse>> getDay(@PathVariable Long id) {
    Day day = dayService.getDay(id);
    return ResponseEntity.ok(ApiResponse.ok(dayMapper.toResponse(day)));
  }

  @PostMapping
  @Operation(summary = "Create a new day")
  public ResponseEntity<ApiResponse<DayResponse>> createDay(@RequestBody @Valid DayRequest request) {
    Day entity = dayMapper.toEntity(request);
    Day saved = dayService.createDay(entity);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(dayMapper.toResponse(saved)));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update an existing day")
  public ResponseEntity<ApiResponse<DayResponse>> updateDay(@PathVariable Long id,
      @RequestBody @Valid DayRequest request) {
    Day details = dayMapper.toEntity(request);
    Day updated = dayService.updateDay(id, details);
    return ResponseEntity.ok(ApiResponse.ok(dayMapper.toResponse(updated)));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a day")
  public ResponseEntity<ApiResponse<Void>> deleteDay(@PathVariable Long id) {
    dayService.deleteDay(id);
    return ResponseEntity.ok(ApiResponse.ok("Day deleted"));
  }
}
