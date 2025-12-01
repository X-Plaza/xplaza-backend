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
import com.xplaza.backend.domain.ProductVariationType;
import com.xplaza.backend.http.dto.response.ProductVariationTypeResponse;
import com.xplaza.backend.mapper.ProductVariationTypeMapper;
import com.xplaza.backend.service.ProductVariationTypeService;

/**
 * Product Variation Type Controller - Clean REST API design.
 */
@RestController
@RequestMapping("/api/v1/product-variation-types")
@RequiredArgsConstructor
@Validated
@Tag(name = "Product Variation Type Management", description = "APIs for managing product variation types")
public class ProductVariationTypeController {

  private final ProductVariationTypeService productVariationTypeService;
  private final ProductVariationTypeMapper productVariationTypeMapper;

  @GetMapping
  @Operation(summary = "List product variation types", description = "Get paginated list of product variation types")
  public ResponseEntity<ApiResponse<List<ProductVariationTypeResponse>>> getProductVariationTypes(
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) int size,
      @RequestParam(defaultValue = "productVariationTypeId") String sort,
      @RequestParam(defaultValue = "ASC") Sort.Direction direction) {

    size = Math.min(size, 100);
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

    List<ProductVariationType> allTypes = productVariationTypeService.listProductVariationTypes();

    if (search != null && !search.isBlank()) {
      String searchLower = search.toLowerCase().trim();
      allTypes = allTypes.stream()
          .filter(t -> t.getVarTypeName() != null
              && t.getVarTypeName().toLowerCase().contains(searchLower))
          .toList();
    }

    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), allTypes.size());
    List<ProductVariationType> pageContent = start < allTypes.size() ? allTypes.subList(start, end) : List.of();

    List<ProductVariationTypeResponse> dtos = pageContent.stream()
        .map(productVariationTypeMapper::toResponse)
        .toList();

    Page<ProductVariationTypeResponse> responsePage = new PageImpl<>(dtos, pageable, allTypes.size());
    PageMeta pageMeta = PageMeta.from(responsePage);

    return ResponseEntity.ok(ApiResponse.ok(dtos, pageMeta));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get product variation type by ID", description = "Retrieve a specific product variation type")
  public ResponseEntity<ApiResponse<ProductVariationTypeResponse>> getProductVariationType(
      @PathVariable @Positive Long id) {
    ProductVariationType type = productVariationTypeService.listProductVariationType(id);
    ProductVariationTypeResponse dto = productVariationTypeMapper.toResponse(type);
    return ResponseEntity.ok(ApiResponse.ok(dto));
  }

  @PostMapping
  @Operation(summary = "Create product variation type", description = "Create a new product variation type")
  public ResponseEntity<ApiResponse<ProductVariationTypeResponse>> createProductVariationType(
      @RequestBody @Valid ProductVariationType productVariationType) {
    ProductVariationType created = productVariationTypeService.addProductVariationType(productVariationType);
    ProductVariationTypeResponse dto = productVariationTypeMapper.toResponse(created);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(dto));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update product variation type", description = "Update an existing product variation type")
  public ResponseEntity<ApiResponse<ProductVariationTypeResponse>> updateProductVariationType(
      @PathVariable @Positive Long id,
      @RequestBody @Valid ProductVariationType productVariationType) {
    productVariationType.setProductVarTypeId(id);
    ProductVariationType updated = productVariationTypeService.updateProductVariationType(productVariationType);
    ProductVariationTypeResponse dto = productVariationTypeMapper.toResponse(updated);
    return ResponseEntity.ok(ApiResponse.ok(dto));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete product variation type", description = "Delete a product variation type")
  public ResponseEntity<ApiResponse<Void>> deleteProductVariationType(@PathVariable @Positive Long id) {
    productVariationTypeService.deleteProductVariationType(id);
    return ResponseEntity.ok(ApiResponse.ok("Product variation type deleted successfully"));
  }
}
