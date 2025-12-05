/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.order.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.order.domain.entity.CheckoutSession;
import com.xplaza.backend.order.domain.entity.CustomerOrder;
import com.xplaza.backend.order.service.CheckoutService;

/**
 * REST controller for checkout operations.
 */
@RestController
@RequestMapping("/api/v1/checkout")
@RequiredArgsConstructor
@Tag(name = "Checkout", description = "Checkout session management APIs")
public class CheckoutController {

  private final CheckoutService checkoutService;

  @Operation(summary = "Start a new checkout session")
  @PostMapping("/start")
  public ResponseEntity<CheckoutSession> startCheckout(
      @RequestParam UUID cartId,
      @RequestParam Long customerId) {
    CheckoutSession checkout = checkoutService.startCheckout(cartId, customerId);
    return ResponseEntity.ok(checkout);
  }

  @Operation(summary = "Get checkout session by ID")
  @GetMapping("/{checkoutId}")
  public ResponseEntity<CheckoutSession> getCheckout(
      @Parameter(description = "Checkout session ID") @PathVariable UUID checkoutId) {
    return checkoutService.getCheckout(checkoutId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Set shipping address")
  @PutMapping("/{checkoutId}/shipping-address")
  public ResponseEntity<CheckoutSession> setShippingAddress(
      @PathVariable UUID checkoutId,
      @RequestParam Long addressId) {
    CheckoutSession checkout = checkoutService.setShippingAddress(checkoutId, addressId);
    return ResponseEntity.ok(checkout);
  }

  @Operation(summary = "Set shipping method")
  @PutMapping("/{checkoutId}/shipping-method")
  public ResponseEntity<CheckoutSession> setShippingMethod(
      @PathVariable UUID checkoutId,
      @RequestParam Long methodId,
      @RequestParam String methodName,
      @RequestParam BigDecimal cost) {
    CheckoutSession checkout = checkoutService.setShippingMethod(checkoutId, methodId, methodName, cost);
    return ResponseEntity.ok(checkout);
  }

  @Operation(summary = "Set delivery schedule")
  @PutMapping("/{checkoutId}/delivery-schedule")
  public ResponseEntity<CheckoutSession> setDeliverySchedule(
      @PathVariable UUID checkoutId,
      @RequestParam LocalDate date,
      @RequestParam(required = false) LocalTime slotStart,
      @RequestParam(required = false) LocalTime slotEnd,
      @RequestParam(required = false) String instructions) {
    CheckoutSession checkout = checkoutService.setDeliverySchedule(checkoutId, date, slotStart, slotEnd, instructions);
    return ResponseEntity.ok(checkout);
  }

  @Operation(summary = "Set billing address")
  @PutMapping("/{checkoutId}/billing-address")
  public ResponseEntity<CheckoutSession> setBillingAddress(
      @PathVariable UUID checkoutId,
      @RequestParam Long addressId,
      @RequestParam(defaultValue = "false") boolean sameAsShipping) {
    CheckoutSession checkout = checkoutService.setBillingAddress(checkoutId, addressId, sameAsShipping);
    return ResponseEntity.ok(checkout);
  }

  @Operation(summary = "Set payment method")
  @PutMapping("/{checkoutId}/payment-method")
  public ResponseEntity<CheckoutSession> setPaymentMethod(
      @PathVariable UUID checkoutId,
      @RequestParam Long methodId,
      @RequestParam String methodType) {
    CheckoutSession checkout = checkoutService.setPaymentMethod(checkoutId, methodId, methodType);
    return ResponseEntity.ok(checkout);
  }

  @Operation(summary = "Apply coupon code")
  @PostMapping("/{checkoutId}/coupon")
  public ResponseEntity<CheckoutSession> applyCoupon(
      @PathVariable UUID checkoutId,
      @RequestParam Long couponId,
      @RequestParam String couponCode,
      @RequestParam BigDecimal discountAmount) {
    CheckoutSession checkout = checkoutService.applyCoupon(checkoutId, couponId, couponCode, discountAmount);
    return ResponseEntity.ok(checkout);
  }

  @Operation(summary = "Remove coupon")
  @DeleteMapping("/{checkoutId}/coupon")
  public ResponseEntity<CheckoutSession> removeCoupon(@PathVariable UUID checkoutId) {
    CheckoutSession checkout = checkoutService.removeCoupon(checkoutId);
    return ResponseEntity.ok(checkout);
  }

  @Operation(summary = "Set customer notes")
  @PutMapping("/{checkoutId}/notes")
  public ResponseEntity<CheckoutSession> setCustomerNotes(
      @PathVariable UUID checkoutId,
      @RequestBody String notes) {
    CheckoutSession checkout = checkoutService.setCustomerNotes(checkoutId, notes);
    return ResponseEntity.ok(checkout);
  }

  @Operation(summary = "Complete checkout and create order")
  @PostMapping("/{checkoutId}/complete")
  public ResponseEntity<CustomerOrder> completeCheckout(@PathVariable UUID checkoutId) {
    CustomerOrder order = checkoutService.completeCheckout(checkoutId);
    return ResponseEntity.ok(order);
  }

  @Operation(summary = "Abandon checkout session")
  @PostMapping("/{checkoutId}/abandon")
  public ResponseEntity<Void> abandonCheckout(@PathVariable UUID checkoutId) {
    checkoutService.abandonCheckout(checkoutId);
    return ResponseEntity.ok().build();
  }
}
