/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.delivery.domain.entity;

import jakarta.persistence.*;

import lombok.*;

@Table(name = "days")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Day {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long dayId;

  @Column(nullable = false, unique = true)
  private String dayName;

  private Integer dayNumber; // 1=Monday, 7=Sunday
}
