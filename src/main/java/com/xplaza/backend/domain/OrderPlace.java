/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.domain;

import java.sql.Time;
import java.util.Date;
import java.util.List;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderPlace {
  private Long invoiceNumber;
  private Long shopId;
  private String shopName;
  private Long customerId;
  private String customerName;
  private String mobileNo;
  private String deliveryAddress;
  private String additionalInfo;
  private String remarks;
  private Time deliveryScheduleStart;
  private Time deliveryScheduleEnd;
  private Date receivedTime;
  private Date dateToDeliver;
  private Long statusId;
  private Long currencyId;
  private Double totalPrice;
  private Double netTotal;
  private Double discountAmount;
  private Long couponId;
  private String couponCode;
  private Double couponAmount;
  private Long deliveryCostId;
  private Double deliveryCost;
  private Double grandTotalPrice;
  private Long paymentTypeId;
  private List<OrderItem> orderItemList;
}