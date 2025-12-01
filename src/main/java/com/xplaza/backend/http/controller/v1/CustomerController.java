/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller.v1;

import java.security.NoSuchAlgorithmException;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.domain.Customer;
import com.xplaza.backend.http.dto.request.CustomerUserRequest;
import com.xplaza.backend.http.dto.response.CustomerUserResponse;
import com.xplaza.backend.mapper.CustomerMapper;
import com.xplaza.backend.service.CustomerLoginService;
import com.xplaza.backend.service.CustomerUserService;
import com.xplaza.backend.service.SecurityService;

/**
 * Customer Controller - Clean REST API design for customer management.
 */
@RestController
@RequestMapping("/api/v1/customers")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Customer Management", description = "APIs for managing customers")
public class CustomerController {

  private final CustomerUserService customerUserService;
  private final CustomerLoginService customerLoginService;
  private final SecurityService securityService;
  private final CustomerMapper customerMapper;

  @GetMapping("/{id}")
  @Operation(summary = "Get customer by ID", description = "Retrieve a customer by their ID")
  public ResponseEntity<ApiResponse<CustomerUserResponse>> getCustomer(
      @PathVariable @Positive Long id) {

    Customer entity = customerUserService.getCustomer(id);
    CustomerUserResponse dto = customerMapper.toResponse(entity);
    return ResponseEntity.ok(ApiResponse.ok(dto));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update customer", description = "Update customer information")
  public ResponseEntity<ApiResponse<CustomerUserResponse>> updateCustomer(
      @PathVariable @Positive Long id,
      @RequestBody @Valid CustomerUserRequest request) {

    Customer entity = customerMapper.toEntity(request);
    entity.setCustomerId(id);
    customerUserService.updateCustomer(entity);

    // Fetch updated customer
    Customer updated = customerUserService.getCustomer(id);
    CustomerUserResponse dto = customerMapper.toResponse(updated);
    return ResponseEntity.ok(ApiResponse.ok(dto));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete customer", description = "Delete a customer account")
  public ResponseEntity<ApiResponse<Void>> deleteCustomer(
      @PathVariable @Positive Long id) {

    customerUserService.deleteCustomer(id);
    return ResponseEntity.ok(ApiResponse.ok("Customer has been deleted"));
  }

  /**
   * PATCH /api/v1/customers/{id}/password
   *
   * Change customer password.
   */
  @PatchMapping("/{id}/password")
  @Operation(summary = "Change password", description = "Change customer password")
  public ResponseEntity<ApiResponse<Void>> changePassword(
      @PathVariable @Positive Long id,
      @RequestBody @Valid PasswordChangeRequest request) {

    // Validate old password
    boolean isValidUser = customerLoginService.isValidCustomerUser(
        request.username().toLowerCase(),
        request.oldPassword());

    if (!isValidUser) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(ApiResponse.error("INVALID_CREDENTIALS", "Current password is incorrect"));
    }

    try {
      // Generate new salt and hash
      byte[] byteSalt = securityService.getSalt();
      byte[] byteDigestPsw = securityService.getSaltedHashSHA512(request.newPassword(), byteSalt);
      String strDigestPsw = securityService.toHex(byteDigestPsw);
      String strSalt = securityService.toHex(byteSalt);

      customerUserService.changeCustomerPassword(strDigestPsw, strSalt, request.username().toLowerCase());

      return ResponseEntity.ok(ApiResponse.ok("Password changed successfully"));
    } catch (NoSuchAlgorithmException ex) {
      log.error("Salt generation error", ex);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("INTERNAL_ERROR", "Failed to process password change"));
    }
  }

  /**
   * Request record for password change.
   */
  public record PasswordChangeRequest(
      @NotBlank(message = "Username is required") String username,
      @NotBlank(message = "Old password is required") String oldPassword,
      @NotBlank(message = "New password is required") String newPassword
  ) {
  }
}
