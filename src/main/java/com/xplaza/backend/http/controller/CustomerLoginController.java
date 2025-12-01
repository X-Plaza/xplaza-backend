/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.logging.Level;
import java.util.logging.Logger;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.service.ConfirmationTokenService;
import com.xplaza.backend.service.CustomerLoginService;
import com.xplaza.backend.service.CustomerUserService;
import com.xplaza.backend.service.SecurityService;
import com.xplaza.backend.service.entity.ConfirmationToken;
import com.xplaza.backend.service.entity.Customer;

@RestController
@RequestMapping("/api/v1/customer-login")
public class CustomerLoginController extends BaseController {
  private final CustomerLoginService customerLoginService;
  private final CustomerUserService customerUserService;
  private final ConfirmationTokenService confirmationTokenService;
  private final SecurityService securityService;

  @Autowired
  public CustomerLoginController(CustomerLoginService customerLoginService, CustomerUserService customerUserService,
      ConfirmationTokenService confirmationTokenService, SecurityService securityService) {
    this.customerLoginService = customerLoginService;
    this.customerUserService = customerUserService;
    this.confirmationTokenService = confirmationTokenService;
    this.securityService = securityService;
  }

  @PostMapping
  public ResponseEntity<String> loginCustomerUser(@RequestParam("username") @Valid String username,
      @RequestParam("password") @Valid String password) throws IOException {
    long start = System.currentTimeMillis();
    boolean isValidUser = customerLoginService.isValidCustomerUser(username.toLowerCase(), password);
    Customer dtos = new Customer();
    if (isValidUser) {
      dtos = customerLoginService.getCustomerLoginDetails(username.toLowerCase());
      dtos.setAuthentication(true);
    } else {
      dtos.setAuthentication(false);
    }
    long responseTime = System.currentTimeMillis() - start;

    ObjectMapper mapper = new ObjectMapper();
    String response = "{\n" +
        "  \"responseTime\": " + responseTime + ",\n" +
        "  \"responseType\": \"Customer Authentication\",\n" +
        "  \"status\": 200,\n" +
        "  \"response\": \"Success\",\n" +
        "  \"msg\": \"\",\n" +
        "  \"data\":" +
        mapper.writeValueAsString(dtos) + "\n}";
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @PostMapping("/send-otp")
  public ResponseEntity<ApiResponse> sendOTP(@RequestParam("username") @Valid String username) {
    long start = System.currentTimeMillis();
    Customer customer = customerLoginService.getCustomerLoginDetails(username.toLowerCase());
    if (customer == null) {
      long responseTime = System.currentTimeMillis() - start;
      return new ResponseEntity<>(new ApiResponse(responseTime, "Send OTP", HttpStatus.FORBIDDEN.value(),
          "Failed", "User Does Not Exist!", null), HttpStatus.FORBIDDEN);
    }
    confirmationTokenService.sendOTPToCustomer(username.toLowerCase());
    long responseTime = System.currentTimeMillis() - start;
    return new ResponseEntity<>(new ApiResponse(responseTime, "Send OTP", HttpStatus.CREATED.value(),
        "Success", "An OTP has been sent to your email.", null), HttpStatus.CREATED);
  }

  @PostMapping("/validate-otp")
  public ResponseEntity<ApiResponse> validateOTP(@RequestParam("username") @Valid String username,
      @RequestParam("OTP") @Valid String OTP) {
    long start = System.currentTimeMillis();
    ConfirmationToken token = confirmationTokenService.getConfirmationToken(OTP);
    if (token == null) {
      long responseTime = System.currentTimeMillis() - start;
      return new ResponseEntity<>(new ApiResponse(responseTime, "Validate OTP", HttpStatus.FORBIDDEN.value(),
          "Failed", "OTP does not match!", null), HttpStatus.FORBIDDEN);
    }
    if (!token.getEmail().equals(username.toLowerCase())) {
      long responseTime = System.currentTimeMillis() - start;
      return new ResponseEntity<>(new ApiResponse(responseTime, "Validate OTP", HttpStatus.FORBIDDEN.value(),
          "Failed", "OTP does not match!", null), HttpStatus.FORBIDDEN);
    }
    long responseTime = System.currentTimeMillis() - start;
    return new ResponseEntity<>(new ApiResponse(responseTime, "Validate OTP", HttpStatus.OK.value(),
        "Success", "OTP matched!", null), HttpStatus.OK);
  }

  @PostMapping("/change-password")
  public ResponseEntity<ApiResponse> changeUserPassword(@RequestParam("username") @Valid String username,
      @RequestParam("newPassword") @Valid String newPassword)
      throws IOException {
    long start = System.currentTimeMillis();
    byte[] byteSalt = null;
    try {
      byteSalt = securityService.getSalt();
    } catch (NoSuchAlgorithmException ex) {
      Logger.getLogger("Salt error").log(Level.SEVERE, null, ex);
    }
    byte[] biteDigestPsw = securityService.getSaltedHashSHA512(newPassword, byteSalt);
    String strDigestPsw = securityService.toHex(biteDigestPsw);
    String strSalt = securityService.toHex(byteSalt);
    customerUserService.changeCustomerPassword(strDigestPsw, strSalt, username.toLowerCase());
    long responseTime = System.currentTimeMillis() - start;
    return new ResponseEntity<>(new ApiResponse(responseTime, "Change Customer User Password",
        HttpStatus.OK.value(), "Success", "Password has been updated successfully.", null), HttpStatus.OK);
  }

}
