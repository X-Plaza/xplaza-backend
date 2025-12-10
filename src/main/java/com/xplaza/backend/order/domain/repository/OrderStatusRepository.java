/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.order.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xplaza.backend.order.domain.entity.OrderStatus;

@Repository
public interface OrderStatusRepository extends JpaRepository<OrderStatus, Long> {
  Optional<OrderStatus> findByStatusName(String statusName);

  List<OrderStatus> findByIsActiveTrueOrderBySortOrder();
}
