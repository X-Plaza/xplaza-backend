/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.order.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.order.domain.entity.Cart;
import com.xplaza.backend.order.domain.entity.CheckoutSession;
import com.xplaza.backend.order.domain.entity.CustomerOrder;
import com.xplaza.backend.order.domain.repository.CartRepository;
import com.xplaza.backend.order.domain.repository.CheckoutSessionRepository;

/**
 * Service for checkout operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CheckoutService {

  private final CheckoutSessionRepository checkoutSessionRepository;
  private final CartRepository cartRepository;
  private final CustomerOrderService customerOrderService;

  /**
   * Start a new checkout session for a cart.
   */
  public CheckoutSession startCheckout(UUID cartId, Long customerId) {
    // Check if cart exists and has items
    Cart cart = cartRepository.findByIdWithItems(cartId)
        .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + cartId));

    if (cart.isEmpty()) {
      throw new IllegalStateException("Cannot checkout with an empty cart");
    }

    // Check for existing active checkout
    Optional<CheckoutSession> existingCheckout = checkoutSessionRepository.findActiveCheckoutByCartId(cartId);
    if (existingCheckout.isPresent()) {
      CheckoutSession existing = existingCheckout.get();
      // Refresh expiration
      existing.setDefaultExpiration();
      return checkoutSessionRepository.save(existing);
    }

    // Create new checkout session
    CheckoutSession checkout = CheckoutSession.builder()
        .cartId(cartId)
        .customerId(customerId)
        .subtotal(cart.getSubtotal())
        .discountAmount(cart.getDiscountTotal())
        .currency(cart.getCurrency())
        .build();

    checkout.setDefaultExpiration();
    checkout.calculateGrandTotal();

    CheckoutSession saved = checkoutSessionRepository.save(checkout);
    log.info("Started checkout session {} for cart {}", saved.getCheckoutId(), cartId);

    return saved;
  }

  /**
   * Get checkout session by ID.
   */
  @Transactional(readOnly = true)
  public Optional<CheckoutSession> getCheckout(UUID checkoutId) {
    return checkoutSessionRepository.findById(checkoutId);
  }

  /**
   * Set shipping address for checkout.
   */
  public CheckoutSession setShippingAddress(UUID checkoutId, Long addressId) {
    CheckoutSession checkout = getActiveCheckout(checkoutId);
    checkout.setShippingAddressId(addressId);
    checkout.setShippingCompleted(true);
    checkout.setCurrentStep("PAYMENT");
    checkout.setStatus(CheckoutSession.CheckoutStatus.SHIPPING_SELECTED);
    return checkoutSessionRepository.save(checkout);
  }

  /**
   * Set shipping method and cost.
   */
  public CheckoutSession setShippingMethod(UUID checkoutId, Long methodId, String methodName, BigDecimal cost) {
    CheckoutSession checkout = getActiveCheckout(checkoutId);
    checkout.setShippingMethodId(methodId);
    checkout.setShippingMethodName(methodName);
    checkout.setShippingCost(cost);
    checkout.calculateGrandTotal();
    return checkoutSessionRepository.save(checkout);
  }

  /**
   * Set delivery schedule.
   */
  public CheckoutSession setDeliverySchedule(UUID checkoutId, LocalDate date, LocalTime slotStart, LocalTime slotEnd,
      String instructions) {
    CheckoutSession checkout = getActiveCheckout(checkoutId);
    checkout.setRequestedDeliveryDate(date);
    checkout.setDeliverySlotStart(slotStart);
    checkout.setDeliverySlotEnd(slotEnd);
    checkout.setDeliveryInstructions(instructions);
    return checkoutSessionRepository.save(checkout);
  }

  /**
   * Set billing address.
   */
  public CheckoutSession setBillingAddress(UUID checkoutId, Long addressId, boolean sameAsShipping) {
    CheckoutSession checkout = getActiveCheckout(checkoutId);
    checkout.setBillingAddressId(addressId);
    checkout.setBillingSameAsShipping(sameAsShipping);
    return checkoutSessionRepository.save(checkout);
  }

  /**
   * Set payment method.
   */
  public CheckoutSession setPaymentMethod(UUID checkoutId, Long methodId, String methodType) {
    CheckoutSession checkout = getActiveCheckout(checkoutId);
    checkout.setPaymentMethodId(methodId);
    checkout.setPaymentMethodType(methodType);
    checkout.setPaymentCompleted(true);
    checkout.setCurrentStep("REVIEW");
    checkout.setStatus(CheckoutSession.CheckoutStatus.PAYMENT_SELECTED);
    return checkoutSessionRepository.save(checkout);
  }

  /**
   * Apply coupon to checkout.
   */
  public CheckoutSession applyCoupon(UUID checkoutId, Long couponId, String couponCode, BigDecimal discountAmount) {
    CheckoutSession checkout = getActiveCheckout(checkoutId);
    checkout.setCouponId(couponId);
    checkout.setCouponCode(couponCode);
    checkout.setCouponDiscountAmount(discountAmount);
    checkout.setDiscountAmount(checkout.getDiscountAmount().add(discountAmount));
    checkout.calculateGrandTotal();
    return checkoutSessionRepository.save(checkout);
  }

  /**
   * Remove coupon from checkout.
   */
  public CheckoutSession removeCoupon(UUID checkoutId) {
    CheckoutSession checkout = getActiveCheckout(checkoutId);
    if (checkout.getCouponDiscountAmount() != null) {
      checkout.setDiscountAmount(checkout.getDiscountAmount().subtract(checkout.getCouponDiscountAmount()));
    }
    checkout.setCouponId(null);
    checkout.setCouponCode(null);
    checkout.setCouponDiscountAmount(null);
    checkout.calculateGrandTotal();
    return checkoutSessionRepository.save(checkout);
  }

  /**
   * Set customer notes for the order.
   */
  public CheckoutSession setCustomerNotes(UUID checkoutId, String notes) {
    CheckoutSession checkout = getActiveCheckout(checkoutId);
    checkout.setCustomerNotes(notes);
    return checkoutSessionRepository.save(checkout);
  }

  /**
   * Complete the checkout and create the order.
   */
  public CustomerOrder completeCheckout(UUID checkoutId) {
    CheckoutSession checkout = getActiveCheckout(checkoutId);

    // Validate checkout is ready
    if (!checkout.isReadyForOrder()) {
      throw new IllegalStateException("Checkout is not ready. Please complete all required steps.");
    }

    // Create the order
    CustomerOrder order = customerOrderService.createOrderFromCheckout(checkout);

    // Mark checkout as completed
    checkout.complete(order.getOrderId());
    checkoutSessionRepository.save(checkout);

    log.info("Completed checkout {} and created order {}", checkoutId, order.getOrderNumber());

    return order;
  }

  /**
   * Abandon checkout session.
   */
  public void abandonCheckout(UUID checkoutId) {
    CheckoutSession checkout = checkoutSessionRepository.findById(checkoutId)
        .orElseThrow(() -> new IllegalArgumentException("Checkout not found: " + checkoutId));

    checkout.abandon();
    checkoutSessionRepository.save(checkout);
    log.info("Abandoned checkout session: {}", checkoutId);
  }

  /**
   * Expire old checkout sessions.
   */
  public int expireOldCheckouts() {
    int expired = checkoutSessionRepository.abandonExpiredCheckouts(Instant.now());
    log.info("Expired {} checkout sessions", expired);
    return expired;
  }

  // Private helpers

  private CheckoutSession getActiveCheckout(UUID checkoutId) {
    CheckoutSession checkout = checkoutSessionRepository.findById(checkoutId)
        .orElseThrow(() -> new IllegalArgumentException("Checkout not found: " + checkoutId));

    if (checkout.isExpired()) {
      checkout.abandon();
      checkoutSessionRepository.save(checkout);
      throw new IllegalStateException("Checkout session has expired");
    }

    if (checkout.getStatus() == CheckoutSession.CheckoutStatus.COMPLETED) {
      throw new IllegalStateException("Checkout has already been completed");
    }

    if (checkout.getStatus() == CheckoutSession.CheckoutStatus.ABANDONED) {
      throw new IllegalStateException("Checkout has been abandoned");
    }

    return checkout;
  }
}
