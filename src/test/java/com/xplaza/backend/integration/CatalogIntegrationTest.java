/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.xplaza.backend.catalog.dto.request.BrandRequest;
import com.xplaza.backend.catalog.dto.request.CategoryRequest;
import com.xplaza.backend.catalog.dto.request.ProductRequest;
import com.xplaza.backend.shop.dto.request.ShopRequest;

public class CatalogIntegrationTest extends BaseIntegrationTest {

  @Test
  public void testProductLifecycle() throws Exception {
    String token = getAdminToken();
    String authHeader = "Bearer " + token;

    // 1. Create Category
    CategoryRequest categoryRequest = new CategoryRequest();
    categoryRequest.setCategoryName("Electronics");
    categoryRequest.setCategoryDescription("Gadgets and devices");

    String categoryResponse = mockMvc.perform(post("/api/v1/categories")
        .header("Authorization", authHeader)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(categoryRequest)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();

    Long categoryId = objectMapper.readTree(categoryResponse).path("data").path("categoryId").asLong();

    // 2. Create Brand
    BrandRequest brandRequest = new BrandRequest();
    brandRequest.setBrandName("TechBrand");
    brandRequest.setBrandDescription("Top tier tech");

    String brandResponse = mockMvc.perform(post("/api/v1/brands")
        .header("Authorization", authHeader)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(brandRequest)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();

    Long brandId = objectMapper.readTree(brandResponse).path("data").path("brandId").asLong();

    // 3. Create Shop
    ShopRequest shopRequest = new ShopRequest();
    shopRequest.setShopName("TechStore");
    shopRequest.setShopDescription("Best tech store");
    shopRequest.setShopOwner("Owner Name");

    String shopResponse = mockMvc.perform(post("/api/v1/shops")
        .header("Authorization", authHeader)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(shopRequest)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();

    Long shopId = objectMapper.readTree(shopResponse).path("data").path("shopId").asLong();

    // 4. Create Product
    ProductRequest productRequest = new ProductRequest();
    productRequest.setProductName("Smartphone X");
    productRequest.setProductDescription("Latest model");
    productRequest.setProductPrice(999.99);
    productRequest.setQuantity(100);
    productRequest.setCategoryId(categoryId);
    productRequest.setBrandId(brandId);
    productRequest.setShopId(shopId);

    String productResponse = mockMvc.perform(post("/api/v1/products")
        .header("Authorization", authHeader)
        .contentType(MediaType.APPLICATION_JSON)
        .content(objectMapper.writeValueAsString(productRequest)))
        .andExpect(status().isCreated())
        .andReturn().getResponse().getContentAsString();

    Long productId = objectMapper.readTree(productResponse).path("data").path("productId").asLong();

    // 5. Get Product
    mockMvc.perform(get("/api/v1/products/" + productId)
        .header("Authorization", authHeader))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.data.productName").value("Smartphone X"));
  }
}
