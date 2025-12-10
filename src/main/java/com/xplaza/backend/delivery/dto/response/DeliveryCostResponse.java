/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.delivery.dto.response;

import lombok.Data;

@Data
public class DeliveryCostResponse {
  private Long id;
  private String name;
  private Double cost;
  // add other fields as needed
}
