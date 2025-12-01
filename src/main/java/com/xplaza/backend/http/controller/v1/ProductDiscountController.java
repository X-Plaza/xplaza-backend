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
import com.xplaza.backend.domain.ProductDiscount;
import com.xplaza.backend.http.dto.request.ProductDiscountRequest;
import com.xplaza.backend.http.dto.response.ProductDiscountResponse;
import com.xplaza.backend.mapper.ProductDiscountMapper;
import com.xplaza.backend.service.ProductDiscountService;

/**
 * Product Discount Controller - Clean REST API design.
 */
@RestController
@RequestMapping("/api/v1/product-discounts")
@RequiredArgsConstructor
@Validated
@Tag(name = "Product Discount Management", description = "APIs for managing product discounts")
public class ProductDiscountController {

  private final ProductDiscountService productDiscountService;
  private final ProductDiscountMapper productDiscountMapper;

  @GetMapping
  @Operation(summary = "List product discounts", description = "Get paginated list of product discounts")
  public ResponseEntity<ApiResponse<List<ProductDiscountResponse>>> getProductDiscounts(
      @RequestParam(required = false) Long productId,
      @RequestParam(required = false) Boolean active,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) int size,
      @RequestParam(defaultValue = "productDiscountId") String sort,
      @RequestParam(defaultValue = "DESC") Sort.Direction direction) {

    size = Math.min(size, 100);
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

    List<ProductDiscount> allDiscounts = productDiscountService.listProductDiscounts();

    // Filter by productId if provided
    if (productId != null) {
      allDiscounts = allDiscounts.stream()
          .filter(d -> d.getProduct() != null && productId.equals(d.getProduct().getProductId()))
          .toList();
    }

    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), allDiscounts.size());
    List<ProductDiscount> pageContent = start < allDiscounts.size() ? allDiscounts.subList(start, end) : List.of();

    List<ProductDiscountResponse> dtos = pageContent.stream()
        .map(productDiscountMapper::toResponse)
        .toList();

    Page<ProductDiscountResponse> responsePage = new PageImpl<>(dtos, pageable, allDiscounts.size());
    PageMeta pageMeta = PageMeta.from(responsePage);

    return ResponseEntity.ok(ApiResponse.ok(dtos, pageMeta));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get product discount by ID", description = "Retrieve a specific product discount")
  public ResponseEntity<ApiResponse<ProductDiscountResponse>> getProductDiscount(@PathVariable @Positive Long id) {
    ProductDiscount discount = productDiscountService.listProductDiscount(id);
    ProductDiscountResponse dto = productDiscountMapper.toResponse(discount);
    return ResponseEntity.ok(ApiResponse.ok(dto));
  }

  @PostMapping
  @Operation(summary = "Create product discount", description = "Create a new product discount")
  public ResponseEntity<ApiResponse<Void>> createProductDiscount(
      @RequestBody @Valid ProductDiscountRequest request) {
    ProductDiscount entity = productDiscountMapper.toEntity(request);

    if (!productDiscountService.checkDiscountValidity(entity)) {
      return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
          .body(ApiResponse.error("INVALID_DISCOUNT", "Discount cannot be greater than the original price"));
    }

    if (!productDiscountService.checkDiscountDateValidity(entity)) {
      return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
          .body(ApiResponse.error("INVALID_DATE", "Discount date is not valid"));
    }

    productDiscountService.addProductDiscount(entity);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Product discount created successfully"));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update product discount", description = "Update an existing product discount")
  public ResponseEntity<ApiResponse<Void>> updateProductDiscount(
      @PathVariable @Positive Long id,
      @RequestBody @Valid ProductDiscountRequest request) {
    ProductDiscount entity = productDiscountMapper.toEntity(request);

    if (!productDiscountService.checkDiscountValidity(entity)) {
      return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
          .body(ApiResponse.error("INVALID_DISCOUNT", "Discount cannot be greater than the original price"));
    }

    if (!productDiscountService.checkDiscountDateValidity(entity)) {
      return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
          .body(ApiResponse.error("INVALID_DATE", "Discount date is not valid"));
    }

    productDiscountService.updateProductDiscount(id, entity);
    return ResponseEntity.ok(ApiResponse.ok("Product discount updated successfully"));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete product discount", description = "Delete a product discount")
  public ResponseEntity<ApiResponse<Void>> deleteProductDiscount(@PathVariable @Positive Long id) {
    productDiscountService.deleteProductDiscount(id);
    return ResponseEntity.ok(ApiResponse.ok("Product discount deleted successfully"));
  }
}
