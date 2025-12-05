/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.order.dto.request;

import java.sql.Time;
import java.util.Date;
import java.util.List;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class OrderRequest {
  @NotNull(message = "Shop ID is required")
  private Long shopId;
  private String shopName;
  @NotNull(message = "Customer ID is required")
  private Long customerId;
  private String customerName;
  private String mobileNo;
  @NotBlank(message = "Delivery address is required")
  private String deliveryAddress;
  private String additionalInfo;
  private String remarks;
  private Time deliveryScheduleStart;
  private Time deliveryScheduleEnd;
  private Date receivedTime;
  private Date dateToDeliver;
  private Long statusId;
  private Long currencyId;
  @NotNull(message = "Order item list is required")
  @Size(min = 1, message = "Order must have at least one item")
  private List<OrderItemRequest> orderItemList;
  // add other fields as needed
}
