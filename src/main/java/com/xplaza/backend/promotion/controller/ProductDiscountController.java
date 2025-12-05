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
import com.xplaza.backend.promotion.domain.entity.ProductDiscount;
import com.xplaza.backend.promotion.dto.request.ProductDiscountRequest;
import com.xplaza.backend.promotion.dto.response.ProductDiscountResponse;
import com.xplaza.backend.promotion.mapper.ProductDiscountMapper;
import com.xplaza.backend.promotion.service.ProductDiscountService;

@RestController
@RequestMapping("/api/v1/product-discounts")
@RequiredArgsConstructor
@Tag(name = "Product Discount Management", description = "APIs for managing product discounts")
public class ProductDiscountController {
  private final ProductDiscountService productDiscountService;
  private final ProductDiscountMapper productDiscountMapper;

  @GetMapping
  @Operation(summary = "List all product discounts")
  public ResponseEntity<ApiResponse<List<ProductDiscountResponse>>> listProductDiscounts(
      @RequestParam(required = false) Long productId,
      @RequestParam(required = false, defaultValue = "false") Boolean activeOnly) {
    List<ProductDiscount> discounts;
    if (productId != null) {
      discounts = productDiscountService.listProductDiscountsByProduct(productId);
    } else if (activeOnly) {
      discounts = productDiscountService.listActiveProductDiscounts();
    } else {
      discounts = productDiscountService.listProductDiscounts();
    }
    List<ProductDiscountResponse> response = discounts.stream()
        .map(productDiscountMapper::toResponse).toList();
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get product discount by ID")
  public ResponseEntity<ApiResponse<ProductDiscountResponse>> getProductDiscount(@PathVariable Long id) {
    ProductDiscount discount = productDiscountService.getProductDiscount(id);
    return ResponseEntity.ok(ApiResponse.ok(productDiscountMapper.toResponse(discount)));
  }

  @PostMapping
  @Operation(summary = "Create a new product discount")
  public ResponseEntity<ApiResponse<ProductDiscountResponse>> createProductDiscount(
      @RequestBody @Valid ProductDiscountRequest request) {
    ProductDiscount entity = productDiscountMapper.toEntity(request);
    ProductDiscount saved = productDiscountService.createProductDiscount(entity, request.getProductId(),
        request.getDiscountTypeId());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created(productDiscountMapper.toResponse(saved)));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update an existing product discount")
  public ResponseEntity<ApiResponse<ProductDiscountResponse>> updateProductDiscount(@PathVariable Long id,
      @RequestBody @Valid ProductDiscountRequest request) {
    ProductDiscount details = productDiscountMapper.toEntity(request);
    ProductDiscount updated = productDiscountService.updateProductDiscount(id, details);
    return ResponseEntity.ok(ApiResponse.ok(productDiscountMapper.toResponse(updated)));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a product discount")
  public ResponseEntity<ApiResponse<Void>> deleteProductDiscount(@PathVariable Long id) {
    productDiscountService.deleteProductDiscount(id);
    return ResponseEntity.ok(ApiResponse.ok("Product discount deleted"));
  }
}
