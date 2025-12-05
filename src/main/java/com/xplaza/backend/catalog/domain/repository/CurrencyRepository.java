/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.catalog.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xplaza.backend.catalog.domain.entity.Currency;

@Repository
public interface CurrencyRepository extends JpaRepository<Currency, Long> {
  @org.springframework.data.jpa.repository.Query("SELECT c.currencyName FROM Currency c WHERE c.currencyId = :id")
  String getName(@org.springframework.data.repository.query.Param("id") Long id);
}
