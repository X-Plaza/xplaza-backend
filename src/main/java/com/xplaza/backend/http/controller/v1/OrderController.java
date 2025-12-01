/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller.v1;

import java.time.LocalDate;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.common.util.ApiResponse.PageMeta;
import com.xplaza.backend.domain.Order;
import com.xplaza.backend.domain.OrderList;
import com.xplaza.backend.http.dto.request.OrderRequest;
import com.xplaza.backend.http.dto.response.OrderResponse;
import com.xplaza.backend.mapper.OrderMapper;
import com.xplaza.backend.service.OrderService;

/**
 * Order Controller - Clean REST API design.
 * 
 * Endpoints: - GET /api/v1/orders - List orders with filters - GET
 * /api/v1/orders/{id} - Get single order - POST /api/v1/orders - Create order -
 * PUT /api/v1/orders/{id} - Update order - PATCH /api/v1/orders/{id}/status -
 * Update order status - DELETE /api/v1/orders/{id} - Delete order
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Validated
@Tag(name = "Order Management", description = "APIs for managing orders with pagination and filtering")
public class OrderController {

  private final OrderService orderService;
  private final OrderMapper orderMapper;

  /**
   * GET /api/v1/orders
   * 
   * List orders with pagination and optional filters.
   * 
   * Query Parameters: - status: Filter by order status (e.g., "Pending",
   * "Delivered") - shopId: Filter by shop - customerId: Filter by customer -
   * adminUserId: Filter by admin user (shop owner) - orderDate: Filter by order
   * date (YYYY-MM-DD) - page, size, sort, direction: Pagination params
   */
  @GetMapping
  @Operation(summary = "List orders", description = "Get paginated list of orders with optional filters")
  public ResponseEntity<ApiResponse<List<OrderList>>> getOrders(
      @RequestParam(required = false) String status,
      @RequestParam(required = false) Long shopId,
      @RequestParam(required = false) Long customerId,
      @RequestParam(required = false) Long adminUserId,
      @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate orderDate,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) int size,
      @RequestParam(defaultValue = "orderId") String sort,
      @RequestParam(defaultValue = "DESC") Sort.Direction direction) {

    size = Math.min(size, 100);
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

    // Determine which filter combination to use
    Page<OrderList> orderPage;
    java.sql.Date sqlDate = orderDate != null ? java.sql.Date.valueOf(orderDate) : null;

    if (adminUserId != null) {
      // Admin user specific queries
      if (status != null && sqlDate != null) {
        orderPage = orderService.getOrdersByFilterAndAdminUserPaginated(status, sqlDate, adminUserId, pageable);
      } else if (status != null) {
        orderPage = orderService.getOrdersByStatusAndAdminUserPaginated(status, adminUserId, pageable);
      } else {
        orderPage = orderService.getOrdersByAdminUserPaginated(adminUserId, pageable);
      }
    } else {
      // Global queries
      if (status != null && sqlDate != null) {
        orderPage = orderService.getOrdersByFilterPaginated(status, sqlDate, pageable);
      } else if (status != null) {
        orderPage = orderService.getOrdersByStatusPaginated(status, pageable);
      } else {
        orderPage = orderService.getAllOrdersPaginated(pageable);
      }
    }

    PageMeta pageMeta = PageMeta.from(orderPage);

    return ResponseEntity.ok(ApiResponse.ok(orderPage.getContent(), pageMeta));
  }

  /**
   * GET /api/v1/orders/{id}
   * 
   * Get single order by ID with full details.
   */
  @GetMapping("/{id}")
  @Operation(summary = "Get order by ID", description = "Retrieve a specific order with full details")
  public ResponseEntity<ApiResponse<OrderResponse>> getOrder(
      @PathVariable @Positive Long id) {

    Order order = orderService.getOrderById(id);
    OrderResponse dto = orderMapper.toResponse(order);

    return ResponseEntity.ok(ApiResponse.ok(dto));
  }

  /**
   * POST /api/v1/orders
   * 
   * Create a new order.
   */
  @PostMapping
  @Operation(summary = "Create order", description = "Create a new order")
  public ResponseEntity<ApiResponse<OrderResponse>> createOrder(
      @RequestBody @Valid OrderRequest request) {

    Order entity = orderMapper.toEntity(request);
    Order created = orderService.createOrder(entity);
    OrderResponse dto = orderMapper.toResponse(created);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponse.created(dto));
  }

  /**
   * PUT /api/v1/orders/{id}
   * 
   * Update an existing order.
   */
  @PutMapping("/{id}")
  @Operation(summary = "Update order", description = "Update an existing order")
  public ResponseEntity<ApiResponse<OrderResponse>> updateOrder(
      @PathVariable @Positive Long id,
      @RequestBody @Valid OrderRequest request) {

    Order entity = orderMapper.toEntity(request);
    entity.setInvoiceNumber(id);
    orderService.updateOrder(entity);

    // Fetch updated order
    Order updated = orderService.getOrderById(id);
    OrderResponse dto = orderMapper.toResponse(updated);

    return ResponseEntity.ok(ApiResponse.ok(dto));
  }

  /**
   * PATCH /api/v1/orders/{id}/status
   * 
   * Update order status only.
   */
  @PatchMapping("/{id}/status")
  @Operation(summary = "Update order status", description = "Update the status of an order")
  public ResponseEntity<ApiResponse<Void>> updateOrderStatus(
      @PathVariable @Positive Long id,
      @RequestParam @Positive Long statusId,
      @RequestParam(required = false) String remarks) {

    orderService.updateOrderStatus(id, remarks != null ? remarks : "", statusId);

    return ResponseEntity.ok(ApiResponse.ok("Order status updated successfully"));
  }

  /**
   * DELETE /api/v1/orders/{id}
   * 
   * Delete an order.
   */
  @DeleteMapping("/{id}")
  @Operation(summary = "Delete order", description = "Delete an order by ID")
  public ResponseEntity<ApiResponse<Void>> deleteOrder(
      @PathVariable @Positive Long id) {

    orderService.deleteOrder(id);

    return ResponseEntity.ok(ApiResponse.ok("Order has been deleted"));
  }

  /**
   * POST /api/v1/orders/{id}/cancel
   * 
   * Cancel an order and restore inventory. This is the preferred method for
   * cancelling orders as it properly restores inventory and follows the order
   * state machine.
   */
  @PostMapping("/{id}/cancel")
  @Operation(summary = "Cancel order", description = "Cancel an order and restore inventory")
  public ResponseEntity<ApiResponse<Void>> cancelOrder(
      @PathVariable @Positive Long id,
      @RequestParam(required = false) String remarks) {

    orderService.cancelOrder(id, remarks);

    return ResponseEntity.ok(ApiResponse.ok("Order has been cancelled and inventory restored"));
  }
}
