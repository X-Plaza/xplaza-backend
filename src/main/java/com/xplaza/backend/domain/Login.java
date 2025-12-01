/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.domain;

import java.util.List;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Login {
  private AdminUser adminUser;

  private String userName;

  private String fullName;

  private String roleId;

  private String roleName;

  private boolean authentication;

  private List<AdminUserShopLink> shopList;

  private List<Permission> permissions;
}
