/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.auth.dto.request;

import jakarta.validation.constraints.NotBlank;

import lombok.*;

@Data
public class AuthenticationRequest {
  @NotBlank(message = "Username is required")
  String username;
  @NotBlank(message = "Password is required")
  String password;
}
