/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.customer.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import lombok.Data;

@Data
public class CustomerUserRequest {
  @NotBlank(message = "Username is required")
  private String username;
  @NotBlank(message = "Email is required")
  @Email(message = "Email should be valid")
  private String email;
  // add other fields as needed
}
