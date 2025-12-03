/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.customer.service;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.customer.domain.entity.Wishlist;
import com.xplaza.backend.customer.domain.entity.WishlistItem;
import com.xplaza.backend.customer.domain.repository.WishlistItemRepository;
import com.xplaza.backend.customer.domain.repository.WishlistRepository;

/**
 * Service for wishlist operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class WishlistService {

  private final WishlistRepository wishlistRepository;
  private final WishlistItemRepository wishlistItemRepository;

  private static final int MAX_WISHLISTS_PER_CUSTOMER = 10;

  /**
   * Get or create default wishlist for customer.
   */
  public Wishlist getOrCreateDefaultWishlist(Long customerId) {
    List<Wishlist> wishlists = wishlistRepository.findByCustomerId(customerId);
    if (wishlists.isEmpty()) {
      return createWishlist(customerId, "My Wishlist");
    }
    return wishlists.get(0);
  }

  /**
   * Create a new wishlist.
   */
  public Wishlist createWishlist(Long customerId, String name) {
    long count = wishlistRepository.countByCustomerId(customerId);
    if (count >= MAX_WISHLISTS_PER_CUSTOMER) {
      throw new IllegalStateException("Maximum wishlists limit reached");
    }

    Wishlist wishlist = Wishlist.builder()
        .customerId(customerId)
        .name(name)
        .build();

    return wishlistRepository.save(wishlist);
  }

  /**
   * Get wishlist by ID with items.
   */
  @Transactional(readOnly = true)
  public Optional<Wishlist> getWishlistWithItems(UUID wishlistId) {
    return wishlistRepository.findByIdWithItems(wishlistId);
  }

  /**
   * Get all wishlists for a customer.
   */
  @Transactional(readOnly = true)
  public List<Wishlist> getCustomerWishlists(Long customerId) {
    return wishlistRepository.findByCustomerId(customerId);
  }

  /**
   * Get public wishlist by share token.
   */
  @Transactional(readOnly = true)
  public Optional<Wishlist> getPublicWishlist(String shareToken) {
    return wishlistRepository.findByShareToken(shareToken);
  }

  /**
   * Add item to wishlist.
   */
  public WishlistItem addItem(UUID wishlistId, Long productId, UUID variantId, BigDecimal priceAtAdd) {
    Wishlist wishlist = wishlistRepository.findById(wishlistId)
        .orElseThrow(() -> new IllegalArgumentException("Wishlist not found: " + wishlistId));

    // Check if item already exists
    Optional<WishlistItem> existing = variantId != null
        ? wishlistItemRepository.findByWishlistIdAndProductIdAndVariantId(wishlistId, productId, variantId)
        : wishlistItemRepository.findByWishlistIdAndProductId(wishlistId, productId);

    if (existing.isPresent()) {
      return existing.get(); // Already in wishlist
    }

    WishlistItem item = WishlistItem.builder()
        .wishlist(wishlist)
        .productId(productId)
        .variantId(variantId)
        .priceAtAdd(priceAtAdd)
        .build();

    wishlist.addItem(item);
    wishlistRepository.save(wishlist);

    log.info("Added item to wishlist {}: productId={}", wishlistId, productId);
    return item;
  }

  /**
   * Remove item from wishlist.
   */
  public void removeItem(UUID wishlistId, UUID itemId) {
    WishlistItem item = wishlistItemRepository.findById(itemId)
        .orElseThrow(() -> new IllegalArgumentException("Wishlist item not found: " + itemId));

    if (!item.getWishlist().getWishlistId().equals(wishlistId)) {
      throw new IllegalArgumentException("Item does not belong to wishlist");
    }

    Wishlist wishlist = item.getWishlist();
    wishlist.getItems().remove(item);
    wishlistItemRepository.delete(item);
    wishlistRepository.save(wishlist);

    log.info("Removed item from wishlist {}: itemId={}", wishlistId, itemId);
  }

  /**
   * Move item to another wishlist.
   */
  public WishlistItem moveItem(UUID sourceWishlistId, UUID itemId, UUID targetWishlistId) {
    WishlistItem item = wishlistItemRepository.findById(itemId)
        .orElseThrow(() -> new IllegalArgumentException("Wishlist item not found: " + itemId));

    Wishlist targetWishlist = wishlistRepository.findById(targetWishlistId)
        .orElseThrow(() -> new IllegalArgumentException("Target wishlist not found: " + targetWishlistId));

    // Create new item in target
    WishlistItem newItem = addItem(targetWishlistId, item.getProductId(), item.getVariantId(),
        item.getPriceAtAdd());

    // Remove from source
    removeItem(sourceWishlistId, itemId);

    return newItem;
  }

  /**
   * Update wishlist settings.
   */
  public Wishlist updateWishlist(UUID wishlistId, String name, Wishlist.WishlistVisibility visibility) {
    Wishlist wishlist = wishlistRepository.findById(wishlistId)
        .orElseThrow(() -> new IllegalArgumentException("Wishlist not found: " + wishlistId));

    if (name != null) {
      wishlist.setName(name);
    }
    if (visibility != null) {
      wishlist.setVisibility(visibility);
      if (visibility == Wishlist.WishlistVisibility.SHARED) {
        wishlist.makeShareable();
      }
    }

    return wishlistRepository.save(wishlist);
  }

  /**
   * Delete wishlist.
   */
  public void deleteWishlist(UUID wishlistId, Long customerId) {
    Wishlist wishlist = wishlistRepository.findById(wishlistId)
        .orElseThrow(() -> new IllegalArgumentException("Wishlist not found: " + wishlistId));

    // Verify ownership
    if (!wishlist.getCustomerId().equals(customerId)) {
      throw new IllegalArgumentException("Wishlist does not belong to customer");
    }

    wishlistItemRepository.deleteByWishlistId(wishlistId);
    wishlistRepository.delete(wishlist);

    log.info("Deleted wishlist: {}", wishlistId);
  }

  /**
   * Check if product is in any of customer's wishlists.
   */
  @Transactional(readOnly = true)
  public boolean isInWishlist(Long customerId, Long productId) {
    List<Wishlist> wishlists = wishlistRepository.findByCustomerId(customerId);
    for (Wishlist wishlist : wishlists) {
      if (wishlistItemRepository.findByWishlistIdAndProductId(wishlist.getWishlistId(), productId).isPresent()) {
        return true;
      }
    }
    return false;
  }

  /**
   * Get items needing price drop notification for a product.
   */
  @Transactional(readOnly = true)
  public List<WishlistItem> getItemsForPriceDropNotification(Long productId) {
    return wishlistItemRepository.findByProductIdWithPriceDropNotification(productId);
  }

  /**
   * Get items needing back-in-stock notification for a product.
   */
  @Transactional(readOnly = true)
  public List<WishlistItem> getItemsForBackInStockNotification(Long productId) {
    return wishlistItemRepository.findByProductIdWithBackInStockNotification(productId);
  }

  /**
   * Enable price drop notification for an item.
   */
  public void enablePriceDropNotification(UUID itemId) {
    WishlistItem item = wishlistItemRepository.findById(itemId)
        .orElseThrow(() -> new IllegalArgumentException("Wishlist item not found: " + itemId));
    item.setNotifyPriceDrop(true);
    wishlistItemRepository.save(item);
  }

  /**
   * Enable back-in-stock notification for an item.
   */
  public void enableBackInStockNotification(UUID itemId) {
    WishlistItem item = wishlistItemRepository.findById(itemId)
        .orElseThrow(() -> new IllegalArgumentException("Wishlist item not found: " + itemId));
    item.setNotifyBackInStock(true);
    wishlistItemRepository.save(item);
  }
}
