/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.geography.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;

@Table(name = "states")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class State {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long stateId;

  @Column(nullable = false)
  private String stateName;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "fk_country_id", nullable = false)
  private Country country;

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
