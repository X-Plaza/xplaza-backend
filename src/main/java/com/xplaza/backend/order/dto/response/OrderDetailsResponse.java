/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.order.dto.response;

import java.util.UUID;

import lombok.Data;

@Data
public class OrderDetailsResponse {
  private UUID orderId;
  private String invoiceNumber;
  private Double totalPrice;
  private Double discountAmount;
  private Double netTotal;
  private Double grandTotalPrice;
  private String deliveryAddress;
  private String customerId;
  private String customerName;
  private String mobileNo;
  // add other fields as needed
}
