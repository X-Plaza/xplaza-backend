/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.order.service;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.exception.ResourceNotFoundException;
import com.xplaza.backend.order.domain.entity.OrderStatus;
import com.xplaza.backend.order.domain.repository.OrderStatusRepository;

@Service
@RequiredArgsConstructor
public class OrderStatusService {
  private final OrderStatusRepository orderStatusRepository;

  public List<OrderStatus> listOrderStatuses() {
    return orderStatusRepository.findByIsActiveTrueOrderBySortOrder();
  }

  public List<OrderStatus> listAllOrderStatuses() {
    return orderStatusRepository.findAll();
  }

  public OrderStatus getOrderStatus(Long id) {
    return orderStatusRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("OrderStatus not found: " + id));
  }

  public OrderStatus getOrderStatusByName(String name) {
    return orderStatusRepository.findByStatusName(name)
        .orElseThrow(() -> new ResourceNotFoundException("OrderStatus not found: " + name));
  }

  @Transactional
  public OrderStatus createOrderStatus(OrderStatus status) {
    return orderStatusRepository.save(status);
  }

  @Transactional
  public OrderStatus updateOrderStatus(Long id, OrderStatus details) {
    OrderStatus status = getOrderStatus(id);
    status.setStatusName(details.getStatusName());
    status.setDescription(details.getDescription());
    status.setColor(details.getColor());
    status.setSortOrder(details.getSortOrder());
    status.setIsActive(details.getIsActive());
    return orderStatusRepository.save(status);
  }

  @Transactional
  public void deleteOrderStatus(Long id) {
    if (!orderStatusRepository.existsById(id)) {
      throw new ResourceNotFoundException("OrderStatus not found: " + id);
    }
    orderStatusRepository.deleteById(id);
  }
}
