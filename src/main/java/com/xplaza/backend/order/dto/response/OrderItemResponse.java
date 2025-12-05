/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.order.dto.response;

import lombok.Data;

@Data
public class OrderItemResponse {
  private Long id;
  private Long productId;
  private Long orderId;
  private Long quantity;
  // add other fields as needed
}
