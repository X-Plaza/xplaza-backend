/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import com.xplaza.backend.cart.controller.CartController.AddItemRequest;
import com.xplaza.backend.customer.domain.entity.CustomerAddress;
import com.xplaza.backend.customer.domain.repository.CustomerAddressRepository;
import com.xplaza.backend.customer.domain.repository.CustomerRepository;
import com.xplaza.backend.customer.dto.CustomerRequest;

public class OrderIntegrationTest extends BaseIntegrationTest {

  @Autowired
  private CustomerAddressRepository customerAddressRepository;

  @Autowired
  private CustomerRepository customerRepository;

  @Test
  public void testOrderLifecycle() throws Exception {
    // 1. Register Customer
    String uniqueEmail = "order_user_" + UUID.randomUUID() + "@example.com";
    CustomerRequest registerRequest = new CustomerRequest();
    registerRequest.setFirstName("Order");
    registerRequest.setLastName("User");
    registerRequest.setEmail(uniqueEmail);
    registerRequest.setPassword("Pass123!");
    registerRequest.setPhoneNumber("+1555123456");

    mockMvc.perform(post("/api/v1/customer/auth/register")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(registerRequest)))
        .andExpect(status().isOk());

    Long customerId = customerRepository.findByEmail(uniqueEmail).get().getCustomerId();

    // 2. Create Address
    CustomerAddress address = CustomerAddress.builder()
        .customerId(customerId)
        .addressLine1("123 Test St")
        .city("Test City")
        .state("TS")
        .postalCode("12345")
        .countryCode("US")
        .firstName("Order")
        .lastName("User")
        .build();
    address = customerAddressRepository.save(address);
    Long addressId = address.getAddressId();

    // 3. Create Product (Admin)
    String adminToken = getAdminToken();
    Long productId = createProduct(adminToken);

    // Get Shop ID from product
    String productDetails = mockMvc
        .perform(
            get("/api/v1/products/" + productId)
                .header("Authorization", "Bearer " + adminToken))
        .andReturn().getResponse().getContentAsString();
    Long shopId = objectMapper.readTree(productDetails).path("data").path("shopId").asLong();

    // 4. Create Cart & Add Item
    String cartResponse = mockMvc.perform(post("/api/v1/carts/customer/" + customerId))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    String cartId = objectMapper.readTree(cartResponse).path("id").asText();

    AddItemRequest addItemRequest = new AddItemRequest(
        productId,
        null,
        shopId,
        1,
        new BigDecimal("100.00"),
        "Test Product",
        null,
        "SKU-123",
        "http://image.url");

    mockMvc.perform(post("/api/v1/carts/" + cartId + "/items")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(addItemRequest)))
        .andExpect(status().isCreated());

    // 5. Start Checkout
    String checkoutResponse = mockMvc.perform(post("/api/v1/checkout/start")
        .param("cartId", cartId)
        .param("customerId", customerId.toString()))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();
    String checkoutId = objectMapper.readTree(checkoutResponse).path("checkoutId").asText();

    // 6. Set Shipping Address
    mockMvc.perform(put("/api/v1/checkout/" + checkoutId + "/shipping-address")
        .param("addressId", addressId.toString()))
        .andExpect(status().isOk());

    // 7. Set Shipping Method
    mockMvc.perform(put("/api/v1/checkout/" + checkoutId + "/shipping-method")
        .param("methodId", "1")
        .param("methodName", "Standard Shipping")
        .param("cost", "10.00"))
        .andExpect(status().isOk());

    // 8. Set Payment Method
    mockMvc.perform(put("/api/v1/checkout/" + checkoutId + "/payment-method")
        .param("methodId", "1")
        .param("methodType", "CREDIT_CARD"))
        .andExpect(status().isOk());

    // 9. Complete Checkout
    mockMvc.perform(post("/api/v1/checkout/" + checkoutId + "/complete"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.orderId").exists());
  }
}
