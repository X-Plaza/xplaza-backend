/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.auth.controller;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.auth.dto.request.AdminUserRequest;
import com.xplaza.backend.auth.dto.request.AuthenticationRequest;
import com.xplaza.backend.auth.dto.response.AuthenticationResponse;
import com.xplaza.backend.auth.service.AuthService;
import com.xplaza.backend.common.util.ApiResponse;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication endpoints for login, register, and token refresh")
public class AuthController {

  private final AuthService authService;

  @PostMapping("/login")
  @Operation(summary = "Login", description = "Authenticate user and return JWT tokens")
  public ResponseEntity<ApiResponse<AuthenticationResponse>> login(
      @RequestBody @Valid AuthenticationRequest request) {

    AuthenticationResponse response = authService.login(request);
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  @PostMapping("/register")
  @Operation(summary = "Register", description = "Register a new admin user")
  public ResponseEntity<ApiResponse<AuthenticationResponse>> register(
      @RequestBody @Valid AdminUserRequest request) {

    AuthenticationResponse response = authService.register(request);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(response));
  }

  @PostMapping("/refresh")
  @Operation(summary = "Refresh Token", description = "Get new access token using refresh token")
  public ResponseEntity<ApiResponse<AuthenticationResponse>> refreshToken(
      @RequestHeader("X-Refresh-Token") String refreshToken) {

    AuthenticationResponse response = authService.refreshToken(refreshToken);
    return ResponseEntity.ok(ApiResponse.ok(response));
  }
}
