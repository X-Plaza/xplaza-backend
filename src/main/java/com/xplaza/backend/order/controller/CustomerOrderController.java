/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.order.controller;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.order.domain.entity.CustomerOrder;
import com.xplaza.backend.order.service.CustomerOrderService;

/**
 * REST controller for new order operations with UUID-based orders.
 */
@RestController
@RequestMapping("/api/v1/customer-orders")
@RequiredArgsConstructor
@Tag(name = "Customer Orders", description = "Customer order management APIs")
public class CustomerOrderController {

  private final CustomerOrderService customerOrderService;

  @Operation(summary = "Get order by ID")
  @GetMapping("/{orderId}")
  public ResponseEntity<CustomerOrder> getOrder(
      @Parameter(description = "Order ID") @PathVariable UUID orderId) {
    return customerOrderService.getOrderWithDetails(orderId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Get order by order number")
  @GetMapping("/number/{orderNumber}")
  public ResponseEntity<CustomerOrder> getOrderByNumber(
      @Parameter(description = "Order number") @PathVariable String orderNumber) {
    return customerOrderService.getOrderByNumber(orderNumber)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Get customer orders")
  @GetMapping("/customer/{customerId}")
  public ResponseEntity<List<CustomerOrder>> getCustomerOrders(
      @Parameter(description = "Customer ID") @PathVariable Long customerId) {
    List<CustomerOrder> orders = customerOrderService.getCustomerOrders(customerId);
    return ResponseEntity.ok(orders);
  }

  @Operation(summary = "Get customer orders (paginated)")
  @GetMapping("/customer/{customerId}/paged")
  public ResponseEntity<Page<CustomerOrder>> getCustomerOrdersPaged(
      @Parameter(description = "Customer ID") @PathVariable Long customerId,
      @PageableDefault(size = 10) Pageable pageable) {
    Page<CustomerOrder> orders = customerOrderService.getCustomerOrders(customerId, pageable);
    return ResponseEntity.ok(orders);
  }

  @Operation(summary = "Get shop orders")
  @GetMapping("/shop/{shopId}")
  public ResponseEntity<List<CustomerOrder>> getShopOrders(
      @Parameter(description = "Shop ID") @PathVariable Long shopId) {
    List<CustomerOrder> orders = customerOrderService.getShopOrders(shopId);
    return ResponseEntity.ok(orders);
  }

  @Operation(summary = "Get shop orders (paginated)")
  @GetMapping("/shop/{shopId}/paged")
  public ResponseEntity<Page<CustomerOrder>> getShopOrdersPaged(
      @Parameter(description = "Shop ID") @PathVariable Long shopId,
      @PageableDefault(size = 10) Pageable pageable) {
    Page<CustomerOrder> orders = customerOrderService.getShopOrders(shopId, pageable);
    return ResponseEntity.ok(orders);
  }

  @Operation(summary = "Get orders by status")
  @GetMapping("/status/{status}")
  public ResponseEntity<List<CustomerOrder>> getOrdersByStatus(
      @Parameter(description = "Order status") @PathVariable CustomerOrder.OrderStatus status) {
    List<CustomerOrder> orders = customerOrderService.getOrdersByStatus(status);
    return ResponseEntity.ok(orders);
  }

  @Operation(summary = "Get shop orders by status")
  @GetMapping("/shop/{shopId}/status/{status}")
  public ResponseEntity<List<CustomerOrder>> getShopOrdersByStatus(
      @PathVariable Long shopId,
      @PathVariable CustomerOrder.OrderStatus status) {
    List<CustomerOrder> orders = customerOrderService.getShopOrdersByStatus(shopId, status);
    return ResponseEntity.ok(orders);
  }

  @Operation(summary = "Confirm order (payment received)")
  @PostMapping("/{orderId}/confirm")
  public ResponseEntity<CustomerOrder> confirmOrder(
      @PathVariable UUID orderId,
      @RequestParam UUID paymentTransactionId) {
    CustomerOrder order = customerOrderService.confirmOrder(orderId, paymentTransactionId);
    return ResponseEntity.ok(order);
  }

  @Operation(summary = "Start processing order")
  @PostMapping("/{orderId}/process")
  public ResponseEntity<CustomerOrder> startProcessing(
      @PathVariable UUID orderId,
      @RequestParam(defaultValue = "system") String processedBy) {
    CustomerOrder order = customerOrderService.startProcessing(orderId, processedBy);
    return ResponseEntity.ok(order);
  }

  @Operation(summary = "Mark order as shipped")
  @PostMapping("/{orderId}/ship")
  public ResponseEntity<CustomerOrder> markShipped(
      @PathVariable UUID orderId,
      @RequestParam String carrier,
      @RequestParam String trackingNumber,
      @RequestParam(defaultValue = "system") String shippedBy) {
    CustomerOrder order = customerOrderService.markShipped(orderId, carrier, trackingNumber, shippedBy);
    return ResponseEntity.ok(order);
  }

  @Operation(summary = "Mark order as out for delivery")
  @PostMapping("/{orderId}/out-for-delivery")
  public ResponseEntity<CustomerOrder> markOutForDelivery(
      @PathVariable UUID orderId,
      @RequestParam(defaultValue = "system") String updatedBy) {
    CustomerOrder order = customerOrderService.markOutForDelivery(orderId, updatedBy);
    return ResponseEntity.ok(order);
  }

  @Operation(summary = "Mark order as delivered")
  @PostMapping("/{orderId}/deliver")
  public ResponseEntity<CustomerOrder> markDelivered(
      @PathVariable UUID orderId,
      @RequestParam(defaultValue = "system") String deliveredBy) {
    CustomerOrder order = customerOrderService.markDelivered(orderId, deliveredBy);
    return ResponseEntity.ok(order);
  }

  @Operation(summary = "Cancel order")
  @PostMapping("/{orderId}/cancel")
  public ResponseEntity<CustomerOrder> cancelOrder(
      @PathVariable UUID orderId,
      @RequestParam String reason,
      @RequestParam(defaultValue = "customer") String cancelledBy) {
    CustomerOrder order = customerOrderService.cancelOrder(orderId, reason, cancelledBy);
    return ResponseEntity.ok(order);
  }

  @Operation(summary = "Request return")
  @PostMapping("/{orderId}/return")
  public ResponseEntity<CustomerOrder> requestReturn(
      @PathVariable UUID orderId,
      @RequestParam String reason,
      @RequestParam(defaultValue = "customer") String requestedBy) {
    CustomerOrder order = customerOrderService.requestReturn(orderId, reason, requestedBy);
    return ResponseEntity.ok(order);
  }

  @Operation(summary = "Add internal note to order")
  @PostMapping("/{orderId}/note")
  public ResponseEntity<CustomerOrder> addInternalNote(
      @PathVariable UUID orderId,
      @RequestBody String note) {
    CustomerOrder order = customerOrderService.addInternalNote(orderId, note);
    return ResponseEntity.ok(order);
  }

  @Operation(summary = "Get customer order count")
  @GetMapping("/customer/{customerId}/count")
  public ResponseEntity<Long> getCustomerOrderCount(@PathVariable Long customerId) {
    long count = customerOrderService.countCustomerOrders(customerId);
    return ResponseEntity.ok(count);
  }
}
