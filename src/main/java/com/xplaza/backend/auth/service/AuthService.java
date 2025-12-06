/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.auth.service;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.auth.domain.entity.AdminUser;
import com.xplaza.backend.auth.dto.request.AdminUserRequest;
import com.xplaza.backend.auth.dto.request.AuthenticationRequest;
import com.xplaza.backend.auth.dto.response.AuthenticationResponse;
import com.xplaza.backend.auth.repository.AdminUserRepository;
import com.xplaza.backend.common.util.JwtUtil;
import com.xplaza.backend.exception.ResourceAlreadyExistsException;

@Service
@RequiredArgsConstructor
public class AuthService {

  private final AdminUserRepository adminUserRepository;
  private final PasswordEncoder passwordEncoder;
  private final JwtUtil jwtUtil;

  private static final org.slf4j.Logger log = org.slf4j.LoggerFactory.getLogger(AuthService.class);

  @Transactional
  public AuthenticationResponse login(AuthenticationRequest request) {
    AdminUser user = adminUserRepository.findByUsername(request.getUsername())
        .orElseThrow(() -> new BadCredentialsException("Invalid username or password"));

    if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
      log.error("Authentication failed for user: {}", user.getUsername());
      throw new BadCredentialsException("Invalid username or password");
    }

    // Update last login time
    user.setLastLoginAt(LocalDateTime.now());
    adminUserRepository.save(user);

    String jwtToken = jwtUtil.generateJwtToken(user);
    String refreshToken = jwtUtil.generateRefreshToken(user);

    return new AuthenticationResponse(jwtToken, refreshToken);
  }

  @Transactional
  public AuthenticationResponse register(AdminUserRequest request) {
    if (adminUserRepository.existsByUsername(request.getUserName())) {
      throw new ResourceAlreadyExistsException("Username already exists: " + request.getUserName());
    }

    if (adminUserRepository.existsByEmail(request.getEmail())) {
      throw new ResourceAlreadyExistsException("Email already exists: " + request.getEmail());
    }

    AdminUser user = AdminUser.builder()
        .username(request.getUserName())
        .email(request.getEmail())
        .password(passwordEncoder.encode(request.getPassword()))
        .role("ADMIN")
        .enabled(true)
        .build();

    adminUserRepository.save(user);

    String jwtToken = jwtUtil.generateJwtToken(user);
    String refreshToken = jwtUtil.generateRefreshToken(user);

    return new AuthenticationResponse(jwtToken, refreshToken);
  }

  @Transactional
  public AuthenticationResponse refreshToken(String refreshToken) {
    String username = jwtUtil.extractUsername(refreshToken);
    AdminUser user = adminUserRepository.findByUsername(username)
        .orElseThrow(() -> new BadCredentialsException("Invalid refresh token"));

    if (!jwtUtil.validateRefreshToken(refreshToken, user)) {
      throw new BadCredentialsException("Invalid refresh token");
    }

    String newJwtToken = jwtUtil.generateJwtToken(user);
    String newRefreshToken = jwtUtil.generateRefreshToken(user);

    return new AuthenticationResponse(newJwtToken, newRefreshToken);
  }
}
