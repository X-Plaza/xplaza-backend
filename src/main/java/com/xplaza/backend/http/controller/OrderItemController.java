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
import com.xplaza.backend.http.dto.request.OrderItemRequest;
import com.xplaza.backend.http.dto.response.OrderItemResponse;
import com.xplaza.backend.mapper.OrderItemMapper;
import com.xplaza.backend.service.OrderItemService;
import com.xplaza.backend.service.entity.OrderItem;

@RestController
@RequestMapping("/api/v1/order-items")
public class OrderItemController extends BaseController {
  private final OrderItemService orderItemService;
  private final OrderItemMapper orderItemMapper;

  @Autowired
  public OrderItemController(OrderItemService orderItemService, OrderItemMapper orderItemMapper) {
    this.orderItemService = orderItemService;
    this.orderItemMapper = orderItemMapper;
  }

  @GetMapping
  public ResponseEntity<List<OrderItemResponse>> getOrderItems() {
    List<OrderItem> orderItems = orderItemService.listOrderItems();
    List<OrderItemResponse> responses = orderItems.stream()
        .map(orderItemMapper::toResponse)
        .toList();
    return ResponseEntity.ok(responses);
  }

  @GetMapping("/{id}")
  public ResponseEntity<OrderItemResponse> getOrderItem(@PathVariable @Valid Long id) {
    OrderItem orderItem = orderItemService.listOrderItem(id);
    OrderItemResponse response = orderItemMapper.toResponse(orderItem);
    return ResponseEntity.ok(response);
  }

  @PostMapping
  public ResponseEntity<ApiResponse> createOrderItem(@Valid @RequestBody OrderItemRequest request)
      throws JsonProcessingException {
    long start = System.currentTimeMillis();
    OrderItem orderItem = orderItemMapper.toEntity(request);
    OrderItem createdOrderItem = orderItemService.addOrderItem(orderItem);
    OrderItemResponse response = orderItemMapper.toResponse(createdOrderItem);
    long responseTime = System.currentTimeMillis() - start;
    ObjectMapper mapper = new ObjectMapper();
    String responseData = mapper.writeValueAsString(response);
    ApiResponse apiResponse = new ApiResponse(
        responseTime,
        "Add Order Item",
        HttpStatus.CREATED.value(),
        "Success",
        "Order item has been created.",
        responseData);
    return new ResponseEntity<>(apiResponse, HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse> updateOrderItem(
      @PathVariable Long id,
      @RequestBody @Valid OrderItemRequest request) throws JsonProcessingException {
    long start = System.currentTimeMillis();
    OrderItem orderItem = orderItemMapper.toEntity(request);
    OrderItem updatedOrderItem = orderItemService.updateOrderItem(id, orderItem);
    OrderItemResponse response = orderItemMapper.toResponse(updatedOrderItem);
    long responseTime = System.currentTimeMillis() - start;
    ObjectMapper mapper = new ObjectMapper();
    String responseData = mapper.writeValueAsString(response);
    ApiResponse apiResponse = new ApiResponse(
        responseTime,
        "Update Order Item",
        HttpStatus.OK.value(),
        "Success",
        "Order item has been updated.",
        responseData);
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse> deleteOrderItem(@PathVariable @Valid Long id) {
    String orderItemName = orderItemService.getOrderItemNameByID(id);
    long start = System.currentTimeMillis();
    orderItemService.deleteOrderItem(id);
    long responseTime = System.currentTimeMillis() - start;
    ApiResponse apiResponse = new ApiResponse(
        responseTime,
        "Delete Order Item",
        HttpStatus.OK.value(),
        "Success",
        orderItemName + " has been deleted.",
        null);
    return new ResponseEntity<>(apiResponse, HttpStatus.OK);
  }
}
