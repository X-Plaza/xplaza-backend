/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.cart.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.xplaza.backend.cart.domain.entity.Cart;
import com.xplaza.backend.cart.domain.entity.CartItem;
import com.xplaza.backend.cart.domain.repository.CartItemRepository;
import com.xplaza.backend.cart.domain.repository.CartRepository;
import com.xplaza.backend.catalog.domain.entity.Product;
import com.xplaza.backend.catalog.domain.entity.ProductVariant;
import com.xplaza.backend.catalog.domain.repository.ProductRepository;
import com.xplaza.backend.catalog.domain.repository.ProductVariantRepository;
import com.xplaza.backend.inventory.service.InventoryService;
import com.xplaza.backend.promotion.service.ProductDiscountService;
import com.xplaza.backend.shop.domain.entity.Shop;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

  @Mock
  private CartRepository cartRepository;

  @Mock
  private CartItemRepository cartItemRepository;

  @Mock
  private ProductRepository productRepository;

  @Mock
  private ProductVariantRepository productVariantRepository;

  @Mock
  private InventoryService inventoryService;

  @Mock
  private ProductDiscountService productDiscountService;

  @InjectMocks
  private CartService cartService;

  private Cart activeCart;
  private Long customerId = 101L;
  private UUID cartId = UUID.randomUUID();

  @BeforeEach
  void setUp() {
    activeCart = Cart.builder()
        .id(cartId)
        .customerId(customerId)
        .status(Cart.CartStatus.ACTIVE)
        .items(new ArrayList<>())
        .build();
  }

  @Test
  void getOrCreateCart_ShouldReturnExisting_WhenActiveCartExists() {
    given(cartRepository.findActiveCartByCustomerId(customerId))
        .willReturn(Optional.of(activeCart));

    Cart result = cartService.getOrCreateCart(customerId);

    assertThat(result).isSameAs(activeCart);
    verify(cartRepository, never()).save(any(Cart.class));
  }

  @Test
  void getOrCreateCart_ShouldCreateNew_WhenNoActiveCartExists() {
    given(cartRepository.findActiveCartByCustomerId(customerId))
        .willReturn(Optional.empty());
    given(cartRepository.save(any(Cart.class))).willAnswer(invocation -> invocation.getArgument(0));

    Cart result = cartService.getOrCreateCart(customerId);

    assertThat(result.getCustomerId()).isEqualTo(customerId);
    assertThat(result.getStatus()).isEqualTo(Cart.CartStatus.ACTIVE);
    verify(cartRepository).save(any(Cart.class));
  }

  @Test
  void addItem_ShouldAddNewItem_WhenItemDoesNotExist() {
    Long productId = 500L;
    UUID variantId = UUID.randomUUID();
    Long shopId = 20L;

    given(cartRepository.findByIdWithItems(cartId)).willReturn(Optional.of(activeCart));

    Product product = new Product();
    product.setProductId(productId);
    product.setProductSellingPrice(100.00);
    Shop shop = new Shop();
    shop.setShopId(shopId);
    product.setShop(shop);
    given(productRepository.findById(productId)).willReturn(Optional.of(product));
    given(productDiscountService.calculateDiscountedPrice(product)).willReturn(BigDecimal.valueOf(100.00));

    ProductVariant variant = new ProductVariant();
    variant.setVariantId(variantId);
    variant.setPrice(BigDecimal.valueOf(100.00));
    given(productVariantRepository.findById(variantId)).willReturn(Optional.of(variant));

    given(inventoryService.getAvailableQuantityByVariant(variantId)).willReturn(10);
    given(cartItemRepository.save(any(CartItem.class))).willAnswer(invocation -> invocation.getArgument(0));

    CartItem result = cartService.addItem(
        cartId, productId, variantId, shopId, 2, BigDecimal.valueOf(100.00),
        "Test Product", "Blue", "SKU-123", "http://img.com");

    assertThat(activeCart.getItems()).hasSize(1);
    assertThat(result.getProductId()).isEqualTo(productId);
    assertThat(result.getQuantity()).isEqualTo(2);
    verify(cartItemRepository).save(any(CartItem.class));
  }

  @Test
  void addItem_ShouldIncrementQuantity_WhenItemExists() {
    Long productId = 500L;
    UUID variantId = UUID.randomUUID();
    Long shopId = 20L;

    CartItem existingItem = CartItem.builder()
        .id(UUID.randomUUID())
        .cart(activeCart)
        .productId(productId)
        .variantId(variantId)
        .quantity(1)
        .status(CartItem.ItemStatus.ACTIVE)
        .unitPrice(BigDecimal.valueOf(100.00))
        .build();
    activeCart.addCartItem(existingItem);

    given(cartRepository.findByIdWithItems(cartId)).willReturn(Optional.of(activeCart));

    Product product = new Product();
    product.setProductId(productId);
    product.setProductSellingPrice(100.00);
    given(productRepository.findById(productId)).willReturn(Optional.of(product));
    given(productDiscountService.calculateDiscountedPrice(product)).willReturn(BigDecimal.valueOf(100.00));

    ProductVariant variant = new ProductVariant();
    variant.setVariantId(variantId);
    variant.setPrice(BigDecimal.valueOf(100.00));
    given(productVariantRepository.findById(variantId)).willReturn(Optional.of(variant));

    given(inventoryService.getAvailableQuantityByVariant(variantId)).willReturn(10);

    given(cartItemRepository.save(any(CartItem.class))).willAnswer(invocation -> invocation.getArgument(0));

    CartItem result = cartService.addItem(
        cartId, productId, variantId, shopId, 2, BigDecimal.valueOf(100.00),
        "Test Product", "Blue", "SKU-123", "http://img.com");

    assertThat(result).isSameAs(existingItem);
    assertThat(result.getQuantity()).isEqualTo(3); // 1 + 2
    verify(cartItemRepository).save(existingItem);
    // cart repo save is not called for existing item update in this impl, but
    // cartItemRepo save is
  }

  @Test
  void updateItemQuantity_ShouldUpdateQuantity_WhenItemExists() {
    UUID itemId = UUID.randomUUID();
    CartItem existingItem = CartItem.builder()
        .id(itemId)
        .cart(activeCart)
        .productId(500L)
        .quantity(1)
        .build();
    activeCart.addCartItem(existingItem);

    given(cartRepository.findByIdWithItems(cartId)).willReturn(Optional.of(activeCart));
    given(cartItemRepository.save(any(CartItem.class))).willAnswer(invocation -> invocation.getArgument(0));

    CartItem result = cartService.updateItemQuantity(cartId, itemId, 5);

    assertThat(result.getQuantity()).isEqualTo(5);
    verify(cartItemRepository).save(existingItem);
  }

  @Test
  void updateItemQuantity_ShouldRemoveItem_WhenQuantityIsZero() {
    UUID itemId = UUID.randomUUID();
    CartItem existingItem = CartItem.builder()
        .id(itemId)
        .cart(activeCart)
        .productId(500L)
        .quantity(1)
        .build();
    // Using a modifiable list for items since the service will modify it
    activeCart.setItems(new ArrayList<>(List.of(existingItem)));

    given(cartRepository.findByIdWithItems(cartId)).willReturn(Optional.of(activeCart));

    CartItem result = cartService.updateItemQuantity(cartId, itemId, 0);

    assertThat(result).isNull();
    assertThat(activeCart.getItems()).isEmpty();
    verify(cartRepository).save(activeCart);
  }
}
