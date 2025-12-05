/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.order.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.xplaza.backend.exception.ResourceNotFoundException;
import com.xplaza.backend.order.domain.entity.OrderStatus;
import com.xplaza.backend.order.domain.repository.OrderStatusRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("OrderStatusService Unit Tests")
class OrderStatusServiceTest {

  @Mock
  private OrderStatusRepository orderStatusRepository;

  @InjectMocks
  private OrderStatusService orderStatusService;

  private OrderStatus testOrderStatus;

  @BeforeEach
  void setUp() {
    testOrderStatus = new OrderStatus();
    testOrderStatus.setOrderStatusId(1L);
    testOrderStatus.setStatusName("Pending");
    testOrderStatus.setDescription("Order is pending");
    testOrderStatus.setColor("#FFA500");
    testOrderStatus.setSortOrder(1);
    testOrderStatus.setIsActive(true);
  }

  @Nested
  @DisplayName("listOrderStatuses Tests")
  class ListOrderStatusesTests {

    @Test
    @DisplayName("Should return list of active order statuses")
    void shouldReturnActiveOrderStatuses() {
      when(orderStatusRepository.findByIsActiveTrueOrderBySortOrder()).thenReturn(Arrays.asList(testOrderStatus));

      List<OrderStatus> result = orderStatusService.listOrderStatuses();

      assertNotNull(result);
      assertEquals(1, result.size());
      verify(orderStatusRepository, times(1)).findByIsActiveTrueOrderBySortOrder();
    }

    @Test
    @DisplayName("Should return list of all order statuses")
    void shouldReturnAllOrderStatuses() {
      when(orderStatusRepository.findAll()).thenReturn(Arrays.asList(testOrderStatus));

      List<OrderStatus> result = orderStatusService.listAllOrderStatuses();

      assertNotNull(result);
      assertEquals(1, result.size());
      verify(orderStatusRepository, times(1)).findAll();
    }
  }

  @Nested
  @DisplayName("getOrderStatus Tests")
  class GetOrderStatusTests {

    @Test
    @DisplayName("Should return order status by id")
    void shouldReturnOrderStatusById() {
      when(orderStatusRepository.findById(1L)).thenReturn(Optional.of(testOrderStatus));

      OrderStatus result = orderStatusService.getOrderStatus(1L);

      assertNotNull(result);
      assertEquals(1L, result.getOrderStatusId());
      assertEquals("Pending", result.getStatusName());
    }

    @Test
    @DisplayName("Should return order status by name")
    void shouldReturnOrderStatusByName() {
      when(orderStatusRepository.findByStatusName("Pending")).thenReturn(Optional.of(testOrderStatus));

      OrderStatus result = orderStatusService.getOrderStatusByName("Pending");

      assertNotNull(result);
      assertEquals("Pending", result.getStatusName());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when order status not found")
    void shouldThrowExceptionWhenOrderStatusNotFound() {
      when(orderStatusRepository.findById(999L)).thenReturn(Optional.empty());

      assertThrows(ResourceNotFoundException.class, () -> orderStatusService.getOrderStatus(999L));
    }
  }

  @Nested
  @DisplayName("createOrderStatus Tests")
  class CreateOrderStatusTests {

    @Test
    @DisplayName("Should successfully create a new order status")
    void shouldCreateOrderStatus() {
      when(orderStatusRepository.save(testOrderStatus)).thenReturn(testOrderStatus);

      OrderStatus result = orderStatusService.createOrderStatus(testOrderStatus);

      assertNotNull(result);
      assertEquals("Pending", result.getStatusName());
      verify(orderStatusRepository, times(1)).save(testOrderStatus);
    }
  }

  @Nested
  @DisplayName("updateOrderStatus Tests")
  class UpdateOrderStatusTests {

    @Test
    @DisplayName("Should successfully update an existing order status")
    void shouldUpdateOrderStatus() {
      OrderStatus updatedDetails = new OrderStatus();
      updatedDetails.setStatusName("Processing");
      updatedDetails.setDescription("Order is being processed");

      when(orderStatusRepository.findById(1L)).thenReturn(Optional.of(testOrderStatus));
      when(orderStatusRepository.save(any(OrderStatus.class))).thenReturn(testOrderStatus);

      OrderStatus result = orderStatusService.updateOrderStatus(1L, updatedDetails);

      assertNotNull(result);
      verify(orderStatusRepository, times(1)).findById(1L);
      verify(orderStatusRepository, times(1)).save(any(OrderStatus.class));
    }
  }

  @Nested
  @DisplayName("deleteOrderStatus Tests")
  class DeleteOrderStatusTests {

    @Test
    @DisplayName("Should delete order status by id")
    void shouldDeleteOrderStatus() {
      when(orderStatusRepository.existsById(1L)).thenReturn(true);
      doNothing().when(orderStatusRepository).deleteById(1L);

      orderStatusService.deleteOrderStatus(1L);

      verify(orderStatusRepository, times(1)).existsById(1L);
      verify(orderStatusRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when order status does not exist")
    void shouldThrowExceptionWhenOrderStatusDoesNotExist() {
      when(orderStatusRepository.existsById(999L)).thenReturn(false);

      assertThrows(ResourceNotFoundException.class, () -> orderStatusService.deleteOrderStatus(999L));
    }
  }
}
