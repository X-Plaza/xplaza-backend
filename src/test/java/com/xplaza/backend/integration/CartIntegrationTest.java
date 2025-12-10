/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.xplaza.backend.cart.controller.CartController.AddItemRequest;
import com.xplaza.backend.cart.controller.CartController.UpdateQuantityRequest;

public class CartIntegrationTest extends BaseIntegrationTest {

  @Test
  public void testGuestCartLifecycle() throws Exception {
    String sessionId = UUID.randomUUID().toString();

    // 1. Create Guest Cart
    String cartResponse = mockMvc.perform(post("/api/v1/carts/session/" + sessionId))
        .andExpect(status().isOk())
        .andReturn().getResponse().getContentAsString();

    String cartId = objectMapper.readTree(cartResponse).path("id").asText();

    // 2. Add Item
    // We need a valid shopId, but for cart it might not validate existence strictly
    // if we pass it.
    // However, let's try to be realistic.
    Long shopId = 1L; // Assuming ID 1 exists or we don't care for this test if DB is empty?
    // Actually, if FK constraints exist, we MUST create a shop.
    // Let's use the admin token to create a shop first.
    String adminToken = getAdminToken();
    Long createdProductId = createProduct(adminToken);
    // Note: createProduct creates a shop internally, but we don't get the ID back
    // easily.
    // Let's just use a dummy shopId and see if it fails. If it fails, we know we
    // need to be stricter.
    // But wait, createProduct returns productId. We can fetch the product to get
    // the shopId.

    String productDetails = mockMvc.perform(get("/api/v1/products/" + createdProductId)
        .header("Authorization", "Bearer " + adminToken))
        .andReturn().getResponse().getContentAsString();

    shopId = objectMapper.readTree(productDetails).path("data").path("shopId").asLong();

    AddItemRequest addItemRequest = new AddItemRequest(
        createdProductId,
        null, // variantId
        shopId,
        2,
        new BigDecimal("50.00"),
        "Test Product",
        null,
        "SKU-123",
        "http://image.url");

    String itemResponse = mockMvc.perform(post("/api/v1/carts/" + cartId + "/items")
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(addItemRequest)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();

    // The response is CartItem, which has an ID (UUID)
    String itemId = objectMapper.readTree(itemResponse).path("id").asText();

    // 3. Get Cart
    mockMvc.perform(get("/api/v1/carts/" + cartId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isArray())
        .andExpect(jsonPath("$.items[0].productId").value(createdProductId));

    // 4. Update Quantity
    UpdateQuantityRequest updateRequest = new UpdateQuantityRequest(5);
    mockMvc.perform(put("/api/v1/carts/" + cartId + "/items/" + itemId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(updateRequest)))
        .andDo(print())
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.quantity").value(5));

    // 5. Remove Item
    mockMvc.perform(delete("/api/v1/carts/" + cartId + "/items/" + itemId))
        .andExpect(status().isNoContent());

    // Verify removal
    mockMvc.perform(get("/api/v1/carts/" + cartId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.items").isEmpty());
  }
}
