/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.marketing.controller;

import java.math.BigDecimal;
import java.util.List;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.marketing.domain.entity.Campaign;
import com.xplaza.backend.marketing.service.CampaignService;

/**
 * REST controller for marketing campaign operations.
 */
@RestController
@RequestMapping("/api/v1/campaigns")
@RequiredArgsConstructor
@Tag(name = "Campaigns", description = "Marketing campaign management APIs")
public class CampaignController {

  private final CampaignService campaignService;

  @Operation(summary = "Create a new campaign")
  @PostMapping
  public ResponseEntity<Campaign> createCampaign(@RequestBody Campaign campaign) {
    Campaign created = campaignService.createCampaign(campaign);
    return ResponseEntity.ok(created);
  }

  @Operation(summary = "Update a campaign")
  @PutMapping("/{campaignId}")
  public ResponseEntity<Campaign> updateCampaign(
      @PathVariable Long campaignId,
      @RequestBody Campaign updates) {
    Campaign updated = campaignService.updateCampaign(campaignId, updates);
    return ResponseEntity.ok(updated);
  }

  @Operation(summary = "Get campaign by ID")
  @GetMapping("/{campaignId}")
  public ResponseEntity<Campaign> getCampaign(@PathVariable Long campaignId) {
    return campaignService.getCampaign(campaignId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Get campaign by code")
  @GetMapping("/code/{code}")
  public ResponseEntity<Campaign> getCampaignByCode(@PathVariable String code) {
    return campaignService.getCampaignByCode(code)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Get active campaigns")
  @GetMapping("/active")
  public ResponseEntity<List<Campaign>> getActiveCampaigns() {
    return ResponseEntity.ok(campaignService.getActiveCampaigns());
  }

  @Operation(summary = "Get homepage campaigns")
  @GetMapping("/homepage")
  public ResponseEntity<List<Campaign>> getHomepageCampaigns() {
    return ResponseEntity.ok(campaignService.getHomepageCampaigns());
  }

  @Operation(summary = "Get campaigns by status")
  @GetMapping("/status/{status}")
  public ResponseEntity<Page<Campaign>> getCampaignsByStatus(
      @PathVariable Campaign.CampaignStatus status,
      Pageable pageable) {
    return ResponseEntity.ok(campaignService.getCampaignsByStatus(status, pageable));
  }

  @Operation(summary = "Get active campaigns for a product")
  @GetMapping("/products/{productId}")
  public ResponseEntity<List<Campaign>> getCampaignsForProduct(@PathVariable Long productId) {
    return ResponseEntity.ok(campaignService.getActiveCampaignsForProduct(productId));
  }

  @Operation(summary = "Activate a campaign")
  @PostMapping("/{campaignId}/activate")
  public ResponseEntity<Campaign> activateCampaign(@PathVariable Long campaignId) {
    Campaign activated = campaignService.activateCampaign(campaignId);
    return ResponseEntity.ok(activated);
  }

  @Operation(summary = "Pause a campaign")
  @PostMapping("/{campaignId}/pause")
  public ResponseEntity<Campaign> pauseCampaign(@PathVariable Long campaignId) {
    Campaign paused = campaignService.pauseCampaign(campaignId);
    return ResponseEntity.ok(paused);
  }

  @Operation(summary = "End a campaign")
  @PostMapping("/{campaignId}/end")
  public ResponseEntity<Campaign> endCampaign(@PathVariable Long campaignId) {
    Campaign ended = campaignService.endCampaign(campaignId);
    return ResponseEntity.ok(ended);
  }

  @Operation(summary = "Schedule a campaign for future activation")
  @PostMapping("/{campaignId}/schedule")
  public ResponseEntity<Campaign> scheduleCampaign(@PathVariable Long campaignId) {
    Campaign scheduled = campaignService.scheduleCampaign(campaignId);
    return ResponseEntity.ok(scheduled);
  }

  @Operation(summary = "Apply campaign code and calculate discount")
  @PostMapping("/apply")
  public ResponseEntity<DiscountResult> applyCampaign(@RequestBody ApplyCampaignRequest request) {
    BigDecimal discount = campaignService.applyCampaign(
        request.code(),
        request.subtotal(),
        request.customerUseCount());
    return ResponseEntity.ok(new DiscountResult(discount, request.subtotal().subtract(discount)));
  }

  public record ApplyCampaignRequest(
      String code,
      BigDecimal subtotal,
      int customerUseCount
  ) {
  }

  public record DiscountResult(
      BigDecimal discountAmount,
      BigDecimal newTotal
  ) {
  }
}
