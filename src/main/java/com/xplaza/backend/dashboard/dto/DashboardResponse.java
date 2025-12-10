/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.dashboard.dto;

import java.util.List;

import lombok.Data;

import com.xplaza.backend.catalog.dto.response.ProductToStock;

@Data
public class DashboardResponse {
  private Long shopId;
  private Revenue revenue;
  private List<TopCustomer> topCustomers;
  private List<TopProduct> topProducts;
  private List<ProductToStock> productToStocks;
  // add other fields as needed
}
