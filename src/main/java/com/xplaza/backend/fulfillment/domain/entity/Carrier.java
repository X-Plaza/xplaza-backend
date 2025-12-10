/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.fulfillment.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import lombok.*;

/**
 * Shipping carrier configuration.
 */
@Entity
@Table(name = "carriers")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Carrier {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "carrier_id")
  private Long carrierId;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Column(name = "code", nullable = false, unique = true, length = 50)
  private String code;

  @Column(name = "display_name", length = 100)
  private String displayName;

  @Column(name = "logo_url", length = 500)
  private String logoUrl;

  // Integration
  @Column(name = "api_endpoint", length = 500)
  private String apiEndpoint;

  @Column(name = "api_key_encrypted", length = 500)
  private String apiKeyEncrypted;

  @Column(name = "api_secret_encrypted", length = 500)
  private String apiSecretEncrypted;

  @Column(name = "account_number", length = 100)
  private String accountNumber;

  // Tracking
  @Column(name = "tracking_url_template", length = 500)
  private String trackingUrlTemplate;

  // Capabilities
  @Column(name = "supports_insurance")
  @Builder.Default
  private Boolean supportsInsurance = false;

  @Column(name = "supports_signature")
  @Builder.Default
  private Boolean supportsSignature = false;

  @Column(name = "supports_same_day")
  @Builder.Default
  private Boolean supportsSameDay = false;

  @Column(name = "supports_international")
  @Builder.Default
  private Boolean supportsInternational = false;

  @Column(name = "supports_tracking")
  @Builder.Default
  private Boolean supportsTracking = true;

  // Rates
  @Column(name = "base_rate", precision = 10, scale = 2)
  private BigDecimal baseRate;

  @Column(name = "per_kg_rate", precision = 10, scale = 4)
  private BigDecimal perKgRate;

  @Column(name = "currency", length = 3)
  @Builder.Default
  private String currency = "EUR";

  // Limits
  @Column(name = "max_weight_kg", precision = 10, scale = 3)
  private BigDecimal maxWeightKg;

  @Column(name = "max_length_cm", precision = 10, scale = 2)
  private BigDecimal maxLengthCm;

  // Status
  @Column(name = "is_active")
  @Builder.Default
  private Boolean isActive = true;

  @Column(name = "priority")
  @Builder.Default
  private Integer priority = 0;

  @Column(name = "created_at")
  @Builder.Default
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at")
  @Builder.Default
  private Instant updatedAt = Instant.now();

  // Supported countries (comma-separated ISO codes)
  @Column(name = "supported_countries", columnDefinition = "TEXT")
  private String supportedCountries;

  @OneToMany(mappedBy = "carrier", fetch = FetchType.LAZY)
  @Builder.Default
  private List<Shipment> shipments = new ArrayList<>();

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = Instant.now();
  }

  /**
   * Generate tracking URL for a shipment.
   */
  public String generateTrackingUrl(String trackingNumber) {
    if (trackingUrlTemplate == null || trackingNumber == null) {
      return null;
    }
    return trackingUrlTemplate.replace("{tracking_number}", trackingNumber);
  }

  /**
   * Check if carrier supports the destination country.
   */
  public boolean supportsCountry(String countryCode) {
    if (supportedCountries == null || supportedCountries.isEmpty()) {
      return true; // If not specified, assume all countries
    }
    return supportedCountries.contains(countryCode);
  }

  /**
   * Calculate shipping cost estimate.
   */
  public BigDecimal calculateCost(BigDecimal weightKg) {
    if (baseRate == null) {
      return BigDecimal.ZERO;
    }
    BigDecimal cost = baseRate;
    if (perKgRate != null && weightKg != null) {
      cost = cost.add(perKgRate.multiply(weightKg));
    }
    return cost;
  }
}
