/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.catalog.controller;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.catalog.domain.entity.Brand;
import com.xplaza.backend.catalog.dto.request.BrandRequest;
import com.xplaza.backend.catalog.dto.response.BrandResponse;
import com.xplaza.backend.catalog.mapper.BrandMapper;
import com.xplaza.backend.catalog.service.BrandService;
import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.common.util.ApiResponse.PageMeta;

/**
 * Brand Controller - Clean REST API design.
 * 
 * Endpoints: - GET /api/v1/brands - List all brands with pagination - GET
 * /api/v1/brands/{id} - Get single brand - POST /api/v1/brands - Create brand -
 * PUT /api/v1/brands/{id} - Update brand - DELETE /api/v1/brands/{id} - Delete
 * brand
 */
@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
@Validated
@Tag(name = "Brand Management", description = "APIs for managing product brands with pagination")
public class BrandController {

  private final BrandService brandService;
  private final BrandMapper brandMapper;

  /**
   * GET /api/v1/brands
   * 
   * List brands with pagination and optional search.
   */
  @GetMapping
  @Operation(summary = "List brands", description = "Get paginated list of brands with optional search")
  public ResponseEntity<ApiResponse<List<BrandResponse>>> getBrands(
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) int size,
      @RequestParam(defaultValue = "brandId") String sort,
      @RequestParam(defaultValue = "ASC") Sort.Direction direction) {

    size = Math.min(size, 100);
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

    Page<Brand> brandPage;
    if (search != null && !search.isBlank()) {
      brandPage = brandService.searchBrands(search.trim(), pageable);
    } else {
      brandPage = brandService.listBrandsPaginated(pageable);
    }

    List<BrandResponse> dtos = brandPage.getContent().stream()
        .map(brandMapper::toResponse)
        .toList();

    PageMeta pageMeta = PageMeta.from(brandPage);

    return ResponseEntity.ok(ApiResponse.ok(dtos, pageMeta));
  }

  /**
   * GET /api/v1/brands/{id}
   * 
   * Get single brand by ID.
   */
  @GetMapping("/{id}")
  @Operation(summary = "Get brand by ID", description = "Retrieve a specific brand by its ID")
  public ResponseEntity<ApiResponse<BrandResponse>> getBrand(
      @PathVariable @Positive Long id) {

    Brand brand = brandService.listBrand(id);
    BrandResponse dto = brandMapper.toResponse(brand);

    return ResponseEntity.ok(ApiResponse.ok(dto));
  }

  /**
   * POST /api/v1/brands
   * 
   * Create a new brand.
   */
  @PostMapping
  @Operation(summary = "Create brand", description = "Create a new brand")
  public ResponseEntity<ApiResponse<BrandResponse>> createBrand(
      @RequestBody @Valid BrandRequest request) {

    Brand entity = brandMapper.toEntity(request);
    Brand saved = brandService.addBrand(entity);
    BrandResponse dto = brandMapper.toResponse(saved);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponse.created(dto));
  }

  /**
   * PUT /api/v1/brands/{id}
   * 
   * Update an existing brand.
   */
  @PutMapping("/{id}")
  @Operation(summary = "Update brand", description = "Update an existing brand by ID")
  public ResponseEntity<ApiResponse<BrandResponse>> updateBrand(
      @PathVariable @Positive Long id,
      @RequestBody @Valid BrandRequest request) {

    Brand entity = brandMapper.toEntity(request);
    entity.setBrandId(id);
    Brand updated = brandService.updateBrand(entity);
    BrandResponse dto = brandMapper.toResponse(updated);

    return ResponseEntity.ok(ApiResponse.ok(dto));
  }

  /**
   * DELETE /api/v1/brands/{id}
   * 
   * Delete a brand.
   */
  @DeleteMapping("/{id}")
  @Operation(summary = "Delete brand", description = "Delete a brand by ID")
  public ResponseEntity<ApiResponse<Void>> deleteBrand(
      @PathVariable @Positive Long id) {

    String brandName = brandService.getBrandNameByID(id);
    brandService.deleteBrand(id);

    return ResponseEntity.ok(ApiResponse.ok(brandName + " has been deleted"));
  }
}
