/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.cart.controller;

import java.math.BigDecimal;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.cart.domain.entity.Cart;
import com.xplaza.backend.cart.domain.entity.CartItem;
import com.xplaza.backend.cart.service.CartService;

/**
 * REST controller for shopping cart operations.
 */
@RestController
@RequestMapping("/api/v1/carts")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart operations")
public class CartController {

  private final CartService cartService;

  // ==================== Cart Operations ====================

  @GetMapping("/{cartId}")
  @Operation(summary = "Get cart by ID")
  public ResponseEntity<Cart> getCart(@PathVariable UUID cartId) {
    return cartService.getCart(cartId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @GetMapping("/customer/{customerId}")
  @Operation(summary = "Get active cart for customer")
  public ResponseEntity<Cart> getCustomerCart(@PathVariable Long customerId) {
    return cartService.getActiveCartForCustomer(customerId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping("/customer/{customerId}")
  @Operation(summary = "Get or create cart for customer")
  public ResponseEntity<Cart> getOrCreateCart(@PathVariable Long customerId) {
    Cart cart = cartService.getOrCreateCart(customerId);
    return ResponseEntity.ok(cart);
  }

  @GetMapping("/session/{sessionId}")
  @Operation(summary = "Get active cart for session (guest)")
  public ResponseEntity<Cart> getSessionCart(@PathVariable String sessionId) {
    return cartService.getActiveCartForSession(sessionId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @PostMapping("/session/{sessionId}")
  @Operation(summary = "Get or create cart for session (guest)")
  public ResponseEntity<Cart> getOrCreateGuestCart(@PathVariable String sessionId) {
    Cart cart = cartService.getOrCreateGuestCart(sessionId);
    return ResponseEntity.ok(cart);
  }

  @GetMapping("/{cartId}/summary")
  @Operation(summary = "Get cart summary")
  public ResponseEntity<CartService.CartSummary> getCartSummary(@PathVariable UUID cartId) {
    return ResponseEntity.ok(cartService.getCartSummary(cartId));
  }

  // ==================== Item Operations ====================

  @PostMapping("/{cartId}/items")
  @Operation(summary = "Add item to cart")
  public ResponseEntity<CartItem> addItem(
      @PathVariable UUID cartId,
      @RequestBody AddItemRequest request) {
    CartItem item = cartService.addItem(
        cartId,
        request.productId(),
        request.variantId(),
        request.shopId(),
        request.quantity(),
        request.unitPrice(),
        request.productName(),
        request.variantName(),
        request.sku(),
        request.imageUrl());
    return ResponseEntity.status(HttpStatus.CREATED).body(item);
  }

  @PutMapping("/{cartId}/items/{itemId}")
  @Operation(summary = "Update item quantity")
  public ResponseEntity<CartItem> updateItemQuantity(
      @PathVariable UUID cartId,
      @PathVariable UUID itemId,
      @RequestBody UpdateQuantityRequest request) {
    CartItem item = cartService.updateItemQuantity(cartId, itemId, request.quantity());
    if (item == null) {
      return ResponseEntity.noContent().build(); // Item was removed
    }
    return ResponseEntity.ok(item);
  }

  @DeleteMapping("/{cartId}/items/{itemId}")
  @Operation(summary = "Remove item from cart")
  public ResponseEntity<Void> removeItem(
      @PathVariable UUID cartId,
      @PathVariable UUID itemId) {
    cartService.removeItem(cartId, itemId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{cartId}/items/{itemId}/save-for-later")
  @Operation(summary = "Save item for later")
  public ResponseEntity<CartItem> saveForLater(
      @PathVariable UUID cartId,
      @PathVariable UUID itemId) {
    CartItem item = cartService.saveForLater(cartId, itemId);
    return ResponseEntity.ok(item);
  }

  @PostMapping("/{cartId}/items/{itemId}/move-to-cart")
  @Operation(summary = "Move item back to cart from saved for later")
  public ResponseEntity<CartItem> moveToCart(
      @PathVariable UUID cartId,
      @PathVariable UUID itemId) {
    CartItem item = cartService.moveToCart(cartId, itemId);
    return ResponseEntity.ok(item);
  }

  // ==================== Cart Actions ====================

  @DeleteMapping("/{cartId}")
  @Operation(summary = "Clear cart")
  public ResponseEntity<Void> clearCart(@PathVariable UUID cartId) {
    cartService.clearCart(cartId);
    return ResponseEntity.noContent().build();
  }

  @PostMapping("/{cartId}/coupon")
  @Operation(summary = "Apply coupon to cart")
  public ResponseEntity<Cart> applyCoupon(
      @PathVariable UUID cartId,
      @RequestBody ApplyCouponRequest request) {
    Cart cart = cartService.applyCoupon(cartId, request.couponCode(), request.discountAmount());
    return ResponseEntity.ok(cart);
  }

  @DeleteMapping("/{cartId}/coupon")
  @Operation(summary = "Remove coupon from cart")
  public ResponseEntity<Cart> removeCoupon(@PathVariable UUID cartId) {
    Cart cart = cartService.removeCoupon(cartId);
    return ResponseEntity.ok(cart);
  }

  @PostMapping("/{sessionId}/merge/{customerId}")
  @Operation(summary = "Merge guest cart into customer cart")
  public ResponseEntity<Cart> mergeGuestCart(
      @PathVariable String sessionId,
      @PathVariable Long customerId) {
    Cart cart = cartService.mergeGuestCart(sessionId, customerId);
    return ResponseEntity.ok(cart);
  }

  // ==================== Request DTOs ====================

  public record AddItemRequest(
      Long productId,
      Long variantId,
      Long shopId,
      int quantity,
      BigDecimal unitPrice,
      String productName,
      String variantName,
      String sku,
      String imageUrl
  ) {
  }

  public record UpdateQuantityRequest(int quantity) {
  }

  public record ApplyCouponRequest(
      String couponCode,
      BigDecimal discountAmount
  ) {
  }
}
