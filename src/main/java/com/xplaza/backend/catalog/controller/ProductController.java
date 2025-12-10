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
import org.springframework.web.multipart.MultipartFile;

import com.xplaza.backend.catalog.domain.entity.Product;
import com.xplaza.backend.catalog.dto.request.ProductRequest;
import com.xplaza.backend.catalog.dto.response.ProductResponse;
import com.xplaza.backend.catalog.mapper.ProductMapper;
import com.xplaza.backend.catalog.service.ProductService;
import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.common.util.ApiResponse.PageMeta;

/**
 * Product Controller - Clean REST API design.
 * 
 * Key improvements over V1: - Uses query parameters for filtering instead of
 * separate endpoints - Proper pagination support - Let Spring handle JSON
 * serialization (no manual ObjectMapper) - Consistent response structure via
 * ApiResponse - Proper HTTP status codes - Validation annotations
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Validated
@Tag(name = "Product Management", description = "APIs for managing products with pagination and filtering")
public class ProductController {

  private final ProductService productService;
  private final ProductMapper productMapper;

  /**
   * GET /api/v1/products
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
  @Operation(summary = "List products", description = "Get paginated list of products with optional filters for shop, category, brand, and search")
  public ResponseEntity<ApiResponse<List<ProductResponse>>> getProducts(
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

    return ResponseEntity.ok(ApiResponse.ok(dtos, pageMeta));
  }

  /**
   * GET /api/v1/products/{id}
   * 
   * Get a single product by ID.
   */
  @GetMapping("/{id}")
  @Operation(summary = "Get product by ID", description = "Retrieve a specific product by its ID")
  public ResponseEntity<ApiResponse<ProductResponse>> getProduct(
      @PathVariable @Positive Long id) {

    Product product = productService.listProduct(id);
    ProductResponse dto = productMapper.toResponse(product);

    return ResponseEntity.ok(ApiResponse.ok(dto));
  }

  /**
   * POST /api/v1/products
   * 
   * Create a new product.
   */
  @PostMapping
  @Operation(summary = "Create product", description = "Create a new product")
  public ResponseEntity<ApiResponse<ProductResponse>> createProduct(
      @RequestBody @Valid ProductRequest request) {

    Product entity = productMapper.toEntity(request);
    Product saved = productService.addProduct(entity);
    ProductResponse dto = productMapper.toResponse(saved);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponse.created(dto));
  }

  /**
   * PUT /api/v1/products/{id}
   * 
   * Update an existing product.
   */
  @PutMapping("/{id}")
  @Operation(summary = "Update product", description = "Update an existing product by ID")
  public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(
      @PathVariable @Positive Long id,
      @RequestBody @Valid ProductRequest request) {

    // Set the ID from path to ensure we update the right resource
    request.setProductId(id);

    Product entity = productMapper.toEntity(request);
    Product updated = productService.updateProduct(entity);
    ProductResponse dto = productMapper.toResponse(updated);

    return ResponseEntity.ok(ApiResponse.ok(dto));
  }

  /**
   * DELETE /api/v1/products/{id}
   * 
   * Delete a product.
   */
  @DeleteMapping("/{id}")
  @Operation(summary = "Delete product", description = "Delete a product by ID")
  public ResponseEntity<ApiResponse<Void>> deleteProduct(
      @PathVariable @Positive Long id) {

    String productName = productService.getProductNameByID(id);
    productService.deleteProduct(id);

    return ResponseEntity.ok(ApiResponse.ok(productName + " has been deleted"));
  }

  /**
   * PATCH /api/v1/products/{id}/inventory
   * 
   * Update product inventory (partial update).
   */
  @PatchMapping("/{id}/inventory")
  @Operation(summary = "Update product inventory", description = "Update product inventory quantity")
  public ResponseEntity<ApiResponse<Void>> updateInventory(
      @PathVariable @Positive Long id,
      @RequestParam @Min(0) int quantity) {

    productService.updateProductInventory(id, quantity);

    return ResponseEntity.ok(ApiResponse.ok("Inventory updated"));
  }

  /**
   * POST /api/v1/products/{id}/images
   * 
   * Upload product images.
   */
  @PostMapping(value = "/{id}/images", consumes = "multipart/form-data")
  @Operation(summary = "Upload product images", description = "Upload images for a product or variant (max 10 total)")
  public ResponseEntity<ApiResponse<List<String>>> uploadProductImages(
      @PathVariable Long id,
      @RequestParam(required = false) java.util.UUID variantId,
      @RequestParam("files") List<MultipartFile> files) {
    List<String> imageUrls = productService.uploadProductImages(id, variantId, files);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(imageUrls));
  }
}
