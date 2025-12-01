/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller.v2;

import java.util.List;

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
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.common.util.ApiResponseV2;
import com.xplaza.backend.common.util.ApiResponseV2.PageMeta;
import com.xplaza.backend.http.dto.response.DayResponse;
import com.xplaza.backend.mapper.DayMapper;
import com.xplaza.backend.service.DayService;
import com.xplaza.backend.service.entity.Day;

/**
 * V2 Day Controller - Clean REST API design. Days are reference data (Mon-Sun)
 * - read-only access.
 */
@RestController
@RequestMapping("/api/v2/days")
@RequiredArgsConstructor
@Validated
@Tag(name = "Day Reference Data V2", description = "V2 APIs for accessing day reference data")
public class DayControllerV2 {

  private final DayService dayService;
  private final DayMapper dayMapper;

  @GetMapping
  @Operation(summary = "List days", description = "Get list of days (Mon-Sun)")
  public ResponseEntity<ApiResponseV2<List<DayResponse>>> getDays(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) int size,
      @RequestParam(defaultValue = "dayId") String sort,
      @RequestParam(defaultValue = "ASC") Sort.Direction direction) {

    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

    List<Day> allDays = dayService.listDays();

    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), allDays.size());
    List<Day> pageContent = start < allDays.size() ? allDays.subList(start, end) : List.of();

    List<DayResponse> dtos = pageContent.stream()
        .map(dayMapper::toResponse)
        .toList();

    Page<DayResponse> responsePage = new PageImpl<>(dtos, pageable, allDays.size());
    PageMeta pageMeta = PageMeta.from(responsePage);

    return ResponseEntity.ok(ApiResponseV2.ok(dtos, pageMeta));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get day by ID", description = "Retrieve a specific day")
  public ResponseEntity<ApiResponseV2<DayResponse>> getDay(@PathVariable @Positive Long id) {
    // Days are a fixed set (Mon-Sun), find from list
    List<Day> allDays = dayService.listDays();
    Day day = allDays.stream()
        .filter(d -> d.getDayId().equals(id))
        .findFirst()
        .orElseThrow(
            () -> new com.xplaza.backend.exception.ResourceNotFoundException("Day not found with id: " + id));
    DayResponse dto = dayMapper.toResponse(day);
    return ResponseEntity.ok(ApiResponseV2.ok(dto));
  }
}
