/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.http.dto.request.CouponRequest;
import com.xplaza.backend.http.dto.response.CouponResponse;
import com.xplaza.backend.mapper.CouponMapper;
import com.xplaza.backend.service.CouponService;
import com.xplaza.backend.service.entity.Coupon;

@Deprecated(since = "1.0", forRemoval = true)
@RestController
@RequestMapping("/api/v1/coupons")
public class CouponController extends BaseController {
  private final CouponService couponService;
  private final CouponMapper couponMapper;
  private final ObjectMapper objectMapper;

  @Autowired
  public CouponController(CouponService couponService, CouponMapper couponMapper, ObjectMapper objectMapper) {
    this.couponService = couponService;
    this.couponMapper = couponMapper;
    this.objectMapper = objectMapper;
  }

  @GetMapping
  public ResponseEntity<ApiResponse> getCoupons(@RequestParam(value = "user_id") @Valid Long userId)
      throws JsonProcessingException {
    long startTime = System.currentTimeMillis();
    List<Coupon> entities = couponService.listCoupons();
    List<CouponResponse> dtos = entities.stream().map(couponMapper::toResponse).toList();
    long responseTime = System.currentTimeMillis() - startTime;

    return ResponseEntity.ok(new ApiResponse(
        responseTime,
        "Coupon List",
        HttpStatus.OK.value(),
        "Success",
        "",
        objectMapper.writeValueAsString(dtos)));
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse> getCouponDetails(@PathVariable @Valid Long id) throws JsonProcessingException {
    long startTime = System.currentTimeMillis();
    Coupon entity = couponService.listCoupon(id);
    CouponResponse dto = couponMapper.toResponse(entity);
    long responseTime = System.currentTimeMillis() - startTime;

    return ResponseEntity.ok(new ApiResponse(
        responseTime,
        "Coupon By ID",
        HttpStatus.OK.value(),
        "Success",
        "",
        objectMapper.writeValueAsString(dto)));
  }

  @PostMapping("/validate-coupon")
  public ResponseEntity<ApiResponse> validateCoupon(
      @RequestParam(value = "coupon_code") @Valid String couponCode,
      @RequestParam(value = "net_order_amount") @Valid Double netOrderAmount,
      @RequestParam(value = "shop_id") @Valid Long shopId) {
    long startTime = System.currentTimeMillis();

    if (!couponService.checkCouponValidity(couponCode, netOrderAmount, shopId)) {
      long responseTime = System.currentTimeMillis() - startTime;
      return new ResponseEntity<>(new ApiResponse(responseTime, "Validate Coupon", HttpStatus.FORBIDDEN.value(),
          "Error", "Coupon is not valid!", null), HttpStatus.FORBIDDEN);
    }

    Double couponAmount = couponService.calculateCouponAmount(couponCode, netOrderAmount);
    long responseTime = System.currentTimeMillis() - startTime;

    return new ResponseEntity<>(new ApiResponse(responseTime, "Validate Coupon", HttpStatus.OK.value(), "Success",
        "Coupon is valid.", couponAmount.toString()), HttpStatus.OK);
  }

  @PostMapping
  public ResponseEntity<ApiResponse> addCoupon(@RequestBody @Valid CouponRequest couponRequest) {
    long startTime = System.currentTimeMillis();
    Coupon entity = couponMapper.toEntity(couponRequest);
    couponService.addCoupon(entity);
    long responseTime = System.currentTimeMillis() - startTime;

    return new ResponseEntity<>(new ApiResponse(responseTime, "Add Coupon", HttpStatus.CREATED.value(),
        "Success", "Coupon has been created.", null), HttpStatus.CREATED);
  }

  @PutMapping
  public ResponseEntity<ApiResponse> updateCoupon(@RequestBody @Valid CouponRequest couponRequest) {
    long startTime = System.currentTimeMillis();
    Coupon entity = couponMapper.toEntity(couponRequest);
    couponService.updateCoupon(entity);
    long responseTime = System.currentTimeMillis() - startTime;

    return new ResponseEntity<>(new ApiResponse(responseTime, "Update Coupon", HttpStatus.OK.value(),
        "Success", "Coupon has been updated.", null), HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse> deleteCoupon(@PathVariable @Valid Long id) {
    long startTime = System.currentTimeMillis();
    couponService.deleteCoupon(id);
    long responseTime = System.currentTimeMillis() - startTime;

    return new ResponseEntity<>(new ApiResponse(responseTime, "Delete Coupon", HttpStatus.OK.value(),
        "Success", "Coupon has been deleted.", null), HttpStatus.OK);
  }
}
