/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.order.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;

@Table(name = "order_statuses")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatus {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long orderStatusId;

  @Column(nullable = false, unique = true)
  private String statusName;

  private String description;

  private String color; // For UI display

  private Integer sortOrder;

  @Builder.Default
  private Boolean isActive = true;

  private LocalDateTime createdAt;
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
