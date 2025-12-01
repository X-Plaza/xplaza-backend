/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller;

import java.security.NoSuchAlgorithmException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.telesign.MessagingClient;
import com.telesign.Util;
import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.service.ConfirmationTokenService;
import com.xplaza.backend.service.CustomerLoginService;
import com.xplaza.backend.service.CustomerSignupService;
import com.xplaza.backend.service.SecurityService;
import com.xplaza.backend.service.entity.ConfirmationToken;
import com.xplaza.backend.service.entity.Customer;

@RestController
@RequestMapping("/api/v1/customer-signup")
public class CustomerSignupController extends BaseController {
  @Autowired
  private CustomerSignupService customerSignupService;

  @Autowired
  private SecurityService securityService;

  @Autowired
  private CustomerLoginService customerLoginService;

  @Autowired
  private ConfirmationTokenService confirmationTokenService;

  @PostMapping
  public ResponseEntity<ApiResponse> signupCustomer(@RequestBody @Valid Customer customer) {
    long start = System.currentTimeMillis();
    ConfirmationToken token = confirmationTokenService.getConfirmationToken(customer.getOtp());
    if (token == null) {
      long responseTime = System.currentTimeMillis() - start;
      return new ResponseEntity<>(new ApiResponse(responseTime, "Signup Customer", HttpStatus.FORBIDDEN.value(),
          "Failed", "Confirmation code does not match!", null), HttpStatus.FORBIDDEN);
    }
    if (!token.getEmail().equals(customer.getEmail().toLowerCase())) {
      long responseTime = System.currentTimeMillis() - start;
      return new ResponseEntity<>(new ApiResponse(responseTime, "Signup Customer", HttpStatus.FORBIDDEN.value(),
          "Failed", "Confirmation code does not match!", null), HttpStatus.FORBIDDEN);
    }
    Date today = new Date();
    if (today.after(token.getValidTill())) {
      long responseTime = System.currentTimeMillis() - start;
      return new ResponseEntity<>(new ApiResponse(responseTime, "Signup Customer", HttpStatus.FORBIDDEN.value(),
          "Failed", "Confirmation code expired! Please get a new code!", null), HttpStatus.FORBIDDEN);
    }
    Customer customerLogin = customerLoginService
        .getCustomerLoginDetails(customer.getEmail().toLowerCase());
    if (customerLogin != null) {
      long responseTime = System.currentTimeMillis() - start;
      return new ResponseEntity<>(new ApiResponse(responseTime, "Signup Customer", HttpStatus.FORBIDDEN.value(),
          "Failed", "User Already Exist!", null), HttpStatus.FORBIDDEN);
    }
    // Encrypt Password with Salt
    String temp_password = customer.getPassword();
    byte[] byteSalt = null;
    try {
      byteSalt = securityService.getSalt();
    } catch (NoSuchAlgorithmException ex) {
      Logger.getLogger("Salt error").log(Level.SEVERE, null, ex);
    }
    byte[] biteDigestPsw = securityService.getSaltedHashSHA512(customer.getPassword(), byteSalt);
    String strDigestPsw = securityService.toHex(biteDigestPsw);
    String strSalt = securityService.toHex(byteSalt);
    customer.setPassword(strDigestPsw);
    customer.setSalt(strSalt);
    customer.setEmail(customer.getEmail().toLowerCase());
    customerSignupService.signupCustomer(customer);
    customerSignupService.sendLoginDetails(customer.getEmail(), temp_password);
    long responseTime = System.currentTimeMillis() - start;
    return new ResponseEntity<>(new ApiResponse(responseTime, "Signup Customer", HttpStatus.CREATED.value(), "Success",
        "Customer account has been created successfully.", null), HttpStatus.CREATED);
  }

  @PostMapping("/send-otp")
  public ResponseEntity<ApiResponse> sendOTP(@RequestParam("cell_no") @Valid String cell_no) {
    long start = System.currentTimeMillis();
    String customerId = "D22F37FA-1914-48E3-B018-ACFF0E611C3C";
    String apiKey = "1UKbxwwvd0J7q5ne4vtTAlnE4DVqoVqQ+yvKKyHEISDXUu35Jzv7UIeXTlFcR4IYlIp/+uj2EpvXLbksmu/xkA==";
    String verifyCode = Util.randomWithNDigits(5);
    String message = String.format("Your OTP is %s", verifyCode) + "\n\nTeam Xwinkel";
    String messageType = "OTP";
    try {
      MessagingClient messagingClient = new MessagingClient(customerId, apiKey);
      messagingClient.message(cell_no, message, messageType, null);
    } catch (Exception e) {
      e.printStackTrace();
    }
    long responseTime = System.currentTimeMillis() - start;
    return new ResponseEntity<>(new ApiResponse(responseTime, "Send OTP", HttpStatus.CREATED.value(), "Success",
        "An OTP has been sent to your phone.", null), HttpStatus.CREATED);
  }
}
