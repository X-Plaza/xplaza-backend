/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller;

import java.text.ParseException;
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
import com.xplaza.backend.http.dto.request.ProductRequest;
import com.xplaza.backend.http.dto.response.ProductResponse;
import com.xplaza.backend.mapper.ProductMapper;
import com.xplaza.backend.service.ProductService;
import com.xplaza.backend.service.RoleService;
import com.xplaza.backend.service.entity.Product;

@RestController
@RequestMapping("/api/v1/products")
public class ProductController extends BaseController {
  private final ProductService productService;
  private final RoleService roleService;
  private final ProductMapper productMapper;

  @Autowired
  public ProductController(ProductService productService, RoleService roleService, ProductMapper productMapper) {
    this.productService = productService;
    this.roleService = roleService;
    this.productMapper = productMapper;
  }

  @GetMapping
  public ResponseEntity<ApiResponse> getProducts(@RequestParam(value = "user_id") @Valid Long user_id)
      throws JsonProcessingException, ParseException {
    long start = System.currentTimeMillis();
    List<Product> entities = productService.listProducts();
    List<ProductResponse> dtos = entities == null ? null
        : entities.stream().map(productMapper::toResponse).toList();
    long responseTime = System.currentTimeMillis() - start;
    String data = new ObjectMapper().writeValueAsString(dtos);
    ApiResponse response = new ApiResponse(responseTime, "Product List", HttpStatus.OK.value(), "Success", "", data);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse> getProduct(@PathVariable @Valid Long id)
      throws JsonProcessingException, ParseException {
    long start = System.currentTimeMillis();
    Product entity = productService.listProduct(id);
    ProductResponse dto = productMapper.toResponse(entity);
    long responseTime = System.currentTimeMillis() - start;
    String data = new ObjectMapper().writeValueAsString(dto);
    ApiResponse response = new ApiResponse(responseTime, "Product By ID", HttpStatus.OK.value(), "Success", "", data);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/by-shop-by-admin")
  public ResponseEntity<ApiResponse> getProductsByShopByAdmin(
      @RequestParam(value = "shop_id") @Valid Long shop_id)
      throws JsonProcessingException, ParseException {
    ApiResponse response = new ApiResponse(0L, "Not Implemented", HttpStatus.NOT_IMPLEMENTED.value(), "Error",
        "Not implemented", null);
    return new ResponseEntity<>(response, HttpStatus.NOT_IMPLEMENTED);
  }

  @GetMapping("/by-shop")
  public ResponseEntity<ApiResponse> getProductsByShop(@RequestParam(value = "shop_id") @Valid Long shop_id)
      throws JsonProcessingException, ParseException {
    long start = System.currentTimeMillis();
    List<Product> entities = productService.listProductsByShop(shop_id);
    List<ProductResponse> dtos = entities.stream().map(productMapper::toResponse).toList();
    long responseTime = System.currentTimeMillis() - start;
    String data = new ObjectMapper().writeValueAsString(dtos);
    ApiResponse response = new ApiResponse(responseTime, "Product List By Shop", HttpStatus.OK.value(), "Success", "",
        data);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/by-trending")
  public ResponseEntity<ApiResponse> getProductsByTrending(
      @RequestParam(value = "shop_id") @Valid Long shop_id)
      throws JsonProcessingException, ParseException {
    ApiResponse response = new ApiResponse(0L, "Not Implemented", HttpStatus.NOT_IMPLEMENTED.value(), "Error",
        "Not implemented", null);
    return new ResponseEntity<>(response, HttpStatus.NOT_IMPLEMENTED);
  }

  @GetMapping("/by-category")
  public ResponseEntity<ApiResponse> getProductsByCategory(
      @RequestParam(value = "shop_id") @Valid Long shop_id,
      @RequestParam(value = "category_id") @Valid Long category_id)
      throws JsonProcessingException, ParseException {
    long start = System.currentTimeMillis();
    List<Product> entities = productService.listProductsByCategory(category_id);
    List<ProductResponse> dtos = entities.stream().map(productMapper::toResponse).toList();
    long responseTime = System.currentTimeMillis() - start;
    String data = new ObjectMapper().writeValueAsString(dtos);
    ApiResponse response = new ApiResponse(responseTime, "Product List By Category", HttpStatus.OK.value(), "Success",
        "", data);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/by-name")
  public ResponseEntity<ApiResponse> searchProductsByName(
      @RequestParam(value = "shop_id") @Valid Long shop_id,
      @RequestParam(value = "product_name") @Valid String product_name)
      throws JsonProcessingException {
    long start = System.currentTimeMillis();
    List<Product> entities = List.of();
    List<ProductResponse> dtos = entities.stream().map(productMapper::toResponse).toList();
    long responseTime = System.currentTimeMillis() - start;
    String data = new ObjectMapper().writeValueAsString(dtos);
    ApiResponse response = new ApiResponse(responseTime, "Product List By Name", HttpStatus.OK.value(), "Success", "",
        data);
    return ResponseEntity.ok(response);
  }

  @PostMapping
  public ResponseEntity<ApiResponse> addProduct(@RequestBody @Valid ProductRequest productRequest)
      throws JSONException {
    long start = System.currentTimeMillis();
    Product entity = productMapper.toEntity(productRequest);
    productService.addProduct(entity);
    long responseTime = System.currentTimeMillis() - start;
    return new ResponseEntity<>(new ApiResponse(responseTime, "Add Product", HttpStatus.CREATED.value(),
        "Success", "Product has been created.", null), HttpStatus.CREATED);
  }

  @PutMapping
  public ResponseEntity<ApiResponse> updateProduct(@RequestBody @Valid ProductRequest productRequest) {
    long start = System.currentTimeMillis();
    Product entity = productMapper.toEntity(productRequest);
    productService.updateProduct(entity);
    long responseTime = System.currentTimeMillis() - start;
    return new ResponseEntity<>(new ApiResponse(responseTime, "Update Product", HttpStatus.OK.value(),
        "Success", "Product has been updated.", null), HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse> deleteProduct(@PathVariable @Valid Long id) {
    String product_name = productService.getProductNameByID(id);
    long start = System.currentTimeMillis();
    productService.deleteProduct(id);
    long responseTime = System.currentTimeMillis() - start;
    return new ResponseEntity<>(new ApiResponse(responseTime, "Delete Product", HttpStatus.OK.value(),
        "Success", product_name + " has been deleted.", null), HttpStatus.OK);
  }
}
