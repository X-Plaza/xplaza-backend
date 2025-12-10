/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.geography.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;

@Table(name = "cities")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class City {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long cityId;

  @Column(nullable = false)
  private String cityName;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "fk_state_id", nullable = false)
  private State state;

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
