/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.order.controller;

import java.math.BigDecimal;
import java.util.UUID;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.order.domain.entity.Cart;
import com.xplaza.backend.order.domain.entity.CartItem;
import com.xplaza.backend.order.service.CartService;

/**
 * REST controller for shopping cart operations.
 */
@RestController
@RequestMapping("/api/v2/cart")
@RequiredArgsConstructor
@Tag(name = "Cart", description = "Shopping cart management")
public class CartController {

  private final CartService cartService;

  @Operation(summary = "Get or create cart for customer")
  @GetMapping("/customer/{customerId}")
  public ResponseEntity<Cart> getCustomerCart(@PathVariable Long customerId) {
    Cart cart = cartService.getOrCreateCart(customerId);
    return ResponseEntity.ok(cart);
  }

  @Operation(summary = "Get or create cart for guest session")
  @GetMapping("/session/{sessionId}")
  public ResponseEntity<Cart> getGuestCart(@PathVariable String sessionId) {
    Cart cart = cartService.getOrCreateGuestCart(sessionId);
    return ResponseEntity.ok(cart);
  }

  @Operation(summary = "Get cart by ID with items")
  @GetMapping("/{cartId}")
  public ResponseEntity<Cart> getCart(@PathVariable UUID cartId) {
    return cartService.getCartWithItems(cartId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Add item to cart")
  @PostMapping("/{cartId}/items")
  public ResponseEntity<CartItem> addItem(
      @PathVariable UUID cartId,
      @Valid @RequestBody AddItemRequest request) {
    CartItem item = cartService.addItem(
        cartId,
        request.productId(),
        request.variantId(),
        request.quantity(),
        request.unitPrice(),
        request.productName());
    return ResponseEntity.ok(item);
  }

  @Operation(summary = "Update item quantity")
  @PatchMapping("/{cartId}/items/{itemId}")
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

  @Operation(summary = "Remove item from cart")
  @DeleteMapping("/{cartId}/items/{itemId}")
  public ResponseEntity<Void> removeItem(
      @PathVariable UUID cartId,
      @PathVariable UUID itemId) {
    cartService.removeItem(cartId, itemId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Clear all items from cart")
  @DeleteMapping("/{cartId}/items")
  public ResponseEntity<Void> clearCart(@PathVariable UUID cartId) {
    cartService.clearCart(cartId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Merge guest cart into customer cart")
  @PostMapping("/merge")
  public ResponseEntity<Cart> mergeCart(@RequestBody MergeCartRequest request) {
    Cart cart = cartService.mergeGuestCart(request.sessionId(), request.customerId());
    return ResponseEntity.ok(cart);
  }

  // Request DTOs
  public record AddItemRequest(
      Long productId,
      UUID variantId,
      int quantity,
      BigDecimal unitPrice,
      String productName
  ) {
  }

  public record UpdateQuantityRequest(int quantity) {
  }

  public record MergeCartRequest(
      String sessionId,
      Long customerId
  ) {
  }
}
