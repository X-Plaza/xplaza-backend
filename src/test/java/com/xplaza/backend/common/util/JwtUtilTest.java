/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.common.util;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.lang.reflect.Field;

import jakarta.servlet.http.HttpServletRequest;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

import com.xplaza.backend.auth.domain.entity.AdminUser;

@ExtendWith(MockitoExtension.class)
@DisplayName("JwtUtil Unit Tests")
class JwtUtilTest {

  private JwtUtil jwtUtil;

  @Mock
  private HttpServletRequest httpServletRequest;

  private UserDetails testUser;

  @BeforeEach
  void setUp() throws Exception {
    jwtUtil = new JwtUtil();

    // Set up fields via reflection for testing
    setField(jwtUtil, "SECRET_STRING", "test-secret-key-for-unit-testing-must-be-at-least-256-bits");
    setField(jwtUtil, "TOKEN_EXPIRATION_MS", 300000L); // 5 minutes
    setField(jwtUtil, "REFRESH_TOKEN_EXPIRATION_MS", 604800000L); // 7 days

    // Initialize the SECRET_KEY
    jwtUtil.init();

    // Create test user
    testUser = AdminUser.builder()
        .id(1L)
        .username("testuser")
        .password("password")
        .role("ADMIN")
        .build();
  }

  private void setField(Object target, String fieldName, Object value) throws Exception {
    Field field = target.getClass().getDeclaredField(fieldName);
    field.setAccessible(true);
    field.set(target, value);
  }

  @Nested
  @DisplayName("Token Generation Tests")
  class TokenGenerationTests {

    @Test
    @DisplayName("Should generate a valid JWT token")
    void shouldGenerateJwtToken() {
      String token = jwtUtil.generateJwtToken(testUser);

      assertNotNull(token);
      assertFalse(token.isEmpty());
      assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    @DisplayName("Should generate a valid refresh token")
    void shouldGenerateRefreshToken() {
      String refreshToken = jwtUtil.generateRefreshToken(testUser);

      assertNotNull(refreshToken);
      assertFalse(refreshToken.isEmpty());
      assertTrue(refreshToken.split("\\.").length == 3);
    }

    @Test
    @DisplayName("JWT token and refresh token should be different")
    void jwtAndRefreshTokenShouldBeDifferent() {
      String jwtToken = jwtUtil.generateJwtToken(testUser);
      String refreshToken = jwtUtil.generateRefreshToken(testUser);

      assertNotEquals(jwtToken, refreshToken);
    }
  }

  @Nested
  @DisplayName("Token Extraction Tests")
  class TokenExtractionTests {

    @Test
    @DisplayName("Should extract username from token")
    void shouldExtractUsername() {
      String token = jwtUtil.generateJwtToken(testUser);

      String username = jwtUtil.extractUsername(token);

      assertEquals("testuser", username);
    }

    @Test
    @DisplayName("Should extract role from token")
    void shouldExtractRole() {
      String token = jwtUtil.generateJwtToken(testUser);

      String role = jwtUtil.extractRole(token);

      assertEquals("ADMIN", role);
    }
  }

  @Nested
  @DisplayName("Token Validation Tests")
  class TokenValidationTests {

    @Test
    @DisplayName("Should validate a valid JWT token")
    void shouldValidateJwtToken() {
      String token = jwtUtil.generateJwtToken(testUser);

      boolean isValid = jwtUtil.validateJwtToken(token, testUser);

      assertTrue(isValid);
    }

    @Test
    @DisplayName("Should validate a valid refresh token")
    void shouldValidateRefreshToken() {
      String refreshToken = jwtUtil.generateRefreshToken(testUser);

      boolean isValid = jwtUtil.validateRefreshToken(refreshToken, testUser);

      assertTrue(isValid);
    }

    @Test
    @DisplayName("JWT validation should fail for refresh token")
    void jwtValidationShouldFailForRefreshToken() {
      String refreshToken = jwtUtil.generateRefreshToken(testUser);

      boolean isValid = jwtUtil.validateJwtToken(refreshToken, testUser);

      assertFalse(isValid);
    }

    @Test
    @DisplayName("Refresh validation should fail for JWT token")
    void refreshValidationShouldFailForJwtToken() {
      String jwtToken = jwtUtil.generateJwtToken(testUser);

      boolean isValid = jwtUtil.validateRefreshToken(jwtToken, testUser);

      assertFalse(isValid);
    }

    @Test
    @DisplayName("Should fail validation for wrong user")
    void shouldFailValidationForWrongUser() {
      String token = jwtUtil.generateJwtToken(testUser);
      UserDetails wrongUser = User.withUsername("wronguser")
          .password("password")
          .authorities("ADMIN")
          .build();

      boolean isValid = jwtUtil.validateJwtToken(token, wrongUser);

      assertFalse(isValid);
    }
  }

  @Nested
  @DisplayName("extractJwtToken Static Method Tests")
  class ExtractJwtTokenStaticMethodTests {

    @Test
    @DisplayName("Should extract token from Authorization header")
    void shouldExtractTokenFromHeader() {
      when(httpServletRequest.getHeader("Authorization")).thenReturn("Bearer test-token-value");

      String token = JwtUtil.extractJwtToken(httpServletRequest);

      assertEquals("test-token-value", token);
    }

    @Test
    @DisplayName("Should return null when no Authorization header")
    void shouldReturnNullWhenNoHeader() {
      when(httpServletRequest.getHeader("Authorization")).thenReturn(null);

      String token = JwtUtil.extractJwtToken(httpServletRequest);

      assertNull(token);
    }

    @Test
    @DisplayName("Should return null when Authorization header does not start with Bearer")
    void shouldReturnNullWhenNotBearerToken() {
      when(httpServletRequest.getHeader("Authorization")).thenReturn("Basic credentials");

      String token = JwtUtil.extractJwtToken(httpServletRequest);

      assertNull(token);
    }
  }
}
