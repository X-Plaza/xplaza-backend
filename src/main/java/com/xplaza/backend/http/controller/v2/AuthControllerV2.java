/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller.v2;

import java.security.NoSuchAlgorithmException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.common.util.ApiResponseV2;
import com.xplaza.backend.service.*;
import com.xplaza.backend.service.entity.AdminUser;
import com.xplaza.backend.service.entity.ConfirmationToken;
import com.xplaza.backend.service.entity.Login;

/**
 * V2 Auth Controller - Unified authentication API.
 *
 * Provides: - Admin login - Customer login - Password reset flow (OTP) -
 * Password change
 */
@RestController
@RequestMapping("/api/v2/auth")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Authentication V2", description = "V2 Authentication APIs")
public class AuthControllerV2 {

  private final AdminUserLoginService adminUserLoginService;
  private final AdminUserService adminUserService;
  private final CustomerLoginService customerLoginService;
  private final CustomerUserService customerUserService;
  private final ConfirmationTokenService confirmationTokenService;
  private final SecurityService securityService;

  /**
   * POST /api/v2/auth/admin/login
   *
   * Admin user login.
   */
  @PostMapping("/admin/login")
  @Operation(summary = "Admin login", description = "Authenticate admin user and get session details")
  public ResponseEntity<ApiResponseV2<Login>> adminLogin(@RequestBody @Valid LoginRequest request) {

    String username = request.username().toLowerCase();
    Login loginDetails = adminUserLoginService.getAdminUserDetails(username);

    if (loginDetails == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ApiResponseV2.error("INVALID_CREDENTIALS", "Invalid username or password"));
    }

    boolean isValidUser;
    if (username.equalsIgnoreCase("admin@gmail.com")) {
      isValidUser = adminUserLoginService.isValidMasterAdmin(username, request.password());
    } else {
      isValidUser = adminUserLoginService.isValidAdminUser(username, request.password());
    }

    if (!isValidUser) {
      loginDetails.setAuthentication(false);
      loginDetails.setShopList(null);
      loginDetails.setPermissions(null);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ApiResponseV2.error("INVALID_CREDENTIALS", "Invalid username or password"));
    }

    loginDetails.setAuthentication(true);
    return ResponseEntity.ok(ApiResponseV2.ok(loginDetails));
  }

  /**
   * POST /api/v2/auth/customer/login
   *
   * Customer user login.
   */
  @PostMapping("/customer/login")
  @Operation(summary = "Customer login", description = "Authenticate customer and get session details")
  public ResponseEntity<ApiResponseV2<CustomerLoginResponse>> customerLogin(
      @RequestBody @Valid LoginRequest request) {

    String username = request.username().toLowerCase();
    boolean isValid = customerLoginService.isValidCustomerUser(username, request.password());

    if (!isValid) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ApiResponseV2.error("INVALID_CREDENTIALS", "Invalid username or password"));
    }

    // Get customer details using available method
    com.xplaza.backend.service.entity.Customer customer = customerLoginService.getCustomerLoginDetails(username);
    if (customer == null) {
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
          .body(ApiResponseV2.error("INVALID_CREDENTIALS", "Invalid username or password"));
    }

    CustomerLoginResponse response = new CustomerLoginResponse(
        customer.getCustomerId(),
        username,
        customer.getFirstName() + " " + customer.getLastName(),
        true);

    return ResponseEntity.ok(ApiResponseV2.ok(response));
  }

  /**
   * POST /api/v2/auth/password-reset/request
   *
   * Request OTP for password reset.
   */
  @PostMapping("/password-reset/request")
  @Operation(summary = "Request password reset", description = "Send OTP to user email for password reset")
  public ResponseEntity<ApiResponseV2<Void>> requestPasswordReset(
      @RequestParam @Email @NotBlank String email,
      @RequestParam(defaultValue = "ADMIN") UserType userType) {

    String username = email.toLowerCase();

    // Check if user exists
    if (userType == UserType.ADMIN) {
      AdminUser user = adminUserService.listAdminUser(username);
      if (user == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponseV2.error("USER_NOT_FOUND", "No account found with this email"));
      }
    } else {
      // Check customer exists
      com.xplaza.backend.service.entity.Customer customer = customerLoginService.getCustomerLoginDetails(username);
      if (customer == null) {
        return ResponseEntity.status(HttpStatus.NOT_FOUND)
            .body(ApiResponseV2.error("USER_NOT_FOUND", "No account found with this email"));
      }
    }

    confirmationTokenService.sendOTP(username);
    return ResponseEntity.ok(ApiResponseV2.ok("OTP has been sent to your email"));
  }

  /**
   * POST /api/v2/auth/password-reset/verify
   *
   * Verify OTP.
   */
  @PostMapping("/password-reset/verify")
  @Operation(summary = "Verify OTP", description = "Verify the OTP sent to user email")
  public ResponseEntity<ApiResponseV2<Void>> verifyOTP(
      @RequestParam @Email @NotBlank String email,
      @RequestParam @NotBlank String otp) {

    String username = email.toLowerCase();
    ConfirmationToken token = confirmationTokenService.getConfirmationToken(otp);

    if (token == null || !token.getEmail().equals(username)) {
      return ResponseEntity.status(HttpStatus.BAD_REQUEST)
          .body(ApiResponseV2.error("INVALID_OTP", "OTP is invalid or expired"));
    }

    return ResponseEntity.ok(ApiResponseV2.ok("OTP verified successfully"));
  }

  /**
   * POST /api/v2/auth/password-reset/complete
   *
   * Complete password reset with new password.
   */
  @PostMapping("/password-reset/complete")
  @Operation(summary = "Complete password reset", description = "Set new password after OTP verification")
  public ResponseEntity<ApiResponseV2<Void>> completePasswordReset(
      @RequestBody @Valid PasswordResetRequest request) {

    String username = request.email().toLowerCase();

    try {
      byte[] byteSalt = securityService.getSalt();
      byte[] byteDigestPsw = securityService.getSaltedHashSHA512(request.newPassword(), byteSalt);
      String strDigestPsw = securityService.toHex(byteDigestPsw);
      String strSalt = securityService.toHex(byteSalt);

      if (request.userType() == UserType.ADMIN) {
        adminUserService.changeAdminUserPassword(strDigestPsw, strSalt, username);
      } else {
        customerUserService.changeCustomerPassword(strDigestPsw, strSalt, username);
      }

      return ResponseEntity.ok(ApiResponseV2.ok("Password reset successfully"));
    } catch (NoSuchAlgorithmException ex) {
      log.error("Salt generation error during password reset", ex);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponseV2.error("INTERNAL_ERROR", "Failed to reset password"));
    }
  }

  // ===== Request/Response Records =====

  public record LoginRequest(
      @NotBlank(message = "Username is required") @Email(message = "Username must be a valid email") String username,
      @NotBlank(message = "Password is required") String password
  ) {
  }

  public record CustomerLoginResponse(
      Long customerId,
      String email,
      String name,
      boolean authenticated
  ) {
  }

  public record PasswordResetRequest(
      @NotBlank(message = "Email is required") @Email(message = "Must be a valid email") String email,
      @NotBlank(message = "New password is required") String newPassword,
      UserType userType
  ) {
  }

  public enum UserType {
    ADMIN,
    CUSTOMER
  }
}
