/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.order.dto.response;

import lombok.Data;

@Data
public class OrderStatusResponse {
  private Long orderStatusId;
  private String statusName;
  private String description;
  private String color;
  private Integer sortOrder;
  private Boolean isActive;
}
