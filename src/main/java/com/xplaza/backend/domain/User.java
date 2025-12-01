/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.domain;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {
  private Long userId;
  private String userName;
  private String password;
  private String salt;
  private String fullName;
  private String email;
  private String mobileNo;
  private String confirmationCode;
}
