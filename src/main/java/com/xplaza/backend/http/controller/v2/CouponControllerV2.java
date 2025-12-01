/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller.v2;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
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
import com.xplaza.backend.http.dto.request.CouponRequest;
import com.xplaza.backend.http.dto.response.CouponResponse;
import com.xplaza.backend.mapper.CouponMapper;
import com.xplaza.backend.service.CouponService;
import com.xplaza.backend.service.entity.Coupon;

/**
 * V2 Coupon Controller - Clean REST API design.
 *
 * Endpoints: - GET /api/v2/coupons - List all coupons with pagination - GET
 * /api/v2/coupons/{id} - Get single coupon - POST /api/v2/coupons - Create
 * coupon - PUT /api/v2/coupons/{id} - Update coupon - DELETE
 * /api/v2/coupons/{id} - Delete coupon - POST /api/v2/coupons/validate -
 * Validate coupon code
 */
@RestController
@RequestMapping("/api/v2/coupons")
@RequiredArgsConstructor
@Validated
@Tag(name = "Coupon Management V2", description = "V2 APIs for managing coupons with pagination")
public class CouponControllerV2 {

  private final CouponService couponService;
  private final CouponMapper couponMapper;

  @GetMapping
  @Operation(summary = "List coupons", description = "Get paginated list of coupons")
  public ResponseEntity<ApiResponseV2<List<CouponResponse>>> getCoupons(
      @RequestParam(required = false) Long shopId,
      @RequestParam(required = false) Boolean active,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) int size,
      @RequestParam(defaultValue = "couponId") String sort,
      @RequestParam(defaultValue = "DESC") Sort.Direction direction) {

    size = Math.min(size, 100);
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

    // Get all coupons (service doesn't have paginated methods yet)
    List<Coupon> allCoupons = couponService.listCoupons();

    // Filter by active status if provided
    if (active != null) {
      allCoupons = allCoupons.stream()
          .filter(c -> active.equals(c.getIsActive()))
          .toList();
    }

    // Filter by shop if provided
    if (shopId != null) {
      allCoupons = allCoupons.stream()
          .filter(c -> c.getCouponShopLinks() != null &&
              c.getCouponShopLinks().stream()
                  .anyMatch(link -> shopId.equals(link.getShop().getShopId())))
          .toList();
    }

    // Paginate
    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), allCoupons.size());
    List<Coupon> pageContent = start < allCoupons.size() ? allCoupons.subList(start, end) : List.of();

    List<CouponResponse> dtos = pageContent.stream()
        .map(couponMapper::toResponse)
        .toList();

    Page<CouponResponse> responsePage = new PageImpl<>(dtos, pageable, allCoupons.size());
    PageMeta pageMeta = PageMeta.from(responsePage);

    return ResponseEntity.ok(ApiResponseV2.ok(dtos, pageMeta));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get coupon by ID", description = "Retrieve a specific coupon by its ID")
  public ResponseEntity<ApiResponseV2<CouponResponse>> getCoupon(@PathVariable @Positive Long id) {
    Coupon coupon = couponService.listCoupon(id);
    if (coupon == null) {
      return ResponseEntity.status(HttpStatus.NOT_FOUND)
          .body(ApiResponseV2.error("NOT_FOUND", "Coupon not found with id: " + id));
    }
    CouponResponse dto = couponMapper.toResponse(coupon);
    return ResponseEntity.ok(ApiResponseV2.ok(dto));
  }

  @PostMapping
  @Operation(summary = "Create coupon", description = "Create a new coupon")
  public ResponseEntity<ApiResponseV2<Void>> createCoupon(@RequestBody @Valid CouponRequest request) {
    Coupon entity = couponMapper.toEntity(request);
    couponService.addCoupon(entity);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseV2.ok("Coupon created successfully"));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update coupon", description = "Update an existing coupon by ID")
  public ResponseEntity<ApiResponseV2<Void>> updateCoupon(
      @PathVariable @Positive Long id,
      @RequestBody @Valid CouponRequest request) {
    Coupon entity = couponMapper.toEntity(request);
    entity.setCouponId(id);
    couponService.updateCoupon(entity);
    return ResponseEntity.ok(ApiResponseV2.ok("Coupon updated successfully"));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete coupon", description = "Delete a coupon by ID")
  public ResponseEntity<ApiResponseV2<Void>> deleteCoupon(@PathVariable @Positive Long id) {
    couponService.deleteCoupon(id);
    return ResponseEntity.ok(ApiResponseV2.ok("Coupon has been deleted"));
  }

  /**
   * POST /api/v2/coupons/validate
   *
   * Validate a coupon code and calculate discount.
   */
  @PostMapping("/validate")
  @Operation(summary = "Validate coupon", description = "Validate a coupon code and get discount amount")
  public ResponseEntity<ApiResponseV2<CouponValidationResult>> validateCoupon(
      @RequestParam @NotBlank String code,
      @RequestParam @Positive Double orderAmount,
      @RequestParam @Positive Long shopId) {

    boolean isValid = couponService.checkCouponValidity(code, orderAmount, shopId);

    if (!isValid) {
      return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY)
          .body(ApiResponseV2.error("INVALID_COUPON", "Coupon is not valid for this order"));
    }

    Double discountAmount = couponService.calculateCouponAmount(code, orderAmount);

    CouponValidationResult result = new CouponValidationResult(
        true,
        code,
        discountAmount,
        orderAmount - discountAmount);

    return ResponseEntity.ok(ApiResponseV2.ok(result));
  }

  /**
   * Response record for coupon validation.
   */
  public record CouponValidationResult(
      boolean valid,
      String code,
      Double discountAmount,
      Double finalAmount
  ) {
  }
}
