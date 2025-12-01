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
import com.xplaza.backend.domain.ProductImage;
import com.xplaza.backend.http.dto.response.ProductImageResponse;
import com.xplaza.backend.mapper.ProductImageMapper;
import com.xplaza.backend.service.ProductImageService;

/**
 * Product Image Controller - Clean REST API design.
 */
@RestController
@RequestMapping("/api/v1/product-images")
@RequiredArgsConstructor
@Validated
@Tag(name = "Product Image Management", description = "APIs for managing product images")
public class ProductImageController {

  private final ProductImageService productImageService;
  private final ProductImageMapper productImageMapper;

  @GetMapping
  @Operation(summary = "List product images", description = "Get paginated list of product images")
  public ResponseEntity<ApiResponse<List<ProductImageResponse>>> getProductImages(
      @RequestParam(required = false) Long productId,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "50") @Min(1) int size,
      @RequestParam(defaultValue = "productImageId") String sort,
      @RequestParam(defaultValue = "ASC") Sort.Direction direction) {

    size = Math.min(size, 200);
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

    List<ProductImage> allImages;
    if (productId != null) {
      allImages = productImageService.listProductImagesByProduct(productId);
    } else {
      allImages = productImageService.listProductImages();
    }

    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), allImages.size());
    List<ProductImage> pageContent = start < allImages.size() ? allImages.subList(start, end) : List.of();

    List<ProductImageResponse> dtos = pageContent.stream()
        .map(productImageMapper::toResponse)
        .toList();

    Page<ProductImageResponse> responsePage = new PageImpl<>(dtos, pageable, allImages.size());
    PageMeta pageMeta = PageMeta.from(responsePage);

    return ResponseEntity.ok(ApiResponse.ok(dtos, pageMeta));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get product image by ID", description = "Retrieve a specific product image")
  public ResponseEntity<ApiResponse<ProductImageResponse>> getProductImage(@PathVariable @Positive Long id) {
    ProductImage image = productImageService.listProductImage(id);
    ProductImageResponse dto = productImageMapper.toResponse(image);
    return ResponseEntity.ok(ApiResponse.ok(dto));
  }

  @PostMapping
  @Operation(summary = "Create product image", description = "Create a new product image")
  public ResponseEntity<ApiResponse<ProductImageResponse>> createProductImage(
      @RequestBody @Valid ProductImage productImage) {
    ProductImage created = productImageService.addProductImage(productImage);
    ProductImageResponse dto = productImageMapper.toResponse(created);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(dto));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete product image", description = "Delete a product image")
  public ResponseEntity<ApiResponse<Void>> deleteProductImage(@PathVariable @Positive Long id) {
    productImageService.deleteProductImage(id);
    return ResponseEntity.ok(ApiResponse.ok("Product image deleted successfully"));
  }
}
