/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.delivery.domain.entity;

import java.time.LocalDateTime;
import java.time.LocalTime;

import jakarta.persistence.*;

import lombok.*;

import com.xplaza.backend.shop.domain.entity.Shop;

@Table(name = "delivery_schedules")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliverySchedule {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long deliveryScheduleId;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "fk_shop_id", nullable = false)
  private Shop shop;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "fk_day_id", nullable = false)
  private Day day;

  private LocalTime startTime;

  private LocalTime endTime;

  @Builder.Default
  private Boolean isAvailable = true;

  private Integer maxDeliveries;

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
