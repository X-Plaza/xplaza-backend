/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.marketing.domain.repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.xplaza.backend.marketing.domain.entity.Campaign;

/**
 * Repository for Campaign entity.
 */
@Repository
public interface CampaignRepository extends JpaRepository<Campaign, Long> {

  Optional<Campaign> findByCode(String code);

  @Query("SELECT c FROM Campaign c WHERE c.status = 'ACTIVE' AND c.startDate <= :now AND c.endDate >= :now")
  List<Campaign> findActiveCampaigns(@Param("now") Instant now);

  @Query("SELECT c FROM Campaign c WHERE c.status = 'ACTIVE' AND c.startDate <= :now AND c.endDate >= :now AND c.displayOnHomepage = true ORDER BY c.displayPriority DESC")
  List<Campaign> findHomepageCampaigns(@Param("now") Instant now);

  Page<Campaign> findByStatus(Campaign.CampaignStatus status, Pageable pageable);

  @Query("SELECT c FROM Campaign c WHERE c.status = 'SCHEDULED' AND c.startDate <= :now")
  List<Campaign> findCampaignsToActivate(@Param("now") Instant now);

  @Query("SELECT c FROM Campaign c WHERE c.status = 'ACTIVE' AND c.endDate < :now")
  List<Campaign> findCampaignsToEnd(@Param("now") Instant now);

  @Query("SELECT c FROM Campaign c WHERE c.status = 'ACTIVE' AND c.type = :type ORDER BY c.displayPriority DESC")
  List<Campaign> findActiveCampaignsByType(@Param("type") Campaign.CampaignType type);

  @Query("SELECT c FROM Campaign c JOIN c.products cp WHERE cp.productId = :productId AND c.status = 'ACTIVE' AND c.startDate <= :now AND c.endDate >= :now")
  List<Campaign> findActiveCampaignsForProduct(@Param("productId") Long productId, @Param("now") Instant now);

  boolean existsByCode(String code);
}
