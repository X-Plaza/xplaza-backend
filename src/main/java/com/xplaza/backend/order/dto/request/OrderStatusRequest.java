/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.order.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class OrderStatusRequest {
  @NotBlank(message = "Status name is required")
  private String statusName;
  private String description;
  private String color;
  private Integer sortOrder;
  private Boolean isActive;
}
