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

import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.http.dto.response.ProductVariationTypeResponse;
import com.xplaza.backend.mapper.ProductVariationTypeMapper;
import com.xplaza.backend.service.ProductVariationTypeService;
import com.xplaza.backend.service.entity.ProductVariationType;

@RestController
@RequestMapping("/api/v1/product-variation-types")
public class ProductVariationTypeController extends BaseController {
  @Autowired
  private ProductVariationTypeService prodVarTypeService;
  @Autowired
  private ProductVariationTypeMapper productVariationTypeMapper;

  @GetMapping
  public ResponseEntity<List<ProductVariationTypeResponse>> getProductVarTypes() {
    List<ProductVariationType> entities = prodVarTypeService.listProductVariationTypes();
    List<ProductVariationTypeResponse> dtos = entities.stream().map(productVariationTypeMapper::toResponse).toList();
    return ResponseEntity.ok(dtos);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ProductVariationTypeResponse> getProductVarType(@PathVariable @Valid Long id) {
    ProductVariationType entity = prodVarTypeService.listProductVariationType(id);
    ProductVariationTypeResponse dto = productVariationTypeMapper.toResponse(entity);
    return ResponseEntity.ok(dto);
  }

  @PostMapping
  public ResponseEntity<ApiResponse> addProductVarType(@RequestBody @Valid ProductVariationType productVariationType) {
    long start = System.currentTimeMillis();
    prodVarTypeService.addProductVariationType(productVariationType);
    long responseTime = System.currentTimeMillis() - start;
    return new ResponseEntity<>(new ApiResponse(responseTime, "Add Product Variation Type", HttpStatus.CREATED.value(),
        "Success", "Product Variation Type has been created.", null), HttpStatus.CREATED);
  }

  @PutMapping
  public ResponseEntity<ApiResponse> updateProductVarType(
      @RequestBody @Valid ProductVariationType productVariationType) {
    long start = System.currentTimeMillis();
    prodVarTypeService.updateProductVariationType(productVariationType);
    long responseTime = System.currentTimeMillis() - start;
    return new ResponseEntity<>(new ApiResponse(responseTime, "Update Product Variation Type", HttpStatus.OK.value(),
        "Success", "Product Variation Type has been updated.", null), HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse> deleteProductVarType(@PathVariable @Valid Long id) {
    long start = System.currentTimeMillis();
    prodVarTypeService.deleteProductVariationType(id);
    long responseTime = System.currentTimeMillis() - start;
    return new ResponseEntity<>(new ApiResponse(responseTime, "Delete Product Variation Type", HttpStatus.OK.value(),
        "Success", "Product Variation Type has been deleted.", null), HttpStatus.OK);
  }
}
