/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.domain;

import java.util.Calendar;
import java.util.Date;
import java.util.Objects;
import java.util.Random;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ConfirmationToken {
  private Long tokenId;

  private String confirmationToken;

  private String email;

  private Date createdDate;

  private Date validTill;

  public ConfirmationToken(String email, String type) {
    Random rnd = new Random();
    int number;
    if (Objects.equals(type, "OTP")) {
      number = rnd.nextInt(99999999);
      confirmationToken = String.format("%08d", number);
    } else {
      number = rnd.nextInt(999999);
      confirmationToken = String.format("%06d", number);
    }
    this.email = email;
    createdDate = new Date();
    Calendar calendar = Calendar.getInstance();
    calendar.add(Calendar.MINUTE, 30);
    this.validTill = calendar.getTime();
  }
}
