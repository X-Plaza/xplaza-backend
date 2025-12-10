/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.delivery.domain.entity;

import java.time.LocalDateTime;

import jakarta.persistence.*;

import lombok.*;

import com.xplaza.backend.geography.domain.entity.City;
import com.xplaza.backend.shop.domain.entity.Shop;

@Table(name = "delivery_costs")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryCost {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long deliveryCostId;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "fk_shop_id", nullable = false)
  private Shop shop;

  @ManyToOne(fetch = FetchType.EAGER)
  @JoinColumn(name = "fk_city_id", nullable = false)
  private City city;

  private Double deliveryFee;

  private Double minimumOrderAmount;

  private Double freeDeliveryThreshold;

  private Integer estimatedDeliveryMinutes;

  @Builder.Default
  private Boolean isActive = true;

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
