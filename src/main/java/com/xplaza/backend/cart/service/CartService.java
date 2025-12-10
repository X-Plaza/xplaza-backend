/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.cart.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.cart.domain.entity.Cart;
import com.xplaza.backend.cart.domain.entity.Cart.CartStatus;
import com.xplaza.backend.cart.domain.entity.CartItem;
import com.xplaza.backend.cart.domain.repository.CartItemRepository;
import com.xplaza.backend.cart.domain.repository.CartRepository;
import com.xplaza.backend.catalog.domain.entity.Product;
import com.xplaza.backend.catalog.domain.repository.ProductRepository;
import com.xplaza.backend.catalog.domain.repository.ProductVariantRepository;
import com.xplaza.backend.inventory.service.InventoryService;
import com.xplaza.backend.promotion.service.ProductDiscountService;

/**
 * Service for shopping cart operations.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class CartService {

  private final CartRepository cartRepository;
  private final CartItemRepository cartItemRepository;
  private final ProductRepository productRepository;
  private final ProductVariantRepository productVariantRepository;
  private final InventoryService inventoryService;
  private final ProductDiscountService productDiscountService;

  /** Default cart expiration in days */
  private static final int DEFAULT_CART_EXPIRATION_DAYS = 30;

  // ==================== Cart Operations ====================

  /**
   * Get or create active cart for an authenticated customer.
   */
  public Cart getOrCreateCart(Long customerId) {
    return cartRepository.findActiveCartByCustomerId(customerId)
        .orElseGet(() -> createCart(customerId, null));
  }

  /**
   * Get or create active cart for a guest (by session ID).
   */
  public Cart getOrCreateGuestCart(String sessionId) {
    return cartRepository.findActiveCartBySessionId(sessionId)
        .orElseGet(() -> createCart(null, sessionId));
  }

  /**
   * Create a new cart.
   */
  private Cart createCart(Long customerId, String sessionId) {
    Cart cart = Cart.builder()
        .customerId(customerId)
        .sessionId(sessionId)
        .status(CartStatus.ACTIVE)
        .expiresAt(Instant.now().plus(DEFAULT_CART_EXPIRATION_DAYS, ChronoUnit.DAYS))
        .build();
    return cartRepository.save(cart);
  }

  /**
   * Get cart by ID with items.
   */
  @Transactional(readOnly = true)
  public Optional<Cart> getCart(UUID cartId) {
    return cartRepository.findByIdWithItems(cartId);
  }

  /**
   * Get active cart for customer with items.
   */
  @Transactional(readOnly = true)
  public Optional<Cart> getActiveCartForCustomer(Long customerId) {
    return cartRepository.findActiveCartByCustomerIdWithItems(customerId);
  }

  /**
   * Get active cart for session with items.
   */
  @Transactional(readOnly = true)
  public Optional<Cart> getActiveCartForSession(String sessionId) {
    return cartRepository.findActiveCartBySessionIdWithItems(sessionId);
  }

  // ==================== Item Operations ====================

  /**
   * Add item to cart.
   */
  public CartItem addItem(UUID cartId, Long productId, UUID variantId, Long shopId,
      int quantity, BigDecimal unitPrice, String productName, String variantName,
      String sku, String imageUrl) {
    Cart cart = cartRepository.findByIdWithItems(cartId)
        .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + cartId));

    if (!cart.isActive()) {
      throw new IllegalStateException("Cannot add items to inactive cart");
    }

    // Validate product and price from DB
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new IllegalArgumentException("Product not found: " + productId));

    // Use price from DB to prevent tampering, applying any active discounts
    BigDecimal actualPrice = productDiscountService.calculateDiscountedPrice(product);

    if (variantId != null) {
      actualPrice = productVariantRepository.findById(variantId)
          .map(v -> v.getPrice())
          .orElseThrow(() -> new IllegalArgumentException("Variant not found: " + variantId));
    }

    // Check inventory
    int availableStock = variantId != null
        ? inventoryService.getAvailableQuantityByVariant(variantId)
        : inventoryService.getAvailableQuantity(productId);

    if (availableStock < quantity) {
      throw new IllegalStateException("Insufficient stock. Available: " + availableStock);
    }

    // Check if item already exists
    CartItem existingItem = cart.findItem(productId, variantId);
    if (existingItem != null && existingItem.isActive()) {
      int newTotal = existingItem.getQuantity() + quantity;
      if (availableStock < newTotal) {
        throw new IllegalStateException("Insufficient stock for total quantity. Available: " + availableStock);
      }
      existingItem.incrementQuantity(quantity);
      return cartItemRepository.save(existingItem);
    }

    // Create new item
    CartItem item = CartItem.builder()
        .cart(cart)
        .productId(productId)
        .variantId(variantId)
        .shopId(product.getShop().getShopId()) // Use shop from product
        .quantity(quantity)
        .unitPrice(actualPrice)
        .productName(product.getProductName())
        .variantName(variantName)
        .sku("SKU-" + productId) // Placeholder as Product entity lacks SKU
        .imageUrl(product.getImages() != null && !product.getImages().isEmpty()
            ? product.getImages().get(0).getProductImagePath()
            : null)
        .build();

    cart.addCartItem(item);
    // Save item directly to ensure ID is generated and returned
    return cartItemRepository.save(item);
  }

  /**
   * Update item quantity.
   */
  public CartItem updateItemQuantity(UUID cartId, UUID itemId, int newQuantity) {
    Cart cart = cartRepository.findByIdWithItems(cartId)
        .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + cartId));

    CartItem item = cart.getItem(itemId);
    if (item == null) {
      throw new IllegalArgumentException("Item not found in cart: " + itemId);
    }

    if (newQuantity <= 0) {
      cart.removeItem(itemId);
      cartRepository.save(cart);
      return null;
    }

    item.setQuantity(newQuantity);
    return cartItemRepository.save(item);
  }

  /**
   * Remove item from cart.
   */
  public void removeItem(UUID cartId, UUID itemId) {
    Cart cart = cartRepository.findByIdWithItems(cartId)
        .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + cartId));

    if (!cart.removeItem(itemId)) {
      throw new IllegalArgumentException("Item not found in cart: " + itemId);
    }

    cartRepository.save(cart);
  }

  /**
   * Save item for later.
   */
  public CartItem saveForLater(UUID cartId, UUID itemId) {
    CartItem item = getCartItem(cartId, itemId);
    item.saveForLater();
    return cartItemRepository.save(item);
  }

  /**
   * Move item back to cart from saved for later.
   */
  public CartItem moveToCart(UUID cartId, UUID itemId) {
    CartItem item = getCartItem(cartId, itemId);
    item.moveToCart();
    return cartItemRepository.save(item);
  }

  private CartItem getCartItem(UUID cartId, UUID itemId) {
    Cart cart = cartRepository.findByIdWithItems(cartId)
        .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + cartId));

    CartItem item = cart.getItem(itemId);
    if (item == null) {
      throw new IllegalArgumentException("Item not found in cart: " + itemId);
    }
    return item;
  }

  // ==================== Cart Actions ====================

  /**
   * Clear cart (remove all items).
   */
  public void clearCart(UUID cartId) {
    Cart cart = cartRepository.findByIdWithItems(cartId)
        .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + cartId));

    cart.clear();
    cartRepository.save(cart);
  }

  /**
   * Apply coupon to cart.
   */
  public Cart applyCoupon(UUID cartId, String couponCode, BigDecimal discountAmount) {
    Cart cart = cartRepository.findById(cartId)
        .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + cartId));

    cart.applyCoupon(couponCode, discountAmount);
    return cartRepository.save(cart);
  }

  /**
   * Remove coupon from cart.
   */
  public Cart removeCoupon(UUID cartId) {
    Cart cart = cartRepository.findById(cartId)
        .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + cartId));

    cart.removeCoupon();
    return cartRepository.save(cart);
  }

  /**
   * Merge guest cart into customer cart.
   */
  public Cart mergeGuestCart(String sessionId, Long customerId) {
    Optional<Cart> guestCartOpt = cartRepository.findActiveCartBySessionIdWithItems(sessionId);
    if (guestCartOpt.isEmpty()) {
      return getOrCreateCart(customerId);
    }

    Cart guestCart = guestCartOpt.get();
    Cart customerCart = getOrCreateCart(customerId);

    // Merge items
    for (CartItem guestItem : guestCart.getActiveItems()) {
      CartItem existingItem = customerCart.findItem(guestItem.getProductId(), guestItem.getVariantId());
      if (existingItem != null && existingItem.isActive()) {
        // Add quantities
        existingItem.incrementQuantity(guestItem.getQuantity());
      } else {
        // Copy item to customer cart
        CartItem newItem = CartItem.builder()
            .cart(customerCart)
            .productId(guestItem.getProductId())
            .variantId(guestItem.getVariantId())
            .shopId(guestItem.getShopId())
            .quantity(guestItem.getQuantity())
            .unitPrice(guestItem.getUnitPrice())
            .originalPrice(guestItem.getOriginalPrice())
            .productName(guestItem.getProductName())
            .variantName(guestItem.getVariantName())
            .sku(guestItem.getSku())
            .imageUrl(guestItem.getImageUrl())
            .build();
        customerCart.getItems().add(newItem);
      }
    }

    // Mark guest cart as merged
    guestCart.markMerged();
    cartRepository.save(guestCart);

    return cartRepository.save(customerCart);
  }

  /**
   * Mark cart as converted (when order is created).
   */
  public void markCartConverted(UUID cartId) {
    Cart cart = cartRepository.findById(cartId)
        .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + cartId));

    cart.markConverted();
    cartRepository.save(cart);
  }

  // ==================== Cart Calculations ====================

  /**
   * Get cart summary.
   */
  @Transactional(readOnly = true)
  public CartSummary getCartSummary(UUID cartId) {
    Cart cart = cartRepository.findByIdWithItems(cartId)
        .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + cartId));

    return new CartSummary(
        cart.getId(),
        cart.getUniqueItemCount(),
        cart.getTotalItemCount(),
        cart.getSubtotal(),
        cart.getCouponCode(),
        cart.getCouponDiscount(),
        cart.getTotal());
  }

  // ==================== Maintenance ====================

  /**
   * Mark abandoned carts.
   */
  public int markAbandonedCarts() {
    return cartRepository.markAbandonedCarts(Instant.now());
  }

  /**
   * Delete old abandoned carts.
   */
  public int deleteOldAbandonedCarts(int daysOld) {
    return cartRepository.deleteOldAbandonedCarts(Instant.now().minus(daysOld, ChronoUnit.DAYS));
  }

  /**
   * Find inactive carts for reminder.
   */
  @Transactional(readOnly = true)
  public List<Cart> findInactiveCarts(int daysInactive) {
    return cartRepository.findInactiveCarts(Instant.now().minus(daysInactive, ChronoUnit.DAYS));
  }

  // ==================== DTOs ====================

  public record CartSummary(
      UUID cartId,
      int uniqueItemCount,
      int totalItemCount,
      BigDecimal subtotal,
      String couponCode,
      BigDecimal couponDiscount,
      BigDecimal total
  ) {
  }
}
