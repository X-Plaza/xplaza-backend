/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller.v2;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.common.util.ApiResponseV2;
import com.xplaza.backend.common.util.ApiResponseV2.PageMeta;
import com.xplaza.backend.http.dto.request.OrderItemRequest;
import com.xplaza.backend.http.dto.response.OrderItemResponse;
import com.xplaza.backend.mapper.OrderItemMapper;
import com.xplaza.backend.service.OrderItemService;
import com.xplaza.backend.service.entity.OrderItem;

/**
 * V2 Order Item Controller - Clean REST API design.
 */
@RestController
@RequestMapping("/api/v2/order-items")
@RequiredArgsConstructor
@Validated
@Tag(name = "Order Item Management V2", description = "V2 APIs for managing order items")
public class OrderItemControllerV2 {

  private final OrderItemService orderItemService;
  private final OrderItemMapper orderItemMapper;

  @GetMapping
  @Operation(summary = "List order items", description = "Get paginated list of order items")
  public ResponseEntity<ApiResponseV2<List<OrderItemResponse>>> getOrderItems(
      @RequestParam(required = false) Long orderId,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "50") @Min(1) int size,
      @RequestParam(defaultValue = "orderItemId") String sort,
      @RequestParam(defaultValue = "ASC") Sort.Direction direction) {

    size = Math.min(size, 200);
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

    List<OrderItem> allItems = orderItemService.listOrderItems();

    // Filter by orderId if provided (in-memory, improve with repository query if
    // needed)
    if (orderId != null) {
      allItems = allItems.stream()
          .filter(item -> item.getOrderId() != null && item.getOrderId().equals(orderId))
          .toList();
    }

    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), allItems.size());
    List<OrderItem> pageContent = start < allItems.size() ? allItems.subList(start, end) : List.of();

    List<OrderItemResponse> dtos = pageContent.stream()
        .map(orderItemMapper::toResponse)
        .toList();

    Page<OrderItemResponse> responsePage = new PageImpl<>(dtos, pageable, allItems.size());
    PageMeta pageMeta = PageMeta.from(responsePage);

    return ResponseEntity.ok(ApiResponseV2.ok(dtos, pageMeta));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get order item by ID", description = "Retrieve a specific order item")
  public ResponseEntity<ApiResponseV2<OrderItemResponse>> getOrderItem(@PathVariable @Positive Long id) {
    OrderItem orderItem = orderItemService.listOrderItem(id);
    OrderItemResponse dto = orderItemMapper.toResponse(orderItem);
    return ResponseEntity.ok(ApiResponseV2.ok(dto));
  }

  @PostMapping
  @Operation(summary = "Create order item", description = "Create a new order item")
  public ResponseEntity<ApiResponseV2<OrderItemResponse>> createOrderItem(
      @RequestBody @Valid OrderItemRequest request) {
    OrderItem entity = orderItemMapper.toEntity(request);
    OrderItem created = orderItemService.addOrderItem(entity);
    OrderItemResponse dto = orderItemMapper.toResponse(created);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseV2.created(dto));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update order item", description = "Update an existing order item")
  public ResponseEntity<ApiResponseV2<OrderItemResponse>> updateOrderItem(
      @PathVariable @Positive Long id,
      @RequestBody @Valid OrderItemRequest request) {
    OrderItem entity = orderItemMapper.toEntity(request);
    OrderItem updated = orderItemService.updateOrderItem(id, entity);
    OrderItemResponse dto = orderItemMapper.toResponse(updated);
    return ResponseEntity.ok(ApiResponseV2.ok(dto));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete order item", description = "Delete an order item")
  public ResponseEntity<ApiResponseV2<Void>> deleteOrderItem(@PathVariable @Positive Long id) {
    orderItemService.deleteOrderItem(id);
    return ResponseEntity.ok(ApiResponseV2.ok("Order item deleted successfully"));
  }
}
