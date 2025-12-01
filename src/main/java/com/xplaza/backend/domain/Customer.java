/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.domain;

import java.util.Date;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Customer {
  private Long customerId;
  private String firstName;
  private String lastName;
  private String houseNo;
  private String streetName;
  private Integer postCode;
  private String area;
  private String city;
  private String country;
  private String mobileNo;
  private String email;
  private Date dateOfBirth;
  private String password;
  private String salt;
  private String otp;
  private Date createdAt;
  private Date updatedAt;
  private boolean authentication;
}