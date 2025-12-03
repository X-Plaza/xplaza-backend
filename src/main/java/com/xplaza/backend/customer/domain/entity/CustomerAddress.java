/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.customer.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;

import jakarta.persistence.*;

import lombok.*;

/**
 * Customer address for shipping and billing.
 * 
 * A customer can have multiple addresses (Home, Office, etc.) with one marked
 * as default for each type.
 */
@Entity
@Table(name = "customer_addresses")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerAddress {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "address_id")
  private Long addressId;

  @Column(name = "customer_id", nullable = false)
  private Long customerId;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", length = 20)
  @Builder.Default
  private AddressType type = AddressType.BOTH;

  @Column(name = "is_default")
  @Builder.Default
  private Boolean isDefault = false;

  /**
   * User-friendly label for the address. Examples: "Home", "Office", "Parents'
   * House"
   */
  @Column(name = "label", length = 50)
  private String label;

  @Column(name = "first_name", nullable = false, length = 100)
  private String firstName;

  @Column(name = "last_name", nullable = false, length = 100)
  private String lastName;

  @Column(name = "company", length = 255)
  private String company;

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

  /**
   * ISO 3166-1 alpha-2 country code (e.g., "US", "DE", "GB").
   */
  @Column(name = "country_code", nullable = false, length = 2)
  private String countryCode;

  @Column(name = "phone", length = 20)
  private String phone;

  /**
   * Special delivery instructions. Examples: "Leave at door", "Ring doorbell
   * twice"
   */
  @Column(name = "instructions", columnDefinition = "TEXT")
  private String instructions;

  /**
   * GPS coordinates for precise delivery.
   */
  @Column(name = "latitude", precision = 10, scale = 8)
  private BigDecimal latitude;

  @Column(name = "longitude", precision = 11, scale = 8)
  private BigDecimal longitude;

  /**
   * Whether this address has been verified (via postal service API).
   */
  @Column(name = "is_verified")
  @Builder.Default
  private Boolean isVerified = false;

  /**
   * Whether this address is active (soft delete).
   */
  @Column(name = "is_active")
  @Builder.Default
  private Boolean isActive = true;

  @Column(name = "created_at")
  @Builder.Default
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at")
  @Builder.Default
  private Instant updatedAt = Instant.now();

  public enum AddressType {
    SHIPPING,
    BILLING,
    BOTH
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = Instant.now();
  }

  /**
   * Get full name.
   */
  public String getFullName() {
    return firstName + " " + lastName;
  }

  /**
   * Get formatted address for display.
   */
  public String getFormattedAddress() {
    StringBuilder sb = new StringBuilder();
    sb.append(addressLine1);
    if (addressLine2 != null && !addressLine2.isBlank()) {
      sb.append(", ").append(addressLine2);
    }
    sb.append(", ").append(city);
    if (state != null && !state.isBlank()) {
      sb.append(", ").append(state);
    }
    sb.append(" ").append(postalCode);
    sb.append(", ").append(countryCode);
    return sb.toString();
  }

  /**
   * Get a short display string for lists.
   */
  public String getDisplayString() {
    String labelPart = label != null ? label + ": " : "";
    return labelPart + addressLine1 + ", " + city;
  }
}
