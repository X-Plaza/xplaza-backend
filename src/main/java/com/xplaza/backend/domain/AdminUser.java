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
public class AdminUser {
  private Long adminUserId;
  private String fullName;
  private String userName;
  private String password;
  private String salt;
  private Long roleId;
  private String confirmationCode;
  private List<AdminUserShopLink> adminUserShopLinks;
}
