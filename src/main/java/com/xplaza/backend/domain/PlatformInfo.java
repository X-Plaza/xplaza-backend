/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.domain;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatformInfo {
  Long id;

  String name;

  String invoicePrefix;

  String cellNo;

  String additionalInfo;

  String bannerImage;

  String bannerImagePath;
}
