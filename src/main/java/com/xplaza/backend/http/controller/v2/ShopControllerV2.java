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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.common.util.ApiResponseV2;
import com.xplaza.backend.common.util.ApiResponseV2.PageMeta;
import com.xplaza.backend.http.dto.request.ShopRequest;
import com.xplaza.backend.http.dto.response.ShopResponse;
import com.xplaza.backend.mapper.ShopMapper;
import com.xplaza.backend.service.ShopService;
import com.xplaza.backend.service.entity.Shop;

/**
 * V2 Shop Controller - Clean REST API design.
 * 
 * Endpoints: - GET /api/v2/shops - List all shops with pagination and filters -
 * GET /api/v2/shops/{id} - Get single shop - POST /api/v2/shops - Create shop -
 * PUT /api/v2/shops/{id} - Update shop - DELETE /api/v2/shops/{id} - Delete
 * shop
 */
@RestController
@RequestMapping("/api/v2/shops")
@RequiredArgsConstructor
@Validated
@Tag(name = "Shop Management V2", description = "V2 APIs for managing shops with pagination")
public class ShopControllerV2 {

  private final ShopService shopService;
  private final ShopMapper shopMapper;

  /**
   * GET /api/v2/shops
   * 
   * List shops with pagination and optional filters.
   * 
   * Query Parameters: - locationId: Filter by location (optional) - ownerId:
   * Filter by owner (optional) - search: Search by shop name (optional) - page,
   * size, sort, direction: Pagination params
   */
  @GetMapping
  @Operation(summary = "List shops", description = "Get paginated list of shops with optional filters")
  public ResponseEntity<ApiResponseV2<List<ShopResponse>>> getShops(
      @RequestParam(required = false) Long locationId,
      @RequestParam(required = false) Long ownerId,
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) int size,
      @RequestParam(defaultValue = "shopId") String sort,
      @RequestParam(defaultValue = "ASC") Sort.Direction direction) {

    size = Math.min(size, 100);
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

    Page<Shop> shopPage;
    if (search != null && !search.isBlank()) {
      shopPage = shopService.searchShops(search.trim(), pageable);
    } else if (locationId != null) {
      shopPage = shopService.listShopsByLocationPaginated(locationId, pageable);
    } else if (ownerId != null) {
      shopPage = shopService.listShopsByOwnerPaginated(ownerId, pageable);
    } else {
      shopPage = shopService.listShopsPaginated(pageable);
    }

    List<ShopResponse> dtos = shopPage.getContent().stream()
        .map(shopMapper::toResponse)
        .toList();

    PageMeta pageMeta = PageMeta.from(shopPage);

    return ResponseEntity.ok(ApiResponseV2.ok(dtos, pageMeta));
  }

  /**
   * GET /api/v2/shops/{id}
   * 
   * Get single shop by ID.
   */
  @GetMapping("/{id}")
  @Operation(summary = "Get shop by ID", description = "Retrieve a specific shop by its ID")
  public ResponseEntity<ApiResponseV2<ShopResponse>> getShop(
      @PathVariable @Positive Long id) {

    Shop shop = shopService.listShop(id);
    ShopResponse dto = shopMapper.toResponse(shop);

    return ResponseEntity.ok(ApiResponseV2.ok(dto));
  }

  /**
   * POST /api/v2/shops
   * 
   * Create a new shop.
   */
  @PostMapping
  @Operation(summary = "Create shop", description = "Create a new shop")
  public ResponseEntity<ApiResponseV2<ShopResponse>> createShop(
      @RequestBody @Valid ShopRequest request) {

    Shop entity = shopMapper.toEntity(request);
    Shop saved = shopService.addShop(entity);
    ShopResponse dto = shopMapper.toResponse(saved);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponseV2.created(dto));
  }

  /**
   * PUT /api/v2/shops/{id}
   * 
   * Update an existing shop.
   */
  @PutMapping("/{id}")
  @Operation(summary = "Update shop", description = "Update an existing shop by ID")
  public ResponseEntity<ApiResponseV2<ShopResponse>> updateShop(
      @PathVariable @Positive Long id,
      @RequestBody @Valid ShopRequest request) {

    Shop entity = shopMapper.toEntity(request);
    Shop updated = shopService.updateShop(id, entity);
    ShopResponse dto = shopMapper.toResponse(updated);

    return ResponseEntity.ok(ApiResponseV2.ok(dto));
  }

  /**
   * DELETE /api/v2/shops/{id}
   * 
   * Delete a shop.
   */
  @DeleteMapping("/{id}")
  @Operation(summary = "Delete shop", description = "Delete a shop by ID")
  public ResponseEntity<ApiResponseV2<Void>> deleteShop(
      @PathVariable @Positive Long id) {

    shopService.deleteShop(id);

    return ResponseEntity.ok(ApiResponseV2.ok("Shop has been deleted"));
  }
}
