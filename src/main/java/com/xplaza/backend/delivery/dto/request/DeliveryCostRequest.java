/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.delivery.dto.request;

import lombok.Data;

@Data
public class DeliveryCostRequest {
  private String name;
  private Double cost;
  private Long shopId;
  private Long cityId;
}
