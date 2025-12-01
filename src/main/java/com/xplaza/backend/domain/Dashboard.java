/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.domain;

import java.util.List;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Dashboard {
  private Shop shop;
  private Revenue revenue;
  private List<TopCustomer> topCustomers;
  private List<TopProduct> topProducts;
  private List<ProductToStock> productToStocks;
}
