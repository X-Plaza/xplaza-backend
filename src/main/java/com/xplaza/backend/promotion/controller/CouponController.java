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
import com.xplaza.backend.promotion.domain.entity.Coupon;
import com.xplaza.backend.promotion.dto.request.CouponRequest;
import com.xplaza.backend.promotion.dto.response.CouponResponse;
import com.xplaza.backend.promotion.mapper.CouponMapper;
import com.xplaza.backend.promotion.service.CouponService;

@RestController
@RequestMapping("/api/v1/coupons")
@RequiredArgsConstructor
@Tag(name = "Coupon Management", description = "APIs for managing coupons")
public class CouponController {
  private final CouponService couponService;
  private final CouponMapper couponMapper;

  @GetMapping
  @Operation(summary = "List all coupons")
  public ResponseEntity<ApiResponse<List<CouponResponse>>> listCoupons(
      @RequestParam(required = false, defaultValue = "false") Boolean activeOnly) {
    List<Coupon> coupons = activeOnly ? couponService.listActiveCoupons() : couponService.listCoupons();
    List<CouponResponse> response = coupons.stream().map(couponMapper::toResponse).toList();
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get coupon by ID")
  public ResponseEntity<ApiResponse<CouponResponse>> getCoupon(@PathVariable Long id) {
    Coupon coupon = couponService.getCoupon(id);
    return ResponseEntity.ok(ApiResponse.ok(couponMapper.toResponse(coupon)));
  }

  @GetMapping("/code/{code}")
  @Operation(summary = "Get coupon by code")
  public ResponseEntity<ApiResponse<CouponResponse>> getCouponByCode(@PathVariable String code) {
    Coupon coupon = couponService.getCouponByCode(code);
    return ResponseEntity.ok(ApiResponse.ok(couponMapper.toResponse(coupon)));
  }

  @PostMapping
  @Operation(summary = "Create a new coupon")
  public ResponseEntity<ApiResponse<CouponResponse>> createCoupon(@RequestBody @Valid CouponRequest request) {
    Coupon entity = couponMapper.toEntity(request);
    Coupon saved = couponService.createCoupon(entity);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(couponMapper.toResponse(saved)));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update an existing coupon")
  public ResponseEntity<ApiResponse<CouponResponse>> updateCoupon(@PathVariable Long id,
      @RequestBody @Valid CouponRequest request) {
    Coupon details = couponMapper.toEntity(request);
    Coupon updated = couponService.updateCoupon(id, details);
    return ResponseEntity.ok(ApiResponse.ok(couponMapper.toResponse(updated)));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a coupon")
  public ResponseEntity<ApiResponse<Void>> deleteCoupon(@PathVariable Long id) {
    couponService.deleteCoupon(id);
    return ResponseEntity.ok(ApiResponse.ok("Coupon deleted"));
  }
}
