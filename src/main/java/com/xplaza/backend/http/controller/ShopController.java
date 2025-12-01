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

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.http.dto.request.ShopRequest;
import com.xplaza.backend.http.dto.response.ShopResponse;
import com.xplaza.backend.mapper.ShopMapper;
import com.xplaza.backend.service.RoleService;
import com.xplaza.backend.service.ShopService;
import com.xplaza.backend.service.entity.Shop;

@RestController
@RequestMapping("/api/v1/shops")
public class ShopController extends BaseController {
  private final ShopService shopService;
  private final RoleService roleService;
  private final ShopMapper shopMapper;

  @Autowired
  public ShopController(ShopService shopService, RoleService roleService, ShopMapper shopMapper) {
    this.shopService = shopService;
    this.roleService = roleService;
    this.shopMapper = shopMapper;
  }

  @GetMapping
  public ResponseEntity<ApiResponse> getShops() throws Exception {
    long start = System.currentTimeMillis();
    List<Shop> shops = shopService.listShops();
    List<ShopResponse> dtos = shops.stream().map(shopMapper::toResponse).toList();
    long end = System.currentTimeMillis();
    long responseTime = end - start;
    String data = new ObjectMapper().writeValueAsString(dtos);
    ApiResponse response = new ApiResponse(responseTime, "Shop List", HttpStatus.OK.value(), "Success", "", data);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse> getShop(@PathVariable @Valid Long id) throws Exception {
    long start = System.currentTimeMillis();
    Shop shop = shopService.listShop(id);
    ShopResponse dto = shopMapper.toResponse(shop);
    long end = System.currentTimeMillis();
    long responseTime = end - start;
    String data = new ObjectMapper().writeValueAsString(dto);
    ApiResponse response = new ApiResponse(responseTime, "Shop By ID", HttpStatus.OK.value(), "Success", "", data);
    return ResponseEntity.ok(response);
  }

  @PostMapping
  public ResponseEntity<ApiResponse> addShop(@RequestBody @Valid ShopRequest shopRequest) {
    Shop shop = shopMapper.toEntity(shopRequest);
    shopService.addShop(shop);
    ApiResponse response = new ApiResponse(0, "Add Shop", HttpStatus.CREATED.value(), "Success",
        "Shop has been created.", null);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse> updateShop(@PathVariable Long id, @RequestBody @Valid ShopRequest shopRequest) {
    Shop shop = shopMapper.toEntity(shopRequest);
    shopService.updateShop(id, shop);
    ApiResponse response = new ApiResponse(0, "Update Shop", HttpStatus.OK.value(), "Success",
        "Shop has been updated.", null);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse> deleteShop(@PathVariable @Valid Long id) {
    shopService.deleteShop(id);
    ApiResponse response = new ApiResponse(0, "Delete Shop", HttpStatus.OK.value(), "Success",
        "Shop has been deleted.", null);
    return ResponseEntity.ok(response);
  }
}
