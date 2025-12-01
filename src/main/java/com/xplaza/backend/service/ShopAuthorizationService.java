/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.xplaza.backend.exception.ResourceNotFoundException;
import com.xplaza.backend.jpa.repository.AdminUserShopLinkRepository;
import com.xplaza.backend.jpa.repository.OrderRepository;
import com.xplaza.backend.jpa.repository.ProductRepository;

/**
 * Service for validating shop-level authorization.
 * 
 * In a multi-vendor e-commerce platform, it's critical to ensure that: - Shop
 * admins can only access their own shop's data - Products, orders, and other
 * resources are properly isolated - Cross-shop access is prevented
 * 
 * This service provides centralized authorization checks that should be called
 * from controllers before performing any shop-specific operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ShopAuthorizationService {

  private final AdminUserShopLinkRepository adminUserShopLinkRepository;
  private final OrderRepository orderRepository;
  private final ProductRepository productRepository;

  /**
   * Validates that the given user has access to the specified shop.
   * 
   * @param userId the ID of the user attempting access
   * @param shopId the ID of the shop being accessed
   * @throws AccessDeniedException if user does not have access to the shop
   */
  public void validateUserCanAccessShop(Long userId, Long shopId) {
    if (userId == null || shopId == null) {
      throw new IllegalArgumentException("User ID and Shop ID must not be null");
    }

    boolean hasAccess = adminUserShopLinkRepository.existsByAdminUserIdAndShopId(userId, shopId);
    if (!hasAccess) {
      log.warn("Unauthorized shop access attempt: userId={}, shopId={}", userId, shopId);
      throw new AccessDeniedException("User does not have access to shop: " + shopId);
    }
  }

  /**
   * Validates that the given user has access to the order. This checks that the
   * user has access to the shop that owns the order.
   * 
   * @param userId  the ID of the user attempting access
   * @param orderId the ID of the order being accessed
   * @throws AccessDeniedException     if user does not have access
   * @throws ResourceNotFoundException if order is not found
   */
  public void validateUserCanAccessOrder(Long userId, Long orderId) {
    if (userId == null || orderId == null) {
      throw new IllegalArgumentException("User ID and Order ID must not be null");
    }

    Long shopId = orderRepository.findShopIdByOrderId(orderId);
    if (shopId == null) {
      throw new ResourceNotFoundException("Order not found with id: " + orderId);
    }

    validateUserCanAccessShop(userId, shopId);
  }

  /**
   * Validates that the given user has access to the product. This checks that the
   * user has access to the shop that owns the product.
   * 
   * @param userId    the ID of the user attempting access
   * @param productId the ID of the product being accessed
   * @throws AccessDeniedException     if user does not have access
   * @throws ResourceNotFoundException if product is not found
   */
  public void validateUserCanAccessProduct(Long userId, Long productId) {
    if (userId == null || productId == null) {
      throw new IllegalArgumentException("User ID and Product ID must not be null");
    }

    Long shopId = productRepository.findShopIdByProductId(productId);
    if (shopId == null) {
      throw new ResourceNotFoundException("Product not found with id: " + productId);
    }

    validateUserCanAccessShop(userId, shopId);
  }

  /**
   * Checks if the user is a Master Admin (has access to all shops).
   * 
   * @param userId the ID of the user to check
   * @return true if user is a Master Admin
   */
  public boolean isMasterAdmin(Long userId) {
    if (userId == null) {
      return false;
    }
    // Master Admin typically has a special role or access to all shops
    // This can be implemented based on role checking
    return adminUserShopLinkRepository.countShopsByUserId(userId) == 0
        && hasSystemAdminRole(userId);
  }

  /**
   * Internal method to check if user has system admin role. Override this based
   * on your role management implementation.
   */
  private boolean hasSystemAdminRole(Long userId) {
    // This would typically check against a role service
    // For now, we return false as the default behavior
    return false;
  }
}
