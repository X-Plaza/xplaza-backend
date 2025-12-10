/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.promotion.controller;

import java.util.List;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.promotion.domain.entity.DiscountType;
import com.xplaza.backend.promotion.dto.request.DiscountTypeRequest;
import com.xplaza.backend.promotion.dto.response.DiscountTypeResponse;
import com.xplaza.backend.promotion.mapper.DiscountTypeMapper;
import com.xplaza.backend.promotion.service.DiscountTypeService;

@RestController
@RequestMapping("/api/v1/discount-types")
@RequiredArgsConstructor
@Tag(name = "Discount Type Management", description = "APIs for managing discount types")
public class DiscountTypeController {
  private final DiscountTypeService discountTypeService;
  private final DiscountTypeMapper discountTypeMapper;

  @GetMapping
  @Operation(summary = "List all discount types")
  public ResponseEntity<ApiResponse<List<DiscountTypeResponse>>> listDiscountTypes() {
    List<DiscountTypeResponse> response = discountTypeService.listDiscountTypes().stream()
        .map(discountTypeMapper::toResponse).toList();
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get discount type by ID")
  public ResponseEntity<ApiResponse<DiscountTypeResponse>> getDiscountType(@PathVariable Long id) {
    DiscountType discountType = discountTypeService.getDiscountType(id);
    return ResponseEntity.ok(ApiResponse.ok(discountTypeMapper.toResponse(discountType)));
  }

  @PostMapping
  @Operation(summary = "Create a new discount type")
  public ResponseEntity<ApiResponse<DiscountTypeResponse>> createDiscountType(
      @RequestBody @Valid DiscountTypeRequest request) {
    DiscountType entity = discountTypeMapper.toEntity(request);
    DiscountType saved = discountTypeService.createDiscountType(entity);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created(discountTypeMapper.toResponse(saved)));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update an existing discount type")
  public ResponseEntity<ApiResponse<DiscountTypeResponse>> updateDiscountType(@PathVariable Long id,
      @RequestBody @Valid DiscountTypeRequest request) {
    DiscountType details = discountTypeMapper.toEntity(request);
    DiscountType updated = discountTypeService.updateDiscountType(id, details);
    return ResponseEntity.ok(ApiResponse.ok(discountTypeMapper.toResponse(updated)));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a discount type")
  public ResponseEntity<ApiResponse<Void>> deleteDiscountType(@PathVariable Long id) {
    discountTypeService.deleteDiscountType(id);
    return ResponseEntity.ok(ApiResponse.ok("Discount type deleted"));
  }
}
