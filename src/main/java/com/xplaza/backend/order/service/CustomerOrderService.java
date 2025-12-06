/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.order.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.cart.domain.entity.Cart;
import com.xplaza.backend.cart.domain.entity.CartItem;
import com.xplaza.backend.cart.domain.repository.CartRepository;
import com.xplaza.backend.inventory.service.InventoryService;
import com.xplaza.backend.notification.domain.entity.Notification;
import com.xplaza.backend.notification.service.NotificationService;
import com.xplaza.backend.order.domain.entity.CheckoutSession;
import com.xplaza.backend.order.domain.entity.CustomerOrder;
import com.xplaza.backend.order.domain.entity.CustomerOrderItem;
import com.xplaza.backend.order.domain.repository.CustomerOrderItemRepository;
import com.xplaza.backend.order.domain.repository.CustomerOrderRepository;
import com.xplaza.backend.payment.domain.entity.Refund;
import com.xplaza.backend.payment.service.PaymentService;

/**
 * Service for customer order operations with UUID-based orders.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CustomerOrderService {

  private final CustomerOrderRepository orderRepository;
  private final CustomerOrderItemRepository orderItemRepository;
  private final CartRepository cartRepository;
  private final PaymentService paymentService;
  private final NotificationService notificationService;
  private final InventoryService inventoryService;

  private static final DateTimeFormatter ORDER_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd");
  private static final Random RANDOM = new Random();

  /**
   * Create an order from a checkout session.
   */
  public CustomerOrder createOrderFromCheckout(CheckoutSession checkout) {
    // Get the cart
    Cart cart = cartRepository.findByIdWithItems(checkout.getCartId())
        .orElseThrow(() -> new IllegalArgumentException("Cart not found: " + checkout.getCartId()));

    if (cart.isEmpty()) {
      throw new IllegalStateException("Cannot create order from empty cart");
    }

    // Generate order number
    String orderNumber = generateOrderNumber();

    // Determine shop ID from first item (multi-shop orders need different handling)
    Long shopId = cart.getActiveItems().get(0).getShopId();

    // Create the order
    CustomerOrder order = CustomerOrder.builder()
        .orderNumber(orderNumber)
        .customerId(checkout.getCustomerId())
        .shopId(shopId)
        .cartId(checkout.getCartId())
        .status(CustomerOrder.OrderStatus.PENDING)
        .subtotal(checkout.getSubtotal())
        .discountAmount(checkout.getDiscountAmount())
        .shippingCost(checkout.getShippingCost())
        .taxAmount(checkout.getTaxAmount())
        .grandTotal(checkout.getGrandTotal())
        .currency(checkout.getCurrency())
        .shippingAddressId(checkout.getShippingAddressId())
        .billingAddressId(checkout.getBillingAddressId())
        .billingSameAsShipping(checkout.getBillingSameAsShipping())
        .requestedDeliveryDate(checkout.getRequestedDeliveryDate())
        .deliverySlotStart(checkout.getDeliverySlotStart())
        .deliverySlotEnd(checkout.getDeliverySlotEnd())
        .paymentTypeId(checkout.getPaymentMethodId())
        .paymentMethod(checkout.getPaymentMethodType())
        .couponId(checkout.getCouponId())
        .couponCode(checkout.getCouponCode())
        .couponDiscountAmount(checkout.getCouponDiscountAmount())
        .customerNotes(checkout.getCustomerNotes())
        .shippingInstructions(checkout.getDeliveryInstructions())
        .placedAt(Instant.now())
        .build();

    // Copy cart items to order items
    for (CartItem cartItem : cart.getActiveItems()) {
      // Reserve stock
      inventoryService.reserveStockAnyWarehouse(cartItem.getProductId(), cartItem.getVariantId(),
          cartItem.getQuantity(), order.getOrderId());

      CustomerOrderItem orderItem = CustomerOrderItem.builder()
          .order(order)
          .productId(cartItem.getProductId())
          .variantId(cartItem.getVariantId())
          .shopId(cartItem.getShopId())
          .productName(cartItem.getProductName() != null ? cartItem.getProductName()
              : "Product " + cartItem.getProductId())
          .quantity(cartItem.getQuantity())
          .unitPrice(cartItem.getUnitPrice())
          .discountAmount(cartItem.getDiscountAmount())
          .totalPrice(cartItem.getLineTotal())
          .build();
      order.addItem(orderItem);
    }

    // Record initial status
    order.changeStatus(CustomerOrder.OrderStatus.PENDING, "Order created", "system");

    CustomerOrder savedOrder = orderRepository.save(order);

    // Mark cart as converted
    cart.markConverted();
    cartRepository.save(cart);

    log.info("Created order {} from cart {}", savedOrder.getOrderNumber(), cart.getId());

    // Send notification
    try {
      notificationService.createOrderNotification(
          savedOrder.getCustomerId(),
          Notification.NotificationType.ORDER_PLACED,
          "Order Placed",
          "Your order " + savedOrder.getOrderNumber() + " has been placed successfully.",
          savedOrder.getOrderId().toString());
    } catch (Exception e) {
      log.error("Failed to send order notification: {}", e.getMessage());
    }

    return savedOrder;
  }

  /**
   * Get order by ID.
   */
  @Transactional(readOnly = true)
  public Optional<CustomerOrder> getOrder(UUID orderId) {
    return orderRepository.findById(orderId);
  }

  /**
   * Get order by ID with items.
   */
  @Transactional(readOnly = true)
  public Optional<CustomerOrder> getOrderWithItems(UUID orderId) {
    return orderRepository.findByIdWithItems(orderId);
  }

  /**
   * Get order by ID with full details.
   */
  @Transactional(readOnly = true)
  public Optional<CustomerOrder> getOrderWithDetails(UUID orderId) {
    return orderRepository.findByIdWithDetails(orderId);
  }

  /**
   * Get order by order number.
   */
  @Transactional(readOnly = true)
  public Optional<CustomerOrder> getOrderByNumber(String orderNumber) {
    return orderRepository.findByOrderNumber(orderNumber);
  }

  /**
   * Get orders for a customer.
   */
  @Transactional(readOnly = true)
  public List<CustomerOrder> getCustomerOrders(Long customerId) {
    return orderRepository.findByCustomerIdOrderByCreatedAtDesc(customerId);
  }

  /**
   * Get orders for a customer (paginated).
   */
  @Transactional(readOnly = true)
  public Page<CustomerOrder> getCustomerOrders(Long customerId, Pageable pageable) {
    return orderRepository.findByCustomerId(customerId, pageable);
  }

  /**
   * Get orders for a shop.
   */
  @Transactional(readOnly = true)
  public List<CustomerOrder> getShopOrders(Long shopId) {
    return orderRepository.findByShopIdOrderByCreatedAtDesc(shopId);
  }

  /**
   * Get orders for a shop (paginated).
   */
  @Transactional(readOnly = true)
  public Page<CustomerOrder> getShopOrders(Long shopId, Pageable pageable) {
    return orderRepository.findByShopId(shopId, pageable);
  }

  /**
   * Get orders by status.
   */
  @Transactional(readOnly = true)
  public List<CustomerOrder> getOrdersByStatus(CustomerOrder.OrderStatus status) {
    return orderRepository.findByStatus(status);
  }

  @Transactional(readOnly = true)
  public long countOrdersByCouponCode(String couponCode) {
    return orderRepository.countByCouponCode(couponCode);
  }

  /**
   * Get shop orders by status.
   */
  @Transactional(readOnly = true)
  public List<CustomerOrder> getShopOrdersByStatus(Long shopId, CustomerOrder.OrderStatus status) {
    return orderRepository.findByShopIdAndStatus(shopId, status);
  }

  /**
   * Confirm an order (payment received).
   */
  public CustomerOrder confirmOrder(UUID orderId, UUID paymentTransactionId) {
    CustomerOrder order = getOrderOrThrow(orderId);

    if (order.getStatus() != CustomerOrder.OrderStatus.PENDING) {
      throw new IllegalStateException("Order cannot be confirmed. Current status: " + order.getStatus());
    }

    order.setPaymentTransactionId(paymentTransactionId);
    order.setPaymentStatus("PAID");
    order.changeStatus(CustomerOrder.OrderStatus.CONFIRMED, "Payment confirmed", "system");

    CustomerOrder saved = orderRepository.save(order);
    log.info("Confirmed order: {}", order.getOrderNumber());

    return saved;
  }

  /**
   * Start processing an order.
   */
  public CustomerOrder startProcessing(UUID orderId, String processedBy) {
    CustomerOrder order = getOrderOrThrow(orderId);

    if (order.getStatus() != CustomerOrder.OrderStatus.CONFIRMED) {
      throw new IllegalStateException("Order cannot be processed. Current status: " + order.getStatus());
    }

    order.changeStatus(CustomerOrder.OrderStatus.PROCESSING, "Started processing", processedBy);
    return orderRepository.save(order);
  }

  /**
   * Mark order as shipped.
   */
  public CustomerOrder markShipped(UUID orderId, String carrier, String trackingNumber, String shippedBy) {
    CustomerOrder order = getOrderOrThrow(orderId);

    if (order.getStatus() != CustomerOrder.OrderStatus.PROCESSING) {
      throw new IllegalStateException("Order cannot be shipped. Current status: " + order.getStatus());
    }

    order.changeStatus(CustomerOrder.OrderStatus.SHIPPED,
        "Shipped via " + carrier + ", tracking: " + trackingNumber, shippedBy);

    // Update estimated delivery (add 3-5 business days)
    order.setEstimatedDeliveryDate(LocalDate.now().plusDays(5));

    return orderRepository.save(order);
  }

  /**
   * Mark order as out for delivery.
   */
  public CustomerOrder markOutForDelivery(UUID orderId, String updatedBy) {
    CustomerOrder order = getOrderOrThrow(orderId);

    if (order.getStatus() != CustomerOrder.OrderStatus.SHIPPED) {
      throw new IllegalStateException("Order cannot be marked out for delivery. Current status: " + order.getStatus());
    }

    order.changeStatus(CustomerOrder.OrderStatus.OUT_FOR_DELIVERY, "Out for delivery", updatedBy);
    order.setEstimatedDeliveryDate(LocalDate.now());

    return orderRepository.save(order);
  }

  /**
   * Mark order as delivered.
   */
  public CustomerOrder markDelivered(UUID orderId, String deliveredBy) {
    CustomerOrder order = getOrderOrThrow(orderId);

    if (order.getStatus() != CustomerOrder.OrderStatus.OUT_FOR_DELIVERY
        && order.getStatus() != CustomerOrder.OrderStatus.SHIPPED) {
      throw new IllegalStateException("Order cannot be marked delivered. Current status: " + order.getStatus());
    }

    order.changeStatus(CustomerOrder.OrderStatus.DELIVERED, "Delivered", deliveredBy);
    order.setActualDeliveryDate(LocalDate.now());

    // Update all items to delivered
    orderItemRepository.updateStatusForOrder(orderId, CustomerOrderItem.ItemStatus.DELIVERED);

    return orderRepository.save(order);
  }

  /**
   * Cancel an order.
   */
  public CustomerOrder cancelOrder(UUID orderId, String reason, String cancelledBy) {
    CustomerOrder order = getOrderOrThrow(orderId);

    if (!order.canBeCancelled()) {
      throw new IllegalStateException("Order cannot be cancelled. Current status: " + order.getStatus());
    }

    order.changeStatus(CustomerOrder.OrderStatus.CANCELLED, reason, cancelledBy);
    order.setCancellationReason(reason);

    // Update all items to cancelled
    orderItemRepository.updateStatusForOrder(orderId, CustomerOrderItem.ItemStatus.CANCELLED);

    CustomerOrder saved = orderRepository.save(order);
    log.info("Cancelled order: {} - Reason: {}", order.getOrderNumber(), reason);

    // Trigger refund if payment was made
    if ("PAID".equals(order.getPaymentStatus())) {
      paymentService.createRefundRequest(
          orderId,
          order.getGrandTotal(),
          order.getCurrency(),
          Refund.RefundReason.OTHER,
          "Order cancelled: " + reason,
          -1L, // System/Admin
          Refund.RequesterType.ADMIN);
      log.info("Initiated refund for cancelled order: {}", order.getOrderNumber());
    }

    return saved;
  }

  /**
   * Request return for an order.
   */
  public CustomerOrder requestReturn(UUID orderId, String reason, String requestedBy) {
    CustomerOrder order = getOrderOrThrow(orderId);

    if (order.getStatus() != CustomerOrder.OrderStatus.DELIVERED) {
      throw new IllegalStateException("Return can only be requested for delivered orders");
    }

    // Check if within return window (e.g., 30 days)
    if (order.getDeliveredAt() != null) {
      Instant returnDeadline = order.getDeliveredAt().plusSeconds(30 * 24 * 60 * 60);
      if (Instant.now().isAfter(returnDeadline)) {
        throw new IllegalStateException("Return window has expired");
      }
    }

    order.changeStatus(CustomerOrder.OrderStatus.RETURN_REQUESTED, reason, requestedBy);
    return orderRepository.save(order);
  }

  /**
   * Add internal note to order.
   */
  public CustomerOrder addInternalNote(UUID orderId, String note) {
    CustomerOrder order = getOrderOrThrow(orderId);
    String existingNotes = order.getInternalNotes();
    String timestamp = Instant.now().toString();
    String newNote = "[" + timestamp + "] " + note;

    if (existingNotes != null && !existingNotes.isEmpty()) {
      order.setInternalNotes(existingNotes + "\n" + newNote);
    } else {
      order.setInternalNotes(newNote);
    }

    return orderRepository.save(order);
  }

  /**
   * Calculate customer lifetime value.
   */
  @Transactional(readOnly = true)
  public BigDecimal getCustomerLifetimeValue(Long customerId) {
    BigDecimal total = orderRepository.calculateCustomerLifetimeValue(customerId);
    return total != null ? total : BigDecimal.ZERO;
  }

  /**
   * Get shop revenue for a date range.
   */
  @Transactional(readOnly = true)
  public BigDecimal getShopRevenue(Long shopId, Instant start, Instant end) {
    BigDecimal total = orderRepository.calculateShopRevenue(shopId, start, end);
    return total != null ? total : BigDecimal.ZERO;
  }

  /**
   * Count customer orders.
   */
  @Transactional(readOnly = true)
  public long countCustomerOrders(Long customerId) {
    return orderRepository.countByCustomerId(customerId);
  }

  // Private helpers

  private CustomerOrder getOrderOrThrow(UUID orderId) {
    return orderRepository.findById(orderId)
        .orElseThrow(() -> new IllegalArgumentException("Order not found: " + orderId));
  }

  private String generateOrderNumber() {
    String date = LocalDate.now().format(ORDER_DATE_FORMAT);
    Long sequence = orderRepository.getNextOrderSequence();
    // Format sequence as 6-digit number with leading zeros
    String seqStr = String.format("%06d", sequence);
    return "ORD-" + date + "-" + seqStr;
  }
}
