/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.customer.dto.response;

import lombok.Data;

@Data
public class CustomerUserResponse {
  private Long id;
  private String username;
  private String email;
  // add other fields as needed
}
