/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.http.dto.response.ProductImageResponse;
import com.xplaza.backend.mapper.ProductImageMapper;
import com.xplaza.backend.service.ProductImageService;
import com.xplaza.backend.service.entity.ProductImage;

@Deprecated(since = "1.0", forRemoval = true)
@RestController
@RequestMapping("/api/v1/product-images")
public class ProductImageController extends BaseController {
  @Autowired
  private ProductImageService productImgService;

  @Autowired
  private ProductImageMapper productImageMapper;

  @GetMapping("/{id}")
  public ResponseEntity<ProductImageResponse> getProductImage(@PathVariable @Valid Long id) {
    ProductImage entity = productImgService.listProductImage(id);
    ProductImageResponse dto = productImageMapper.toResponse(entity);
    return ResponseEntity.ok(dto);
  }
}
