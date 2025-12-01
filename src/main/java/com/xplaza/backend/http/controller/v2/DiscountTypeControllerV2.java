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
import com.xplaza.backend.http.dto.response.DiscountTypeResponse;
import com.xplaza.backend.mapper.DiscountTypeMapper;
import com.xplaza.backend.service.DiscountTypeService;
import com.xplaza.backend.service.entity.DiscountType;

/**
 * V2 Discount Type Controller - Clean REST API design. Discount types are
 * reference data - read-only access.
 */
@RestController
@RequestMapping("/api/v2/discount-types")
@RequiredArgsConstructor
@Validated
@Tag(name = "Discount Type Reference Data V2", description = "V2 APIs for accessing discount type reference data")
public class DiscountTypeControllerV2 {

  private final DiscountTypeService discountTypeService;
  private final DiscountTypeMapper discountTypeMapper;

  @GetMapping
  @Operation(summary = "List discount types", description = "Get list of discount types")
  public ResponseEntity<ApiResponseV2<List<DiscountTypeResponse>>> getDiscountTypes(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "10") @Min(1) int size,
      @RequestParam(defaultValue = "discountTypeId") String sort,
      @RequestParam(defaultValue = "ASC") Sort.Direction direction) {

    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

    List<DiscountType> allTypes = discountTypeService.listDiscountTypes();

    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), allTypes.size());
    List<DiscountType> pageContent = start < allTypes.size() ? allTypes.subList(start, end) : List.of();

    List<DiscountTypeResponse> dtos = pageContent.stream()
        .map(discountTypeMapper::toResponse)
        .toList();

    Page<DiscountTypeResponse> responsePage = new PageImpl<>(dtos, pageable, allTypes.size());
    PageMeta pageMeta = PageMeta.from(responsePage);

    return ResponseEntity.ok(ApiResponseV2.ok(dtos, pageMeta));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get discount type by ID", description = "Retrieve a specific discount type")
  public ResponseEntity<ApiResponseV2<DiscountTypeResponse>> getDiscountType(@PathVariable @Positive Long id) {
    DiscountType type = discountTypeService.listDiscountType(id);
    DiscountTypeResponse dto = discountTypeMapper.toResponse(type);
    return ResponseEntity.ok(ApiResponseV2.ok(dto));
  }
}
