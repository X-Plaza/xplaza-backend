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
public class Country {
  private Long countryId;
  private String iso;
  private String countryName;
  private String niceName;
  private String iso3;
  private Short numCode;
  private Integer phoneCode;
  private List<State> states;
}
