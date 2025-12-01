/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.http.dto.request.OrderRequest;
import com.xplaza.backend.http.dto.response.OrderResponse;
import com.xplaza.backend.mapper.OrderMapper;
import com.xplaza.backend.service.OrderService;
import com.xplaza.backend.service.PlatformInfoService;
import com.xplaza.backend.service.RoleService;
import com.xplaza.backend.service.entity.Order;

@Deprecated(since = "1.0", forRemoval = true)
@RestController
@RequestMapping("/api/v1/orders")
public class OrderController extends BaseController {
  private final OrderService orderService;
  private final RoleService roleService;
  private final PlatformInfoService platformInfoService;
  private final OrderMapper orderMapper;

  @Autowired
  public OrderController(OrderService orderService, RoleService roleService, PlatformInfoService platformInfoService,
      OrderMapper orderMapper) {
    this.orderService = orderService;
    this.roleService = roleService;
    this.platformInfoService = platformInfoService;
    this.orderMapper = orderMapper;
  }

  @Operation(summary = "Get order by ID", description = "Retrieve a single order by its ID.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order found and returned successfully."),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found."),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid order ID supplied.")
  })
  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse> getOrder(
      @Parameter(description = "ID of the order to retrieve", required = true) @PathVariable @Valid Long id)
      throws JsonProcessingException {
    long start = System.currentTimeMillis();
    Order entity = orderService.getOrderById(id);
    OrderResponse dto = orderMapper.toResponse(entity);
    long responseTime = System.currentTimeMillis() - start;
    String data = new ObjectMapper().writeValueAsString(dto);
    ApiResponse response = new ApiResponse(responseTime, "Order By ID", HttpStatus.OK.value(), "Success", "", data);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Create a new order", description = "Add a new order to the system.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Order created successfully."),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Validation error in order data.")
  })
  @PostMapping
  public ResponseEntity<ApiResponse> addOrder(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Order request body", required = true) @RequestBody @Valid OrderRequest request)
      throws JsonProcessingException {
    long start = System.currentTimeMillis();
    Order entity = orderMapper.toEntity(request);
    Order createdOrder = orderService.createOrder(entity);
    OrderResponse dto = orderMapper.toResponse(createdOrder);
    long responseTime = System.currentTimeMillis() - start;
    String data = new ObjectMapper().writeValueAsString(dto);
    ApiResponse response = new ApiResponse(responseTime, "Add Order", HttpStatus.CREATED.value(), "Success",
        "Order has been created.", data);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @Operation(summary = "Update an order", description = "Update an existing order.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order updated successfully."),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found."),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "422", description = "Validation error in order data.")
  })
  @PutMapping
  public ResponseEntity<ApiResponse> updateOrder(
      @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Order request body", required = true) @RequestBody @Valid OrderRequest request)
      throws JsonProcessingException {
    long start = System.currentTimeMillis();
    Order entity = orderMapper.toEntity(request);
    orderService.updateOrder(entity);
    long responseTime = System.currentTimeMillis() - start;
    ApiResponse response = new ApiResponse(responseTime, "Update Order", HttpStatus.OK.value(), "Success",
        "Order has been updated.", null);
    return ResponseEntity.ok(response);
  }

  @Operation(summary = "Delete an order", description = "Delete an order by its ID.")
  @ApiResponses({
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Order deleted successfully."),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Order not found."),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid order ID supplied.")
  })
  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse> deleteOrder(
      @Parameter(description = "ID of the order to delete", required = true) @PathVariable @Valid Long id) {
    long start = System.currentTimeMillis();
    orderService.deleteOrder(id);
    long responseTime = System.currentTimeMillis() - start;
    ApiResponse response = new ApiResponse(responseTime, "Delete Order", HttpStatus.OK.value(), "Success",
        "Order has been deleted.", null);
    return ResponseEntity.ok(response);
  }
}
