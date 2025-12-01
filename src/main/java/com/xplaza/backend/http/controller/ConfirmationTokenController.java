/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.service.ConfirmationTokenService;

@Deprecated(since = "1.0", forRemoval = true)
@RestController
@RequestMapping("/api/v1/confirmation-tokens")
public class ConfirmationTokenController {
  @Autowired
  private ConfirmationTokenService confirmationTokenService;

  @PostMapping
  public ResponseEntity<ApiResponse> sendConfirmationTokenToAdmin(@RequestParam("username") @Valid String username) {
    long start = System.currentTimeMillis();
    confirmationTokenService.sendConfirmationToken(username.toLowerCase());
    long responseTime = System.currentTimeMillis() - start;
    return new ResponseEntity<>(new ApiResponse(responseTime, "Send Confirmation Code", HttpStatus.CREATED.value(),
        "Success", "A confirmation code has been sent to the email.", null), HttpStatus.CREATED);
  }

  @PostMapping("/to-customer")
  public ResponseEntity<ApiResponse> sendConfirmationTokenToCustomer(@RequestParam("username") @Valid String username) {
    long start = System.currentTimeMillis();
    confirmationTokenService.sendConfirmationTokenToCustomer(username.toLowerCase());
    long responseTime = System.currentTimeMillis() - start;
    return new ResponseEntity<>(new ApiResponse(responseTime, "Send Confirmation Code", HttpStatus.CREATED.value(),
        "Success", "A confirmation code has been sent to the email.", null), HttpStatus.CREATED);
  }
}
