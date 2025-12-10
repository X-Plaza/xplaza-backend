/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.customer.domain.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xplaza.backend.customer.domain.entity.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long> {
  Optional<Customer> findByEmail(String email);

  boolean existsByEmail(String email);
}
