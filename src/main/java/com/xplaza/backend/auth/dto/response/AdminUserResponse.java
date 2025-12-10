/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.auth.dto.response;

import java.util.List;

import lombok.Data;

@Data
public class AdminUserResponse {
  private Long id;
  private String fullName;
  private String email;
  private Long roleId;
  private List<Long> shopIds;
}
