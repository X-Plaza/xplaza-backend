/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.common.util;

import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

import javax.crypto.SecretKey;

import jakarta.annotation.PostConstruct;
import jakarta.servlet.http.HttpServletRequest;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import com.xplaza.backend.auth.domain.entity.AdminUser;
import com.xplaza.backend.customer.domain.entity.Customer;

@Component
public class JwtUtil {
  public static final String ROLE = "role";

  @Value("${jwt.secret}")
  private String SECRET_STRING;

  @Value("${jwt.expiration}")
  private long TOKEN_EXPIRATION_MS;

  @Value("${jwt.refresh-expiration:604800000}") // Default: 7 days in milliseconds
  private long REFRESH_TOKEN_EXPIRATION_MS;

  private SecretKey SECRET_KEY;

  @PostConstruct
  public void init() {
    byte[] SECRET_KEY_BYTES = SECRET_STRING.getBytes(StandardCharsets.UTF_8);
    SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_BYTES);
  }

  private static final String TOKEN_TYPE = "jwtToken";

  public String extractUsername(String token) {
    return extractClaim(token, Claims::getSubject);
  }

  private Date extractExpiration(String token) {
    return extractClaim(token, Claims::getExpiration);
  }

  private <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
    final Claims claims = extractAllClaims(token);
    return claimsResolver.apply(claims);
  }

  private Claims extractAllClaims(String token) {
    return Jwts.parser()
        .verifyWith(SECRET_KEY)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }

  private boolean isTokenValid(String token) {
    return !extractExpiration(token)
        .before(new Date());
  }

  private boolean isValidUser(String token, UserDetails adminUser) {
    final String username = extractUsername(token);
    return username.equals(adminUser.getUsername());
  }

  public String generateJwtToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    if (userDetails instanceof AdminUser) {
      claims.put(ROLE, "ADMIN");
      claims.put("userId", ((AdminUser) userDetails).getId());
    } else if (userDetails instanceof Customer) {
      claims.put(ROLE, "CUSTOMER");
      claims.put("userId", ((Customer) userDetails).getCustomerId());
    }
    claims.put(TOKEN_TYPE, true);
    return createJwtToken(claims, userDetails.getUsername());
  }

  public String generateRefreshToken(UserDetails userDetails) {
    Map<String, Object> claims = new HashMap<>();
    claims.put(TOKEN_TYPE, false);
    return Jwts.builder()
        .claims(claims)
        .subject(userDetails.getUsername())
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRATION_MS))
        .signWith(SECRET_KEY)
        .compact();
  }

  private String createJwtToken(Map<String, Object> claims, String subject) {
    return Jwts.builder()
        .claims(claims)
        .subject(subject)
        .issuedAt(new Date(System.currentTimeMillis()))
        .expiration(new Date(System.currentTimeMillis() + TOKEN_EXPIRATION_MS))
        .signWith(SECRET_KEY)
        .compact();
  }

  public boolean validateJwtToken(String token, UserDetails adminUser) {
    return isValidUser(token, adminUser) && isTokenValid(token) && isValidTokenType(token);
  }

  public boolean validateRefreshToken(String token, UserDetails adminUser) {
    return isValidUser(token, adminUser) && isTokenValid(token) && !isValidTokenType(token);
  }

  private boolean isValidTokenType(String token) {
    Claims claims = extractAllClaims(token);
    return claims.get(TOKEN_TYPE, Boolean.class);
  }

  public String extractRole(String token) {
    Claims claims = extractAllClaims(token);
    return claims.get(ROLE, String.class);
  }

  public static String extractJwtToken(HttpServletRequest request) {
    final String authorizationHeader = request.getHeader("Authorization");
    if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
      return authorizationHeader.substring(7);
    }
    return null;
  }
}