/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.order.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.order.domain.entity.Cart;
import com.xplaza.backend.order.domain.entity.CartItem;
import com.xplaza.backend.order.domain.repository.CartItemRepository;
import com.xplaza.backend.order.domain.repository.CartRepository;

/**
 * Service for shopping cart operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CartService {

  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;

  private static final int CART_EXPIRATION_HOURS = 72;

  /**
   * Get or create an active cart for a customer.
   */
  public Cart getOrCreateCart(Long customerId) {
    return cartRepository.findActiveCartByCustomerId(customerId)
        .orElseGet(() -> createCart(customerId, null));
  }

  /**
   * Get or create an active cart for a guest session.
   */
  public Cart getOrCreateGuestCart(String sessionId) {
    return cartRepository.findActiveCartBySessionId(sessionId)
        .orElseGet(() -> createCart(null, sessionId));
  }

  /**
   * Get cart by ID with items.
   */
  @Transactional(readOnly = true)
  public Optional<Cart> getCartWithItems(UUID cartId) {
    return cartRepository.findByIdWithItems(cartId);
  }

  /**
   * Add item to cart.
   */
  public CartItem addItem(UUID cartId, Long productId, UUID variantId, int quantity,
      BigDecimal unitPrice, String productName) {
    Cart cart = cartRepository.findById(cartId)
        .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + cartId));

    // Check if item already exists
    Optional<CartItem> existingItem = variantId != null
        ? cartItemRepository.findByCartIdAndProductIdAndVariantId(cartId, productId, variantId)
        : cartItemRepository.findByCartIdAndProductId(cartId, productId);

    CartItem item;
    if (existingItem.isPresent()) {
      item = existingItem.get();
      item.updateQuantity(item.getQuantity() + quantity);
    } else {
      item = CartItem.builder()
          .cart(cart)
          .productId(productId)
          .variantId(variantId)
          .shopId(1L) // Default shop, should be passed as parameter
          .quantity(quantity)
          .unitPrice(unitPrice)
          .priceAtAdd(unitPrice)
          .totalPrice(unitPrice.multiply(BigDecimal.valueOf(quantity)))
          .build();
      cart.addItem(item);
    }

    cartItemRepository.save(item);
    updateCartTotals(cart);
    cart.setUpdatedAt(Instant.now());
    cartRepository.save(cart);

    log.info("Added item to cart {}: productId={}, quantity={}", cartId, productId, quantity);
    return item;
  }

  /**
   * Update item quantity.
   */
  public CartItem updateItemQuantity(UUID cartId, UUID cartItemId, int quantity) {
    CartItem item = cartItemRepository.findById(cartItemId)
        .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + cartItemId));

    if (!item.getCart().getCartId().equals(cartId)) {
      throw new IllegalArgumentException("Item does not belong to cart");
    }

    if (quantity <= 0) {
      removeItem(cartId, cartItemId);
      return null;
    }

    item.updateQuantity(quantity);
    cartItemRepository.save(item);

    Cart cart = item.getCart();
    updateCartTotals(cart);
    cart.setUpdatedAt(Instant.now());
    cartRepository.save(cart);

    return item;
  }

  /**
   * Remove item from cart.
   */
  public void removeItem(UUID cartId, UUID cartItemId) {
    CartItem item = cartItemRepository.findById(cartItemId)
        .orElseThrow(() -> new IllegalArgumentException("Cart item not found: " + cartItemId));

    if (!item.getCart().getCartId().equals(cartId)) {
      throw new IllegalArgumentException("Item does not belong to cart");
    }

    Cart cart = item.getCart();
    cart.getItems().remove(item);
    cartItemRepository.delete(item);

    updateCartTotals(cart);
    cart.setUpdatedAt(Instant.now());
    cartRepository.save(cart);

    log.info("Removed item from cart {}: itemId={}", cartId, cartItemId);
  }

  /**
   * Clear all items from cart.
   */
  public void clearCart(UUID cartId) {
    Cart cart = cartRepository.findById(cartId)
        .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + cartId));

    cartItemRepository.deleteByCartId(cartId);
    cart.getItems().clear();
    updateCartTotals(cart);
    cart.setUpdatedAt(Instant.now());
    cartRepository.save(cart);

    log.info("Cleared cart: {}", cartId);
  }

  /**
   * Merge guest cart into customer cart.
   */
  public Cart mergeGuestCart(String sessionId, Long customerId) {
    Optional<Cart> guestCart = cartRepository.findActiveCartBySessionId(sessionId);
    Cart customerCart = getOrCreateCart(customerId);

    if (guestCart.isPresent() && !guestCart.get().getItems().isEmpty()) {
      Cart guest = guestCart.get();
      for (CartItem guestItem : guest.getItems()) {
        addItemInternal(customerCart,
            guestItem.getProductId(),
            guestItem.getVariantId(),
            guestItem.getQuantity(),
            guestItem.getUnitPrice());
      }
      // Mark guest cart as merged
      guest.markAsMerged();
      cartRepository.save(guest);
      log.info("Merged guest cart {} into customer cart {}", guest.getCartId(), customerCart.getCartId());
    }

    return customerCart;
  }

  /**
   * Convert cart to order.
   */
  public void convertToOrder(UUID cartId, UUID orderId) {
    Cart cart = cartRepository.findById(cartId)
        .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + cartId));

    cart.markAsConverted(orderId);
    cartRepository.save(cart);
    log.info("Converted cart {} to order {}", cartId, orderId);
  }

  private void addItemInternal(Cart cart, Long productId, UUID variantId, int quantity, BigDecimal unitPrice) {
    CartItem item = CartItem.builder()
        .cart(cart)
        .productId(productId)
        .variantId(variantId)
        .shopId(1L)
        .quantity(quantity)
        .unitPrice(unitPrice)
        .priceAtAdd(unitPrice)
        .totalPrice(unitPrice.multiply(BigDecimal.valueOf(quantity)))
        .build();
    cart.addItem(item);
    cartItemRepository.save(item);
  }

  /**
   * Expire old carts.
   */
  public int expireOldCarts() {
    int expired = cartRepository.expireCarts(Instant.now());
    log.info("Expired {} carts", expired);
    return expired;
  }

  /**
   * Mark inactive carts as abandoned.
   */
  public int markAbandonedCarts() {
    Instant cutoff = Instant.now().minus(24, ChronoUnit.HOURS);
    int abandoned = cartRepository.markCartsAsAbandoned(cutoff);
    log.info("Marked {} carts as abandoned", abandoned);
    return abandoned;
  }

  // Private helpers

  private Cart createCart(Long customerId, String sessionId) {
    Cart cart = Cart.builder()
        .customerId(customerId)
        .sessionId(sessionId)
        .expiresAt(Instant.now().plus(CART_EXPIRATION_HOURS, ChronoUnit.HOURS))
        .build();
    return cartRepository.save(cart);
  }

  private void updateCartTotals(Cart cart) {
    cart.recalculateTotals();
  }
}
