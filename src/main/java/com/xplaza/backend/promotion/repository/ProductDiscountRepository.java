/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.promotion.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xplaza.backend.promotion.domain.entity.ProductDiscount;

@Repository
public interface ProductDiscountRepository extends JpaRepository<ProductDiscount, Long> {
  List<ProductDiscount> findByProductProductId(Long productId);

  List<ProductDiscount> findByIsActiveTrue();
}
