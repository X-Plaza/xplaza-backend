/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.fulfillment.domain.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.xplaza.backend.fulfillment.domain.entity.Carrier;

/**
 * Repository for Carrier entity.
 */
@Repository
public interface CarrierRepository extends JpaRepository<Carrier, Long> {

  Optional<Carrier> findByCode(String code);

  List<Carrier> findByIsActiveTrue();

  @Query("SELECT c FROM Carrier c WHERE c.isActive = true AND c.supportedCountries LIKE %:countryCode%")
  List<Carrier> findActiveCarriersByCountry(@Param("countryCode") String countryCode);

  @Query("SELECT c FROM Carrier c WHERE c.isActive = true AND c.supportsInternational = true")
  List<Carrier> findInternationalCarriers();

  @Query("SELECT c FROM Carrier c WHERE c.isActive = true AND c.supportsSameDay = true")
  List<Carrier> findSameDayCarriers();

  @Query("SELECT c FROM Carrier c WHERE c.isActive = true ORDER BY c.priority DESC")
  List<Carrier> findByPriority();

  boolean existsByCode(String code);
}
