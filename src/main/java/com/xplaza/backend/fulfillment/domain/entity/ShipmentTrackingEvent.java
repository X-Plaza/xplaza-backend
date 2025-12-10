/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.fulfillment.domain.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

import lombok.*;

/**
 * Tracking event for shipment.
 */
@Entity
@Table(name = "shipment_tracking_events", indexes = {
    @Index(name = "idx_tracking_shipment", columnList = "shipment_id"),
    @Index(name = "idx_tracking_time", columnList = "event_time")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentTrackingEvent {

  @Id
  @Column(name = "event_id")
  @Builder.Default
  private UUID eventId = UUID.randomUUID();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "shipment_id", nullable = false)
  private Shipment shipment;

  @Column(name = "event_code", length = 50)
  private String eventCode;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  private Shipment.ShipmentStatus status;

  @Column(name = "description", nullable = false, length = 500)
  private String description;

  @Column(name = "location", length = 255)
  private String location;

  @Column(name = "city", length = 100)
  private String city;

  @Column(name = "state", length = 100)
  private String state;

  @Column(name = "country_code", length = 2)
  private String countryCode;

  @Column(name = "postal_code", length = 20)
  private String postalCode;

  @Column(name = "event_time", nullable = false)
  private Instant eventTime;

  @Column(name = "carrier_timestamp")
  private Instant carrierTimestamp;

  @Column(name = "raw_event_data", columnDefinition = "TEXT")
  private String rawEventData;

  @Column(name = "created_at")
  @Builder.Default
  private Instant createdAt = Instant.now();
}
