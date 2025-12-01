/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.domain;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LocationList {
  private Long id;
  private Long locationId;
  private String locationName;
  private Long cityId;
  private String cityName;
  private Long stateId;
  private String stateName;
  private Long countryId;
  private String countryName;
}