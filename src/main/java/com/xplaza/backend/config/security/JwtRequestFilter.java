/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.config.security;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;

import org.jetbrains.annotations.NotNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xplaza.backend.auth.service.AuthUserDetailsService;
import com.xplaza.backend.common.util.ErrorUtils;
import com.xplaza.backend.common.util.JwtUtil;
import com.xplaza.backend.exception.InvalidJwtTokenException;

@Component
public class JwtRequestFilter extends OncePerRequestFilter {

  private final AuthUserDetailsService authUserDetailsService;
  private final JwtUtil jwtUtil;
  private final ErrorUtils errorUtils;

  public JwtRequestFilter(AuthUserDetailsService authUserDetailsService, JwtUtil jwtUtil, ErrorUtils errorUtils) {
    this.authUserDetailsService = authUserDetailsService;
    this.jwtUtil = jwtUtil;
    this.errorUtils = errorUtils;
  }

  @Override
  protected void doFilterInternal(@NotNull HttpServletRequest request,
      @NotNull HttpServletResponse response,
      @NotNull FilterChain filterChain)
      throws ServletException, IOException {
    try {
      // 1. Extract Token
      String jwt = JwtUtil.extractJwtToken(request);

      // 2. Early Exit if No Token
      if (jwt == null || SecurityContextHolder.getContext().getAuthentication() != null) {
        logger.warn("No JWT token found in the request.");
        filterChain.doFilter(request, response);
        return;
      }

      // 3. Validate Token and User
      validateTokenAndUser(request, jwt);

      // 4. Continue Filter Chain
      filterChain.doFilter(request, response);
    } catch (InvalidJwtTokenException e) {
      sendInvalidJwtTokenExceptionResponse(response, e);
    }
  }

  private void sendInvalidJwtTokenExceptionResponse(HttpServletResponse response, InvalidJwtTokenException e)
      throws IOException {
    var errorResponse = errorUtils.buildErrorResponse(e);
    ObjectMapper objectMapper = new ObjectMapper();
    String jsonResponse = objectMapper.writeValueAsString(errorResponse.error());
    response.setHeader("Content-Type", "application/json");
    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    response.getWriter().write(jsonResponse);
  }

  private void validateTokenAndUser(HttpServletRequest request, String jwt) {
    try {
      String username = jwtUtil.extractUsername(jwt);
      UserDetails userDetails = authUserDetailsService.loadUserByUsername(username);
      if (jwtUtil.validateJwtToken(jwt, userDetails))
        setAuthentication(request, jwt, userDetails);
    } catch (MalformedJwtException | ExpiredJwtException | AuthenticationException e) {
      logger.warn(e.getMessage());
      SecurityContextHolder.clearContext();
      throw new InvalidJwtTokenException(e.getMessage());
    }
  }

  private void setAuthentication(HttpServletRequest request, String jwt, UserDetails userDetails) {
    SecurityContextHolder.getContext().setAuthentication(
        createAuthenticationToken(jwt, userDetails, request));
  }

  private UsernamePasswordAuthenticationToken createAuthenticationToken(String jwt, UserDetails userDetails,
      HttpServletRequest request) {
    String role = jwtUtil.extractRole(jwt);
    List<GrantedAuthority> authorities = List.of(new SimpleGrantedAuthority(role));
    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
        userDetails, null, authorities);
    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
    return authToken;
  }
}
