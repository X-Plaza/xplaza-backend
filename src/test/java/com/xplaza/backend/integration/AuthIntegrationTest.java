/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.xplaza.backend.auth.dto.request.AdminUserRequest;
import com.xplaza.backend.auth.dto.request.AuthenticationRequest;
import com.xplaza.backend.customer.dto.CustomerRequest;

public class AuthIntegrationTest extends BaseIntegrationTest {

  @Test
  public void testAdminRegistrationAndLogin() throws Exception {
    String uniqueEmail = "admin_" + UUID.randomUUID() + "@xplaza.com";
    String uniqueUsername = "admin_" + UUID.randomUUID();

    // 1. Register Admin
    AdminUserRequest registerRequest = new AdminUserRequest();
    registerRequest.setUserName(uniqueUsername);
    registerRequest.setFullName("Admin User");
    registerRequest.setEmail(uniqueEmail);
    registerRequest.setPassword("SecurePass123!");
    registerRequest.setRoleId(1L);

    mockMvc.perform(post("/api/v1/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isCreated())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.jwtToken").exists());

    // 2. Login Admin
    AuthenticationRequest loginRequest = new AuthenticationRequest();
    loginRequest.setUsername(uniqueUsername);
    loginRequest.setPassword("SecurePass123!");

    mockMvc.perform(post("/api/v1/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.jwtToken").exists());
  }

  @Test
  public void testAdminLoginFailure() throws Exception {
    AuthenticationRequest loginRequest = new AuthenticationRequest();
    loginRequest.setUsername("nonexistent@xplaza.com");
    loginRequest.setPassword("wrongpass");

    mockMvc.perform(post("/api/v1/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isUnauthorized()); // Assuming 401 for bad creds
  }

  @Test
  public void testCustomerRegistrationAndLogin() throws Exception {
    String uniqueEmail = "customer_" + UUID.randomUUID() + "@example.com";

    // 1. Register Customer
    CustomerRequest registerRequest = new CustomerRequest();
    registerRequest.setFirstName("John");
    registerRequest.setLastName("Doe");
    registerRequest.setEmail(uniqueEmail);
    registerRequest.setPassword("CustomerPass123!");
    registerRequest.setPhoneNumber("+1555000" + (int) (Math.random() * 1000));

    mockMvc.perform(post("/api/v1/customer/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.jwtToken").exists());

    // 2. Login Customer
    AuthenticationRequest loginRequest = new AuthenticationRequest();
    loginRequest.setUsername(uniqueEmail);
    loginRequest.setPassword("CustomerPass123!");

    mockMvc.perform(post("/api/v1/customer/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(loginRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.jwtToken").exists());
  }

  @Test
  public void testCustomerRegistrationDuplicateEmail() throws Exception {
    CustomerRequest request = new CustomerRequest();
    request.setFirstName("Duplicate");
    request.setLastName("User");
    request.setEmail("duplicate_" + UUID.randomUUID() + "@example.com");
    request.setPassword("Pass123!");
    request.setPhoneNumber("1234567890");

    // First registration
    mockMvc.perform(post("/api/v1/customer/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isOk());

    // Second registration (Duplicate)
    mockMvc.perform(post("/api/v1/customer/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(request)))
        .andExpect(status().isConflict());
  }
}
