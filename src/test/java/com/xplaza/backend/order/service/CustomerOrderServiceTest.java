/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.order.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.math.BigDecimal;
import java.util.ArrayList;
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
import com.xplaza.backend.cart.domain.repository.CartRepository;
import com.xplaza.backend.order.domain.entity.CheckoutSession;
import com.xplaza.backend.order.domain.entity.CustomerOrder;
import com.xplaza.backend.order.domain.entity.CustomerOrder.OrderStatus;
import com.xplaza.backend.order.domain.repository.CustomerOrderItemRepository;
import com.xplaza.backend.order.domain.repository.CustomerOrderRepository;

@ExtendWith(MockitoExtension.class)
class CustomerOrderServiceTest {

  @Mock
  private CustomerOrderRepository orderRepository;

  @Mock
  private CustomerOrderItemRepository orderItemRepository;

  @Mock
  private CartRepository cartRepository;

  @Mock
  private com.xplaza.backend.payment.service.PaymentService paymentService;

  @Mock
  private com.xplaza.backend.notification.service.NotificationService notificationService;

  @Mock
  private com.xplaza.backend.inventory.service.InventoryService inventoryService;

  @InjectMocks
  private CustomerOrderService orderService;

  private Cart cart;
  private CheckoutSession checkoutSession;
  private UUID cartId = UUID.randomUUID();
  private Long customerId = 101L;

  @BeforeEach
  void setUp() {
    cart = Cart.builder()
        .id(cartId)
        .customerId(customerId)
        .items(new ArrayList<>())
        .build();

    // Add a cart item
    CartItem item = CartItem.builder()
        .id(UUID.randomUUID())
        .cart(cart)
        .productId(500L)
        .shopId(99L)
        .quantity(2)
        .unitPrice(BigDecimal.valueOf(50.00))
        .status(CartItem.ItemStatus.ACTIVE)
        .build();
    cart.addCartItem(item);

    checkoutSession = CheckoutSession.builder()
        .checkoutId(UUID.randomUUID())
        .cartId(cartId)
        .customerId(customerId)
        .grandTotal(BigDecimal.valueOf(100.00))
        .paymentMethodType("CARD")
        .build();
  }

  @Test
  void createOrderFromCheckout_ShouldCreateOrder_WhenCartIsValid() {
    given(cartRepository.findByIdWithItems(cartId)).willReturn(Optional.of(cart));
    given(orderRepository.getNextOrderSequence()).willReturn(12345L);
    given(orderRepository.save(any(CustomerOrder.class))).willAnswer(inv -> inv.getArgument(0));

    CustomerOrder result = orderService.createOrderFromCheckout(checkoutSession);

    assertThat(result).isNotNull();
    assertThat(result.getCustomerId()).isEqualTo(customerId);
    assertThat(result.getGrandTotal()).isEqualTo(checkoutSession.getGrandTotal());
    assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
    assertThat(result.getItems()).hasSize(1);
    assertThat(result.getOrderNumber()).contains("12345");

    verify(cartRepository).save(cart); // Should mark cart converted
    assertThat(cart.getStatus()).isEqualTo(Cart.CartStatus.CONVERTED);
  }

  @Test
  void createOrderFromCheckout_ShouldThrowException_WhenCartIsEmpty() {
    // Create a fresh empty cart for this test
    Cart emptyCart = Cart.builder()
        .id(cartId)
        .customerId(customerId)
        .items(new ArrayList<>())
        .build();

    given(cartRepository.findByIdWithItems(cartId)).willReturn(Optional.of(emptyCart));

    assertThatThrownBy(() -> orderService.createOrderFromCheckout(checkoutSession))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("empty cart");
  }

  @Test
  void confirmOrder_ShouldUpdateStatus_WhenOrderIsPending() {
    UUID orderId = UUID.randomUUID();
    CustomerOrder order = CustomerOrder.builder()
        .orderId(orderId)
        .status(OrderStatus.PENDING)
        .orderNumber("ORD-001")
        .build();
    UUID txnId = UUID.randomUUID();

    given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
    given(orderRepository.save(any(CustomerOrder.class))).willAnswer(inv -> inv.getArgument(0));

    CustomerOrder result = orderService.confirmOrder(orderId, txnId);

    assertThat(result.getStatus()).isEqualTo(OrderStatus.CONFIRMED);
    assertThat(result.getPaymentTransactionId()).isEqualTo(txnId);
    assertThat(result.getStatusHistory()).hasSize(1);
  }

  @Test
  void confirmOrder_ShouldThrowException_WhenOrderIsNotPending() {
    UUID orderId = UUID.randomUUID();
    CustomerOrder order = CustomerOrder.builder()
        .orderId(orderId)
        .status(OrderStatus.SHIPPED)
        .build();

    given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

    assertThatThrownBy(() -> orderService.confirmOrder(orderId, UUID.randomUUID()))
        .isInstanceOf(IllegalStateException.class)
        .hasMessageContaining("cannot be confirmed");
  }

  @Test
  void markShipped_ShouldUpdateStatusAndDeliveryDate() {
    UUID orderId = UUID.randomUUID();
    CustomerOrder order = CustomerOrder.builder()
        .orderId(orderId)
        .status(OrderStatus.PROCESSING)
        .build();

    given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
    given(orderRepository.save(any(CustomerOrder.class))).willAnswer(inv -> inv.getArgument(0));

    CustomerOrder result = orderService.markShipped(orderId, "FedEx", "TRK123", "Admin");

    assertThat(result.getStatus()).isEqualTo(OrderStatus.SHIPPED);
    assertThat(result.getEstimatedDeliveryDate()).isNotNull();
    assertThat(result.getStatusHistory()).isNotEmpty();
  }
}
