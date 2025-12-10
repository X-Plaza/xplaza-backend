/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.customer.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.xplaza.backend.customer.domain.entity.CustomerAddress;

/**
 * Repository for CustomerAddress entity.
 */
@Repository
public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {

  List<CustomerAddress> findByCustomerId(Long customerId);

  List<CustomerAddress> findByCustomerIdAndIsActiveTrue(Long customerId);

  Optional<CustomerAddress> findByCustomerIdAndIsDefaultTrue(Long customerId);

  @Query("SELECT ca FROM CustomerAddress ca WHERE ca.customerId = :customerId AND ca.type IN ('SHIPPING', 'BOTH') AND ca.isActive = true")
  List<CustomerAddress> findShippingAddresses(@Param("customerId") Long customerId);

  @Query("SELECT ca FROM CustomerAddress ca WHERE ca.customerId = :customerId AND ca.type IN ('BILLING', 'BOTH') AND ca.isActive = true")
  List<CustomerAddress> findBillingAddresses(@Param("customerId") Long customerId);

  @Query("SELECT COUNT(ca) FROM CustomerAddress ca WHERE ca.customerId = :customerId AND ca.isActive = true")
  long countByCustomerId(@Param("customerId") Long customerId);

  @Query("SELECT ca FROM CustomerAddress ca WHERE ca.customerId = :customerId AND ca.label = :label AND ca.isActive = true")
  Optional<CustomerAddress> findByCustomerIdAndLabel(@Param("customerId") Long customerId,
      @Param("label") String label);
}
