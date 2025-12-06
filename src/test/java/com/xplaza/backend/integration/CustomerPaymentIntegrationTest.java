/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xplaza.backend.customer.dto.CustomerRequest;
import com.xplaza.backend.payment.service.PaymentGateway;

@SpringBootTest(properties = {
    "stripe.api-key=sk_test_mock",
    "stripe.webhook-secret=whsec_mock",
    "spring.mail.username=mock_user",
    "spring.mail.password=mock_pass"
})
// @AutoConfigureMockMvc
@ActiveProfiles("local")
public class CustomerPaymentIntegrationTest {

  @Autowired
  private WebApplicationContext webApplicationContext;

  private MockMvc mockMvc;

  @BeforeEach
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
  }

  @Autowired
  private ObjectMapper objectMapper;

  @MockitoBean
  private PaymentGateway paymentGateway;

  @Test
  public void testCustomerRegistrationAndLogin() throws Exception {
    // 1. Register
    CustomerRequest registerRequest = new CustomerRequest();
    registerRequest.setFirstName("Test");
    registerRequest.setLastName("User");
    registerRequest.setEmail("test.user@example.com");
    registerRequest.setPassword("password123");
    registerRequest.setPhoneNumber("+1234567890");

    mockMvc.perform(post("/api/v1/customer/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.jwtToken").exists());

    // 2. Login
    String loginRequest = "{\"username\": \"test.user@example.com\", \"password\": \"password123\"}";

    mockMvc.perform(post("/api/v1/customer/auth/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(loginRequest))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.success").value(true))
        .andExpect(jsonPath("$.data.jwtToken").exists());
  }
}
