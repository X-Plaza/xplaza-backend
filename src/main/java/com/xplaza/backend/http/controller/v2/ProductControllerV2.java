/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller.v2;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.common.util.ApiResponseV2;
import com.xplaza.backend.common.util.ApiResponseV2.PageMeta;
import com.xplaza.backend.http.dto.request.ProductRequest;
import com.xplaza.backend.http.dto.response.ProductResponse;
import com.xplaza.backend.mapper.ProductMapper;
import com.xplaza.backend.service.ProductService;
import com.xplaza.backend.service.entity.Product;

/**
 * V2 Product Controller - Clean REST API design.
 * 
 * Key improvements over V1: - Uses query parameters for filtering instead of
 * separate endpoints - Proper pagination support - Let Spring handle JSON
 * serialization (no manual ObjectMapper) - Consistent response structure via
 * ApiResponseV2 - Proper HTTP status codes - Validation annotations
 */
@RestController
@RequestMapping("/api/v2/products")
@RequiredArgsConstructor
@Validated
public class ProductControllerV2 {

  private final ProductService productService;
  private final ProductMapper productMapper;

  /**
   * GET /api/v2/products
   * 
   * Unified product listing with optional filters and pagination.
   * 
   * Query Parameters: - shopId: Filter by shop (optional) - categoryId: Filter by
   * category (optional) - brandId: Filter by brand (optional) - search: Search by
   * product name (optional) - page: Page number (0-indexed, default: 0) - size:
   * Page size (default: 20, max: 100) - sort: Sort field (default: productId) -
   * direction: Sort direction (ASC/DESC, default: ASC)
   */
  @GetMapping
  public ResponseEntity<ApiResponseV2<List<ProductResponse>>> getProducts(
      @RequestParam(required = false) Long shopId,
      @RequestParam(required = false) Long categoryId,
      @RequestParam(required = false) Long brandId,
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) int size,
      @RequestParam(defaultValue = "productId") String sort,
      @RequestParam(defaultValue = "ASC") Sort.Direction direction) {

    // Cap page size to prevent abuse
    size = Math.min(size, 100);
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

    // Use appropriate service method based on filters
    Page<Product> productPage;
    if (search != null && !search.isBlank()) {
      productPage = productService.searchProductsByName(search.trim(), pageable);
    } else if (shopId != null && categoryId != null) {
      productPage = productService.findProductsByShopAndCategory(shopId, categoryId, pageable);
    } else if (shopId != null) {
      productPage = productService.findProductsByShop(shopId, pageable);
    } else if (categoryId != null) {
      productPage = productService.findProductsByCategory(categoryId, pageable);
    } else if (brandId != null) {
      productPage = productService.findProductsByBrand(brandId, pageable);
    } else {
      productPage = productService.findProducts(pageable);
    }

    List<ProductResponse> dtos = productPage.getContent().stream()
        .map(productMapper::toResponse)
        .toList();

    PageMeta pageMeta = PageMeta.from(productPage);

    return ResponseEntity.ok(ApiResponseV2.ok(dtos, pageMeta));
  }

  /**
   * GET /api/v2/products/{id}
   * 
   * Get a single product by ID.
   */
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponseV2<ProductResponse>> getProduct(
      @PathVariable @Positive Long id) {

    Product product = productService.listProduct(id);
    ProductResponse dto = productMapper.toResponse(product);

    return ResponseEntity.ok(ApiResponseV2.ok(dto));
  }

  /**
   * POST /api/v2/products
   * 
   * Create a new product.
   */
  @PostMapping
  public ResponseEntity<ApiResponseV2<ProductResponse>> createProduct(
      @RequestBody @Valid ProductRequest request) {

    Product entity = productMapper.toEntity(request);
    Product saved = productService.addProduct(entity);
    ProductResponse dto = productMapper.toResponse(saved);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponseV2.created(dto));
  }

  /**
   * PUT /api/v2/products/{id}
   * 
   * Update an existing product.
   */
  @PutMapping("/{id}")
  public ResponseEntity<ApiResponseV2<ProductResponse>> updateProduct(
      @PathVariable @Positive Long id,
      @RequestBody @Valid ProductRequest request) {

    // Set the ID from path to ensure we update the right resource
    request.setProductId(id);

    Product entity = productMapper.toEntity(request);
    Product updated = productService.updateProduct(entity);
    ProductResponse dto = productMapper.toResponse(updated);

    return ResponseEntity.ok(ApiResponseV2.ok(dto));
  }

  /**
   * DELETE /api/v2/products/{id}
   * 
   * Delete a product.
   */
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponseV2<Void>> deleteProduct(
      @PathVariable @Positive Long id) {

    String productName = productService.getProductNameByID(id);
    productService.deleteProduct(id);

    return ResponseEntity.ok(ApiResponseV2.ok(productName + " has been deleted"));
  }

  /**
   * PATCH /api/v2/products/{id}/inventory
   * 
   * Update product inventory (partial update).
   */
  @PatchMapping("/{id}/inventory")
  public ResponseEntity<ApiResponseV2<Void>> updateInventory(
      @PathVariable @Positive Long id,
      @RequestParam @Min(0) int quantity) {

    productService.updateProductInventory(id, quantity);

    return ResponseEntity.ok(ApiResponseV2.ok("Inventory updated"));
  }
}
