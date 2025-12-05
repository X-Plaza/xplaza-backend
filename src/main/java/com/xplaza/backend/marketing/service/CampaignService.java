/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.marketing.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.marketing.domain.entity.Campaign;
import com.xplaza.backend.marketing.domain.repository.CampaignRepository;

/**
 * Service for marketing campaign operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class CampaignService {

  private final CampaignRepository campaignRepository;

  /**
   * Create a new campaign.
   */
  public Campaign createCampaign(Campaign campaign) {
    if (campaignRepository.existsByCode(campaign.getCode())) {
      throw new IllegalArgumentException("Campaign code already exists: " + campaign.getCode());
    }
    campaign.setStatus(Campaign.CampaignStatus.DRAFT);
    campaign = campaignRepository.save(campaign);
    log.info("Created campaign: {}", campaign.getCode());
    return campaign;
  }

  /**
   * Update an existing campaign.
   */
  public Campaign updateCampaign(Long campaignId, Campaign updates) {
    Campaign existing = getCampaignOrThrow(campaignId);

    // Only update allowed fields
    if (updates.getName() != null)
      existing.setName(updates.getName());
    if (updates.getDescription() != null)
      existing.setDescription(updates.getDescription());
    if (updates.getDiscountType() != null)
      existing.setDiscountType(updates.getDiscountType());
    if (updates.getDiscountValue() != null)
      existing.setDiscountValue(updates.getDiscountValue());
    if (updates.getMaxDiscount() != null)
      existing.setMaxDiscount(updates.getMaxDiscount());
    if (updates.getMinPurchase() != null)
      existing.setMinPurchase(updates.getMinPurchase());
    if (updates.getTotalUsesLimit() != null)
      existing.setTotalUsesLimit(updates.getTotalUsesLimit());
    if (updates.getPerCustomerLimit() != null)
      existing.setPerCustomerLimit(updates.getPerCustomerLimit());
    if (updates.getStartDate() != null)
      existing.setStartDate(updates.getStartDate());
    if (updates.getEndDate() != null)
      existing.setEndDate(updates.getEndDate());
    if (updates.getBannerImageUrl() != null)
      existing.setBannerImageUrl(updates.getBannerImageUrl());
    if (updates.getDisplayOnHomepage() != null)
      existing.setDisplayOnHomepage(updates.getDisplayOnHomepage());
    if (updates.getDisplayPriority() != null)
      existing.setDisplayPriority(updates.getDisplayPriority());

    return campaignRepository.save(existing);
  }

  /**
   * Get campaign by ID.
   */
  @Transactional(readOnly = true)
  public Optional<Campaign> getCampaign(Long campaignId) {
    return campaignRepository.findById(campaignId);
  }

  /**
   * Get campaign by code.
   */
  @Transactional(readOnly = true)
  public Optional<Campaign> getCampaignByCode(String code) {
    return campaignRepository.findByCode(code);
  }

  /**
   * Get active campaigns.
   */
  @Transactional(readOnly = true)
  public List<Campaign> getActiveCampaigns() {
    return campaignRepository.findActiveCampaigns(Instant.now());
  }

  /**
   * Get homepage campaigns.
   */
  @Transactional(readOnly = true)
  public List<Campaign> getHomepageCampaigns() {
    return campaignRepository.findHomepageCampaigns(Instant.now());
  }

  /**
   * Get campaigns by status (paginated).
   */
  @Transactional(readOnly = true)
  public Page<Campaign> getCampaignsByStatus(Campaign.CampaignStatus status, Pageable pageable) {
    return campaignRepository.findByStatus(status, pageable);
  }

  /**
   * Get active campaigns for a product.
   */
  @Transactional(readOnly = true)
  public List<Campaign> getActiveCampaignsForProduct(Long productId) {
    return campaignRepository.findActiveCampaignsForProduct(productId, Instant.now());
  }

  /**
   * Activate a campaign.
   */
  public Campaign activateCampaign(Long campaignId) {
    Campaign campaign = getCampaignOrThrow(campaignId);

    if (campaign.getStatus() != Campaign.CampaignStatus.DRAFT &&
        campaign.getStatus() != Campaign.CampaignStatus.SCHEDULED &&
        campaign.getStatus() != Campaign.CampaignStatus.PAUSED) {
      throw new IllegalStateException("Campaign cannot be activated. Current status: " + campaign.getStatus());
    }

    campaign.activate();
    campaign = campaignRepository.save(campaign);
    log.info("Activated campaign: {}", campaign.getCode());
    return campaign;
  }

  /**
   * Pause a campaign.
   */
  public Campaign pauseCampaign(Long campaignId) {
    Campaign campaign = getCampaignOrThrow(campaignId);

    if (campaign.getStatus() != Campaign.CampaignStatus.ACTIVE) {
      throw new IllegalStateException("Only active campaigns can be paused");
    }

    campaign.pause();
    campaign = campaignRepository.save(campaign);
    log.info("Paused campaign: {}", campaign.getCode());
    return campaign;
  }

  /**
   * End a campaign.
   */
  public Campaign endCampaign(Long campaignId) {
    Campaign campaign = getCampaignOrThrow(campaignId);
    campaign.end();
    campaign = campaignRepository.save(campaign);
    log.info("Ended campaign: {}", campaign.getCode());
    return campaign;
  }

  /**
   * Schedule a campaign for future activation.
   */
  public Campaign scheduleCampaign(Long campaignId) {
    Campaign campaign = getCampaignOrThrow(campaignId);

    if (campaign.getStatus() != Campaign.CampaignStatus.DRAFT) {
      throw new IllegalStateException("Only draft campaigns can be scheduled");
    }

    if (campaign.getStartDate().isBefore(Instant.now())) {
      throw new IllegalArgumentException("Start date must be in the future");
    }

    campaign.setStatus(Campaign.CampaignStatus.SCHEDULED);
    campaign = campaignRepository.save(campaign);
    log.info("Scheduled campaign: {}", campaign.getCode());
    return campaign;
  }

  /**
   * Apply campaign to an order and calculate discount.
   */
  public BigDecimal applyCampaign(String code, BigDecimal subtotal, int customerUseCount) {
    Campaign campaign = campaignRepository.findByCode(code)
        .orElseThrow(() -> new IllegalArgumentException("Campaign not found: " + code));

    if (!campaign.isActive()) {
      throw new IllegalStateException("Campaign is not active");
    }

    if (!campaign.hasAvailableUses()) {
      throw new IllegalStateException("Campaign usage limit reached");
    }

    if (!campaign.canCustomerUse(customerUseCount)) {
      throw new IllegalStateException("Customer has reached usage limit for this campaign");
    }

    BigDecimal discount = campaign.calculateDiscount(subtotal);

    // Record the use
    campaign.recordUse();
    campaignRepository.save(campaign);

    log.info("Applied campaign {} for discount: {}", code, discount);
    return discount;
  }

  /**
   * Scheduled job to activate campaigns.
   */
  public int activateScheduledCampaigns() {
    List<Campaign> toActivate = campaignRepository.findCampaignsToActivate(Instant.now());
    for (Campaign campaign : toActivate) {
      campaign.activate();
      campaignRepository.save(campaign);
      log.info("Auto-activated campaign: {}", campaign.getCode());
    }
    return toActivate.size();
  }

  /**
   * Scheduled job to end expired campaigns.
   */
  public int endExpiredCampaigns() {
    List<Campaign> toEnd = campaignRepository.findCampaignsToEnd(Instant.now());
    for (Campaign campaign : toEnd) {
      campaign.end();
      campaignRepository.save(campaign);
      log.info("Auto-ended campaign: {}", campaign.getCode());
    }
    return toEnd.size();
  }

  private Campaign getCampaignOrThrow(Long campaignId) {
    return campaignRepository.findById(campaignId)
        .orElseThrow(() -> new IllegalArgumentException("Campaign not found: " + campaignId));
  }
}
