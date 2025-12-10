/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.catalog.domain.repository;

import java.util.UUID;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.xplaza.backend.catalog.domain.entity.VariantImage;

@Repository
public interface VariantImageRepository extends JpaRepository<VariantImage, UUID> {
  long countByVariantVariantId(UUID variantId);
}
