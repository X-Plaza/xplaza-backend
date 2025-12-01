/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.service.AdminUserLoginService;
import com.xplaza.backend.service.AdminUserService;
import com.xplaza.backend.service.ConfirmationTokenService;
import com.xplaza.backend.service.SecurityService;
import com.xplaza.backend.service.entity.AdminUser;
import com.xplaza.backend.service.entity.ConfirmationToken;
import com.xplaza.backend.service.entity.Login;

@Deprecated(since = "1.0", forRemoval = true)
@RestController
@RequestMapping("/api/v1/login")
public class LoginController extends BaseController {
  private static final Logger logger = LoggerFactory.getLogger(LoginController.class);

  @Autowired
  private AdminUserLoginService adminUserLoginService;

  @Autowired
  private AdminUserService adminUserService;

  @Autowired
  private ConfirmationTokenService confirmationTokenService;

  @Autowired
  private SecurityService securityService;

  @PostMapping
  public ResponseEntity<Login> loginAdminUser(@RequestParam("username") @Valid String username,
      @RequestParam("password") @Valid String password) throws IOException {
    long startTime = System.currentTimeMillis();
    Login dtos = adminUserLoginService.getAdminUserDetails(username.toLowerCase());
    if (dtos != null) {
      boolean isValidUser;
      if (username.equalsIgnoreCase("admin@gmail.com"))
        isValidUser = adminUserLoginService.isValidMasterAdmin(username.toLowerCase(), password);
      else
        isValidUser = adminUserLoginService.isValidAdminUser(username.toLowerCase(), password);

      if (isValidUser) {
        dtos.setAuthentication(true);
      } else {
        dtos.setShopList(null);
        dtos.setPermissions(null);
      }
    } else {
      dtos = new Login();
      dtos.setAuthentication(false);
      dtos.setShopList(null);
      dtos.setPermissions(null);
    }

    return ResponseEntity.ok(dtos);
  }

  @PostMapping("/send-otp")
  public ResponseEntity<ApiResponse> sendOTP(@RequestParam("username") @Valid String username) {
    long startTime = System.currentTimeMillis();
    AdminUser user = adminUserService.listAdminUser(username.toLowerCase());
    if (user == null) {
      long responseTime = System.currentTimeMillis() - startTime;
      return new ResponseEntity<>(new ApiResponse(responseTime, "Send OTP", HttpStatus.FORBIDDEN.value(),
          "Failed", "User Does Not Exist!", null), HttpStatus.FORBIDDEN);
    }
    confirmationTokenService.sendOTP(username.toLowerCase());
    long responseTime = System.currentTimeMillis() - startTime;
    return new ResponseEntity<>(new ApiResponse(responseTime, "Send OTP", HttpStatus.CREATED.value(),
        "Success", "An OTP has been sent to the email.", null), HttpStatus.CREATED);
  }

  @PostMapping("/validate-otp")
  public ResponseEntity<ApiResponse> validateOTP(@RequestParam("username") @Valid String username,
      @RequestParam("OTP") @Valid String otp) {
    long startTime = System.currentTimeMillis();
    ConfirmationToken token = confirmationTokenService.getConfirmationToken(otp);
    if (token == null) {
      long responseTime = System.currentTimeMillis() - startTime;
      return new ResponseEntity<>(new ApiResponse(responseTime, "Validate OTP", HttpStatus.FORBIDDEN.value(),
          "Failed", "OTP does not match!", null), HttpStatus.FORBIDDEN);
    }
    if (!token.getEmail().equals(username.toLowerCase())) {
      long responseTime = System.currentTimeMillis() - startTime;
      return new ResponseEntity<>(new ApiResponse(responseTime, "Validate OTP", HttpStatus.FORBIDDEN.value(),
          "Failed", "OTP does not match!", null), HttpStatus.FORBIDDEN);
    }
    long responseTime = System.currentTimeMillis() - startTime;
    return new ResponseEntity<>(new ApiResponse(responseTime, "Validate OTP", HttpStatus.OK.value(),
        "Success", "OTP matched.", null), HttpStatus.OK);
  }

  @PostMapping("/change-password")
  public ResponseEntity<ApiResponse> changeAdminUserPassword(@RequestParam("username") @Valid String username,
      @RequestParam("newPassword") @Valid String newPassword) {
    long startTime = System.currentTimeMillis();
    try {
      byte[] byteSalt = securityService.getSalt();
      byte[] biteDigestPsw = securityService.getSaltedHashSHA512(newPassword, byteSalt);
      String strDigestPsw = securityService.toHex(biteDigestPsw);
      String strSalt = securityService.toHex(byteSalt);
      adminUserService.changeAdminUserPassword(strDigestPsw, strSalt, username.toLowerCase());

      long responseTime = System.currentTimeMillis() - startTime;
      return new ResponseEntity<>(new ApiResponse(responseTime, "Change Admin User Password", HttpStatus.OK.value(),
          "Success", "Password has been updated successfully.", null), HttpStatus.OK);
    } catch (NoSuchAlgorithmException ex) {
      logger.error("Salt generation error", ex);
      long responseTime = System.currentTimeMillis() - startTime;
      return new ResponseEntity<>(
          new ApiResponse(responseTime, "Change Admin User Password", HttpStatus.INTERNAL_SERVER_ERROR.value(),
              "Error", "Failed to update password: " + ex.getMessage(), null),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
