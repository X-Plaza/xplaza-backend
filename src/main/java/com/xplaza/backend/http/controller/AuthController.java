/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.common.util.JwtUtil;
import com.xplaza.backend.http.dto.request.AuthenticationRequest;
import com.xplaza.backend.http.dto.response.AuthenticationResponse;
import com.xplaza.backend.service.AdminUserLoginService;
import com.xplaza.backend.service.AuthUserDetailsService;

@RestController
@RequestMapping("/api/v1/authenticate")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "APIs for user authentication and authorization")
public class AuthController extends BaseController {

  private final JwtUtil jwtTokenUtil;
  private final AuthUserDetailsService authUserDetailsService;
  private final AdminUserLoginService adminUserLoginService;

  @PostMapping
  @Operation(summary = "User authentication", description = "Authenticates a user with username and password and returns JWT tokens", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Authentication credentials", required = true, content = @Content(schema = @Schema(implementation = AuthenticationRequest.class))), responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Authentication successful", content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "401", description = "Invalid credentials"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<AuthenticationResponse> createAuthenticationToken(
      @Valid @RequestBody AuthenticationRequest authenticationRequest) {
    try {
      String username = authenticationRequest.getUsername();
      String password = authenticationRequest.getPassword();

      // Verify credentials before generating tokens
      if (!adminUserLoginService.isValidAdminUser(username, password)) {
        log.warn("Authentication failed for user: {}", username);
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
      }

      UserDetails userDetails = authUserDetailsService.loadUserByUsername(username);
      String jwtToken = jwtTokenUtil.generateJwtToken(userDetails);
      String refreshToken = jwtTokenUtil.generateRefreshToken(userDetails);
      return ResponseEntity.ok(new AuthenticationResponse(jwtToken, refreshToken));
    } catch (Exception e) {
      log.error("Authentication error for user: {}", authenticationRequest.getUsername(), e);
      return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }
  }
}
