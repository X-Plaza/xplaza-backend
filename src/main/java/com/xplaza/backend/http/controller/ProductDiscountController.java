/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.http.dto.request.ProductDiscountRequest;
import com.xplaza.backend.http.dto.response.ProductDiscountResponse;
import com.xplaza.backend.mapper.ProductDiscountMapper;
import com.xplaza.backend.service.ProductDiscountService;
import com.xplaza.backend.service.RoleService;
import com.xplaza.backend.service.entity.ProductDiscount;

@RestController
@RequestMapping("/api/v1/product-discounts")
public class ProductDiscountController extends BaseController {
  private final ProductDiscountService productDiscountService;
  private final ProductDiscountMapper productDiscountMapper;

  @Autowired
  public ProductDiscountController(ProductDiscountService productDiscountService,
      ProductDiscountMapper productDiscountMapper) {
    this.productDiscountService = productDiscountService;
    this.productDiscountMapper = productDiscountMapper;
  }

  @Autowired
  private RoleService roleService;

  @GetMapping
  public ResponseEntity<ApiResponse> getProductDiscounts(
      @RequestParam(value = "user_id") @Valid Long user_id) throws JsonProcessingException {
    long start = System.currentTimeMillis();
    List<ProductDiscount> entities = productDiscountService.listProductDiscounts();
    List<ProductDiscountResponse> dtos = entities.stream()
        .map(productDiscountMapper::toResponse)
        .toList();
    long responseTime = System.currentTimeMillis() - start;
    String data = new ObjectMapper().writeValueAsString(dtos);
    ApiResponse response = new ApiResponse(responseTime, "Product Discount List", HttpStatus.OK.value(), "Success", "",
        data);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse> getProductDiscountByID(@PathVariable @Valid Long id)
      throws JsonProcessingException {
    long start = System.currentTimeMillis();
    ProductDiscount entity = productDiscountService.listProductDiscount(id);
    ProductDiscountResponse dto = productDiscountMapper.toResponse(entity);
    long responseTime = System.currentTimeMillis() - start;
    String data = new ObjectMapper().writeValueAsString(dto);
    ApiResponse response = new ApiResponse(responseTime, "Product Discount By ID", HttpStatus.OK.value(), "Success", "",
        data);
    return ResponseEntity.ok(response);
  }

  @PostMapping
  public ResponseEntity<ApiResponse> addProductDiscount(
      @RequestBody @Valid ProductDiscountRequest productDiscountRequest)
      throws JSONException {
    long start = System.currentTimeMillis();
    ProductDiscount entity = productDiscountMapper.toEntity(productDiscountRequest);
    if (!productDiscountService.checkDiscountValidity(entity)) {
      long responseTime = System.currentTimeMillis() - start;
      return new ResponseEntity<>(new ApiResponse(responseTime, "Add Product Discount", HttpStatus.FORBIDDEN.value(),
          "Error", "Discount cannot be greater than the original price!", null), HttpStatus.FORBIDDEN);
    }
    if (!productDiscountService.checkDiscountDateValidity(entity)) {
      long responseTime = System.currentTimeMillis() - start;
      return new ResponseEntity<>(new ApiResponse(responseTime, "Add Product Discount", HttpStatus.FORBIDDEN.value(),
          "Error", "Discount date is not valid! Please change discount date.", null), HttpStatus.FORBIDDEN);
    }
    productDiscountService.addProductDiscount(entity);
    long responseTime = System.currentTimeMillis() - start;
    return new ResponseEntity<>(new ApiResponse(responseTime, "Add Product Discount", HttpStatus.CREATED.value(),
        "Success", "Discount on product has been created.", null), HttpStatus.CREATED);
  }

  @PutMapping
  public ResponseEntity<ApiResponse> updateProductDiscount(
      @RequestParam Long id, @RequestBody @Valid ProductDiscountRequest productDiscountRequest) {
    long start = System.currentTimeMillis();
    ProductDiscount entity = productDiscountMapper.toEntity(productDiscountRequest);
    if (!productDiscountService.checkDiscountValidity(entity)) {
      long responseTime = System.currentTimeMillis() - start;
      return new ResponseEntity<>(new ApiResponse(responseTime, "Update Product Discount", HttpStatus.FORBIDDEN.value(),
          "Error", "Discount cannot be greater than the original price!", null), HttpStatus.FORBIDDEN);
    }
    if (!productDiscountService.checkDiscountDateValidity(entity)) {
      long responseTime = System.currentTimeMillis() - start;
      return new ResponseEntity<>(new ApiResponse(responseTime, "Update Product Discount", HttpStatus.FORBIDDEN.value(),
          "Error", "Discount date is not valid! Please change discount date.", null), HttpStatus.FORBIDDEN);
    }
    productDiscountService.updateProductDiscount(id, entity);
    long responseTime = System.currentTimeMillis() - start;
    return new ResponseEntity<>(new ApiResponse(responseTime, "Update Product Discount", HttpStatus.OK.value(),
        "Success", "Discount on product has been updated.", null), HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse> deleteProductDiscount(@PathVariable @Valid Long id) {
    productDiscountService.deleteProductDiscount(id);
    return new ResponseEntity<>(new ApiResponse(0L, "Delete Product Discount", HttpStatus.OK.value(),
        "Success", "Discount has been deleted.", null), HttpStatus.OK);
  }
}
