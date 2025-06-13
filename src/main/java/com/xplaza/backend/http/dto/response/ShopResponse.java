/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopResponse {
  private Long shopId;
  private String shopName;
  private String shopDescription;
  private String shopAddress;
  private Long locationId;
  private String locationName;
  private String shopOwner;
}
