/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.customer.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.customer.domain.entity.Wishlist;
import com.xplaza.backend.customer.domain.entity.WishlistItem;
import com.xplaza.backend.customer.service.WishlistService;

/**
 * REST controller for wishlist operations.
 */
@RestController
@RequestMapping("/api/v1/wishlists")
@RequiredArgsConstructor
@Tag(name = "Wishlist", description = "Customer wishlist management")
public class WishlistController {

  private final WishlistService wishlistService;

  @Operation(summary = "Get all wishlists for a customer")
  @GetMapping("/customer/{customerId}")
  public ResponseEntity<List<Wishlist>> getCustomerWishlists(@PathVariable Long customerId) {
    List<Wishlist> wishlists = wishlistService.getCustomerWishlists(customerId);
    return ResponseEntity.ok(wishlists);
  }

  @Operation(summary = "Get default wishlist for a customer")
  @GetMapping("/customer/{customerId}/default")
  public ResponseEntity<Wishlist> getDefaultWishlist(@PathVariable Long customerId) {
    Wishlist wishlist = wishlistService.getOrCreateDefaultWishlist(customerId);
    return ResponseEntity.ok(wishlist);
  }

  @Operation(summary = "Create a new wishlist")
  @PostMapping
  public ResponseEntity<Wishlist> createWishlist(@Valid @RequestBody CreateWishlistRequest request) {
    Wishlist wishlist = wishlistService.createWishlist(request.customerId(), request.name());
    return ResponseEntity.ok(wishlist);
  }

  @Operation(summary = "Get wishlist by ID with items")
  @GetMapping("/{wishlistId}")
  public ResponseEntity<Wishlist> getWishlist(@PathVariable UUID wishlistId) {
    return wishlistService.getWishlistWithItems(wishlistId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Get shared wishlist by token")
  @GetMapping("/shared/{shareToken}")
  public ResponseEntity<Wishlist> getSharedWishlist(@PathVariable String shareToken) {
    return wishlistService.getPublicWishlist(shareToken)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Update wishlist settings")
  @PatchMapping("/{wishlistId}")
  public ResponseEntity<Wishlist> updateWishlist(
      @PathVariable UUID wishlistId,
      @RequestBody UpdateWishlistRequest request) {
    Wishlist wishlist = wishlistService.updateWishlist(
        wishlistId, request.name(), request.visibility());
    return ResponseEntity.ok(wishlist);
  }

  @Operation(summary = "Delete a wishlist")
  @DeleteMapping("/{wishlistId}")
  public ResponseEntity<Void> deleteWishlist(
      @PathVariable UUID wishlistId,
      @RequestParam Long customerId) {
    wishlistService.deleteWishlist(wishlistId, customerId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Add item to wishlist")
  @PostMapping("/{wishlistId}/items")
  public ResponseEntity<WishlistItem> addItem(
      @PathVariable UUID wishlistId,
      @Valid @RequestBody AddWishlistItemRequest request) {
    WishlistItem item = wishlistService.addItem(
        wishlistId,
        request.productId(),
        request.variantId(),
        request.priceAtAdd());
    return ResponseEntity.ok(item);
  }

  @Operation(summary = "Remove item from wishlist")
  @DeleteMapping("/{wishlistId}/items/{itemId}")
  public ResponseEntity<Void> removeItem(
      @PathVariable UUID wishlistId,
      @PathVariable UUID itemId) {
    wishlistService.removeItem(wishlistId, itemId);
    return ResponseEntity.noContent().build();
  }

  @Operation(summary = "Move item to another wishlist")
  @PostMapping("/{wishlistId}/items/{itemId}/move")
  public ResponseEntity<WishlistItem> moveItem(
      @PathVariable UUID wishlistId,
      @PathVariable UUID itemId,
      @RequestBody MoveItemRequest request) {
    WishlistItem item = wishlistService.moveItem(wishlistId, itemId, request.targetWishlistId());
    return ResponseEntity.ok(item);
  }

  @Operation(summary = "Check if product is in customer's wishlists")
  @GetMapping("/customer/{customerId}/contains/{productId}")
  public ResponseEntity<Boolean> isInWishlist(
      @PathVariable Long customerId,
      @PathVariable Long productId) {
    boolean inWishlist = wishlistService.isInWishlist(customerId, productId);
    return ResponseEntity.ok(inWishlist);
  }

  @Operation(summary = "Enable price drop notification")
  @PostMapping("/items/{itemId}/notify-price-drop")
  public ResponseEntity<Void> enablePriceDropNotification(@PathVariable UUID itemId) {
    wishlistService.enablePriceDropNotification(itemId);
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Enable back-in-stock notification")
  @PostMapping("/items/{itemId}/notify-back-in-stock")
  public ResponseEntity<Void> enableBackInStockNotification(@PathVariable UUID itemId) {
    wishlistService.enableBackInStockNotification(itemId);
    return ResponseEntity.ok().build();
  }

  // Request DTOs
  public record CreateWishlistRequest(
      Long customerId,
      String name
  ) {
  }

  public record UpdateWishlistRequest(
      String name,
      Wishlist.WishlistVisibility visibility
  ) {
  }

  public record AddWishlistItemRequest(
      Long productId,
      UUID variantId,
      BigDecimal priceAtAdd
  ) {
  }

  public record MoveItemRequest(UUID targetWishlistId) {
  }
}
