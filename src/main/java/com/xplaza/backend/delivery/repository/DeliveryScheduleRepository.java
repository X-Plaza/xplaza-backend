/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.delivery.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xplaza.backend.delivery.domain.entity.DeliverySchedule;

@Repository
public interface DeliveryScheduleRepository extends JpaRepository<DeliverySchedule, Long> {
  List<DeliverySchedule> findByShopShopId(Long shopId);

  List<DeliverySchedule> findByDayDayId(Long dayId);

  List<DeliverySchedule> findByIsAvailableTrue();
}
