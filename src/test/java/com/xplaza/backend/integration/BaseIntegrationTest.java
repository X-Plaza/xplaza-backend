/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xplaza.backend.auth.dto.request.AdminUserRequest;
import com.xplaza.backend.catalog.dto.request.BrandRequest;
import com.xplaza.backend.catalog.dto.request.CategoryRequest;
import com.xplaza.backend.catalog.dto.request.ProductRequest;
import com.xplaza.backend.inventory.domain.entity.InventoryItem;
import com.xplaza.backend.inventory.domain.entity.Warehouse;
import com.xplaza.backend.inventory.domain.repository.InventoryItemRepository;
import com.xplaza.backend.inventory.domain.repository.WarehouseRepository;
import com.xplaza.backend.payment.service.PaymentGateway;
import com.xplaza.backend.shop.dto.request.ShopRequest;

@SpringBootTest(properties = {
    "stripe.api-key=sk_test_mock",
    "stripe.webhook-secret=whsec_mock",
    "spring.mail.username=mock_user",
    "spring.mail.password=mock_pass"
})
@ActiveProfiles("local")
public abstract class BaseIntegrationTest {

  @Autowired
  protected WebApplicationContext webApplicationContext;

  @Autowired
  protected ObjectMapper objectMapper;

  @MockitoBean
  protected PaymentGateway paymentGateway;

  protected MockMvc mockMvc;

  @Autowired
  protected InventoryItemRepository inventoryItemRepository;

  @Autowired
  protected WarehouseRepository warehouseRepository;

  @Autowired
  protected JdbcTemplate jdbcTemplate;

  @BeforeEach
  public void setup() {
    this.mockMvc = MockMvcBuilders.webAppContextSetup(this.webApplicationContext).build();
    jdbcTemplate.execute("CREATE SEQUENCE IF NOT EXISTS order_number_seq START WITH 1 INCREMENT BY 1");
  }

  protected String getAdminToken() throws Exception {
    String uniqueName = "admin_" + java.util.UUID.randomUUID();
    String email = uniqueName + "@xplaza.com";

    AdminUserRequest registerRequest = new AdminUserRequest();
    registerRequest.setUserName(uniqueName);
    registerRequest.setFullName("Admin User");
    registerRequest.setEmail(email);
    registerRequest.setPassword("SecurePass123!");
    registerRequest.setRoleId(1L);

    String response = mockMvc
        .perform(post("/api/v1/auth/register")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(registerRequest)))
        .andReturn().getResponse().getContentAsString();

    JsonNode root = objectMapper.readTree(response);
    return root.path("data").path("jwtToken").asText();
  }

  protected Long createProduct(String token) throws Exception {
    String authHeader = "Bearer " + token;

    // 1. Create Category
    CategoryRequest categoryRequest = new CategoryRequest();
    categoryRequest.setCategoryName("Cat_" + java.util.UUID.randomUUID());
    categoryRequest.setCategoryDescription("Desc");

    String categoryResponse = mockMvc
        .perform(post("/api/v1/categories")
            .header("Authorization", authHeader)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(categoryRequest)))
        .andReturn().getResponse().getContentAsString();
    Long categoryId = objectMapper.readTree(categoryResponse).path("data").path("categoryId").asLong();

    // 2. Create Brand
    BrandRequest brandRequest = new BrandRequest();
    brandRequest.setBrandName("Brand_" + java.util.UUID.randomUUID());
    brandRequest.setBrandDescription("Desc");

    String brandResponse = mockMvc
        .perform(post("/api/v1/brands")
            .header("Authorization", authHeader)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(brandRequest)))
        .andReturn().getResponse().getContentAsString();
    Long brandId = objectMapper.readTree(brandResponse).path("data").path("brandId").asLong();

    // 3. Create Shop
    ShopRequest shopRequest = new ShopRequest();
    shopRequest.setShopName("Shop_" + java.util.UUID.randomUUID());
    shopRequest.setShopDescription("Desc");
    shopRequest.setShopOwner("Owner");

    String shopResponse = mockMvc
        .perform(post("/api/v1/shops")
            .header("Authorization", authHeader)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(shopRequest)))
        .andReturn().getResponse().getContentAsString();
    Long shopId = objectMapper.readTree(shopResponse).path("data").path("shopId").asLong();

    // 4. Create Product
    ProductRequest productRequest = new ProductRequest();
    productRequest.setProductName("Prod_" + java.util.UUID.randomUUID());
    productRequest.setProductDescription("Desc");
    productRequest.setProductPrice(100.0);
    productRequest.setQuantity(10);
    productRequest.setCategoryId(categoryId);
    productRequest.setBrandId(brandId);
    productRequest.setShopId(shopId);

    String productResponse = mockMvc
        .perform(post("/api/v1/products")
            .header("Authorization", authHeader)
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(productRequest)))
        .andReturn().getResponse().getContentAsString();

    Long productId = objectMapper.readTree(productResponse).path("data").path("productId").asLong();

    // 5. Create Inventory
    // Ensure Warehouse exists
    Warehouse warehouse = warehouseRepository.findByCode("MAIN")
        .orElseGet(() -> warehouseRepository.save(Warehouse.builder()
            .name("Main Warehouse")
            .code("MAIN")
            .city("City")
            .countryCode("US")
            .addressLine1("123 Main St")
            .postalCode("12345")
            .isActive(true)
            .build()));

    InventoryItem inventoryItem = InventoryItem
        .builder()
        .productId(productId)
        .warehouse(warehouse)
        .sku("SKU-" + productId)
        .quantityOnHand(100)
        .quantityReserved(0)
        .status(InventoryItem.InventoryStatus.ACTIVE)
        .build();

    inventoryItemRepository.save(inventoryItem);

    return productId;
  }
}
