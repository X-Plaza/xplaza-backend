/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.delivery.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xplaza.backend.delivery.domain.entity.DeliveryCost;

@Repository
public interface DeliveryCostRepository extends JpaRepository<DeliveryCost, Long> {
  List<DeliveryCost> findByShopShopId(Long shopId);

  List<DeliveryCost> findByCityCityId(Long cityId);

  List<DeliveryCost> findByIsActiveTrue();
}
