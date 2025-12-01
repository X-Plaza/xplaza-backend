/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller.v2;

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

import com.xplaza.backend.common.util.ApiResponseV2;
import com.xplaza.backend.common.util.ApiResponseV2.PageMeta;
import com.xplaza.backend.http.dto.response.ProductImageResponse;
import com.xplaza.backend.mapper.ProductImageMapper;
import com.xplaza.backend.service.ProductImageService;
import com.xplaza.backend.service.entity.ProductImage;

/**
 * V2 Product Image Controller - Clean REST API design.
 */
@RestController
@RequestMapping("/api/v2/product-images")
@RequiredArgsConstructor
@Validated
@Tag(name = "Product Image Management V2", description = "V2 APIs for managing product images")
public class ProductImageControllerV2 {

  private final ProductImageService productImageService;
  private final ProductImageMapper productImageMapper;

  @GetMapping
  @Operation(summary = "List product images", description = "Get paginated list of product images")
  public ResponseEntity<ApiResponseV2<List<ProductImageResponse>>> getProductImages(
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

    return ResponseEntity.ok(ApiResponseV2.ok(dtos, pageMeta));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get product image by ID", description = "Retrieve a specific product image")
  public ResponseEntity<ApiResponseV2<ProductImageResponse>> getProductImage(@PathVariable @Positive Long id) {
    ProductImage image = productImageService.listProductImage(id);
    ProductImageResponse dto = productImageMapper.toResponse(image);
    return ResponseEntity.ok(ApiResponseV2.ok(dto));
  }

  @PostMapping
  @Operation(summary = "Create product image", description = "Create a new product image")
  public ResponseEntity<ApiResponseV2<ProductImageResponse>> createProductImage(
      @RequestBody @Valid ProductImage productImage) {
    ProductImage created = productImageService.addProductImage(productImage);
    ProductImageResponse dto = productImageMapper.toResponse(created);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseV2.created(dto));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete product image", description = "Delete a product image")
  public ResponseEntity<ApiResponseV2<Void>> deleteProductImage(@PathVariable @Positive Long id) {
    productImageService.deleteProductImage(id);
    return ResponseEntity.ok(ApiResponseV2.ok("Product image deleted successfully"));
  }
}
