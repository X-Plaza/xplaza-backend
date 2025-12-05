/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.geography.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xplaza.backend.geography.domain.entity.State;

@Repository
public interface StateRepository extends JpaRepository<State, Long> {
  List<State> findByCountryCountryId(Long countryId);

  List<State> findByStateNameContainingIgnoreCase(String name);
}
