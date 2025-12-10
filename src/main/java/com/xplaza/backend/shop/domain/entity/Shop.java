/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.shop.domain.entity;

import jakarta.persistence.*;

import lombok.*;

@Table(name = "shops")
@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shop {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long shopId;

  private String shopName;

  private String shopDescription;

  private String shopAddress;

  @Column(name = "fk_location_id")
  private Long locationId;

  private String shopOwner;
}
