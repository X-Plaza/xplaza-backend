/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.order.controller;

import java.util.List;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.order.domain.entity.OrderStatus;
import com.xplaza.backend.order.dto.request.OrderStatusRequest;
import com.xplaza.backend.order.dto.response.OrderStatusResponse;
import com.xplaza.backend.order.mapper.OrderStatusMapper;
import com.xplaza.backend.order.service.OrderStatusService;

@RestController
@RequestMapping("/api/v1/order-statuses")
@RequiredArgsConstructor
@Tag(name = "Order Status Management", description = "APIs for managing order statuses")
public class OrderStatusController {
  private final OrderStatusService orderStatusService;
  private final OrderStatusMapper orderStatusMapper;

  @GetMapping
  @Operation(summary = "List all active order statuses")
  public ResponseEntity<ApiResponse<List<OrderStatusResponse>>> listOrderStatuses(
      @RequestParam(required = false, defaultValue = "false") Boolean includeInactive) {
    List<OrderStatus> statuses = includeInactive ? orderStatusService.listAllOrderStatuses()
        : orderStatusService.listOrderStatuses();
    List<OrderStatusResponse> response = statuses.stream().map(orderStatusMapper::toResponse).toList();
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get order status by ID")
  public ResponseEntity<ApiResponse<OrderStatusResponse>> getOrderStatus(@PathVariable Long id) {
    OrderStatus status = orderStatusService.getOrderStatus(id);
    return ResponseEntity.ok(ApiResponse.ok(orderStatusMapper.toResponse(status)));
  }

  @PostMapping
  @Operation(summary = "Create a new order status")
  public ResponseEntity<ApiResponse<OrderStatusResponse>> createOrderStatus(
      @RequestBody @Valid OrderStatusRequest request) {
    OrderStatus entity = orderStatusMapper.toEntity(request);
    OrderStatus saved = orderStatusService.createOrderStatus(entity);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(orderStatusMapper.toResponse(saved)));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update an existing order status")
  public ResponseEntity<ApiResponse<OrderStatusResponse>> updateOrderStatus(@PathVariable Long id,
      @RequestBody @Valid OrderStatusRequest request) {
    OrderStatus details = orderStatusMapper.toEntity(request);
    OrderStatus updated = orderStatusService.updateOrderStatus(id, details);
    return ResponseEntity.ok(ApiResponse.ok(orderStatusMapper.toResponse(updated)));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete an order status")
  public ResponseEntity<ApiResponse<Void>> deleteOrderStatus(@PathVariable Long id) {
    orderStatusService.deleteOrderStatus(id);
    return ResponseEntity.ok(ApiResponse.ok("Order status deleted"));
  }
}
