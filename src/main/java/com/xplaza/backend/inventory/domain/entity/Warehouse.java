/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.inventory.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import jakarta.persistence.*;

import lombok.*;

/**
 * Warehouse/fulfillment center.
 */
@Entity
@Table(name = "warehouses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Warehouse {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "warehouse_id")
  private Long warehouseId;

  @Column(name = "code", nullable = false, unique = true, length = 20)
  private String code;

  @Column(name = "name", nullable = false, length = 100)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 30)
  @Builder.Default
  private WarehouseType type = WarehouseType.FULFILLMENT_CENTER;

  // Address
  @Column(name = "address_line1", nullable = false, length = 255)
  private String addressLine1;

  @Column(name = "address_line2", length = 255)
  private String addressLine2;

  @Column(name = "city", nullable = false, length = 100)
  private String city;

  @Column(name = "state", length = 100)
  private String state;

  @Column(name = "postal_code", nullable = false, length = 20)
  private String postalCode;

  @Column(name = "country_code", nullable = false, length = 2)
  private String countryCode;

  // Coordinates for shipping calculation
  @Column(name = "latitude", precision = 10, scale = 7)
  private BigDecimal latitude;

  @Column(name = "longitude", precision = 10, scale = 7)
  private BigDecimal longitude;

  // Contact
  @Column(name = "contact_name", length = 100)
  private String contactName;

  @Column(name = "contact_phone", length = 50)
  private String contactPhone;

  @Column(name = "contact_email", length = 255)
  private String contactEmail;

  // Capacity
  @Column(name = "capacity_units")
  private Integer capacityUnits;

  @Column(name = "current_utilization", precision = 5, scale = 2)
  @Builder.Default
  private BigDecimal currentUtilization = BigDecimal.ZERO;

  // Priority for order fulfillment
  @Column(name = "priority")
  @Builder.Default
  private Integer priority = 0;

  // Status
  @Column(name = "is_active")
  @Builder.Default
  private Boolean isActive = true;

  @Column(name = "accepts_returns")
  @Builder.Default
  private Boolean acceptsReturns = true;

  @Column(name = "accepts_inbound")
  @Builder.Default
  private Boolean acceptsInbound = true;

  // Operating hours (JSON format)
  @Column(name = "operating_hours", columnDefinition = "TEXT")
  private String operatingHours;

  // Supported carriers (comma-separated carrier codes)
  @Column(name = "supported_carriers", columnDefinition = "TEXT")
  private String supportedCarriers;

  @Column(name = "created_at")
  @Builder.Default
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at")
  @Builder.Default
  private Instant updatedAt = Instant.now();

  @OneToMany(mappedBy = "warehouse", fetch = FetchType.LAZY)
  @Builder.Default
  private List<InventoryItem> inventoryItems = new ArrayList<>();

  public enum WarehouseType {
    /** Main fulfillment center */
    FULFILLMENT_CENTER,
    /** Retail store with ship-from-store */
    RETAIL_STORE,
    /** Third party logistics */
    THIRD_PARTY,
    /** Cross-dock facility */
    CROSS_DOCK,
    /** Returns processing center */
    RETURNS_CENTER,
    /** Drop ship vendor location */
    DROP_SHIP
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = Instant.now();
  }

  /**
   * Check if warehouse supports a carrier.
   */
  public boolean supportsCarrier(String carrierCode) {
    if (supportedCarriers == null || supportedCarriers.isEmpty()) {
      return true;
    }
    return supportedCarriers.contains(carrierCode);
  }

  /**
   * Calculate distance to a location (Haversine formula).
   */
  public double calculateDistanceKm(BigDecimal targetLat, BigDecimal targetLon) {
    if (latitude == null || longitude == null || targetLat == null || targetLon == null) {
      return Double.MAX_VALUE;
    }

    final int R = 6371; // Earth radius in km

    double lat1 = Math.toRadians(latitude.doubleValue());
    double lat2 = Math.toRadians(targetLat.doubleValue());
    double deltaLat = Math.toRadians(targetLat.subtract(latitude).doubleValue());
    double deltaLon = Math.toRadians(targetLon.subtract(longitude).doubleValue());

    double a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
        Math.cos(lat1) * Math.cos(lat2) *
            Math.sin(deltaLon / 2) * Math.sin(deltaLon / 2);
    double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return R * c;
  }
}
