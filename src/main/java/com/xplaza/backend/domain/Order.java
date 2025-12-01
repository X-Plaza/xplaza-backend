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
public class Order {
  private Long invoiceNumber;
  private Double totalPrice;
  private Double discountAmount;
  private Double netTotal;
  private Double grandTotalPrice;
  private String deliveryAddress;
  private Long customerId;
  private Long shopId;
  private Long deliveryCostId;
  private Long paymentTypeId;
  private Long statusId;
  private Long couponId;
  private Date receivedTime;
  private Date dateToDeliver;
  private Long currencyId;
  private String additionalInfo;
  private String remarks;
  private String customerName;
  private String shopName;
  private Time deliveryScheduleStart;
  private Time deliveryScheduleEnd;
  private Double deliveryCost;
  private String couponCode;
  private Double couponAmount;
  private String mobileNo;
  private List<OrderItem> orderItemList;
}
