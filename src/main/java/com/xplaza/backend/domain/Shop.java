/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.domain;

import java.util.List;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shop {
  private Long shopId;
  private String shopName;
  private String shopDescription;
  private String shopAddress;
  private Location location;
  private String shopOwner;
  private List<Product> products;
}
