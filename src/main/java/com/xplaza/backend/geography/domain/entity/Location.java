/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.geography.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;

@Table(name = "locations")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Location {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long locationId;

  @Column(nullable = false)
  private String locationName;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "fk_city_id", nullable = false)
  private City city;

  private String postalCode;

  private Double latitude;

  private Double longitude;

  private Integer createdBy;

  private LocalDateTime createdAt;

  private Integer lastUpdatedBy;

  private LocalDateTime lastUpdatedAt;

  @PrePersist
  protected void onCreate() {
    createdAt = LocalDateTime.now();
  }

  @PreUpdate
  protected void onUpdate() {
    lastUpdatedAt = LocalDateTime.now();
  }
}
