/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.jpa.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.xplaza.backend.jpa.dao.DiscountTypeDao;

public interface DiscountTypeRepository extends JpaRepository<DiscountTypeDao, Long> {
}
