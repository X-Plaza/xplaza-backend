/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.auth.dto.request;

import java.util.List;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import lombok.Data;

@Data
public class AdminUserRequest {
  @NotBlank(message = "Full name is required")
  private String fullName;
  @NotBlank(message = "Email is required")
  @Email(message = "Email should be valid")
  private String email;
  @NotNull(message = "Role ID is required")
  private Long roleId;
  private List<Long> shopIds;
  @NotBlank(message = "Username is required")
  private String userName;
  @NotBlank(message = "Password is required")
  private String password;
  private String salt;
  private String confirmationCode;
}
