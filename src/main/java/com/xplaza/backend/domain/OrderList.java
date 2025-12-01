/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.domain;

import java.text.SimpleDateFormat;
import java.util.Currency;
import java.util.Date;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderList {
  private Long orderId;
  private String invoiceNumber;
  private Double totalPrice;
  private Double discountPrice;
  private Double netTotal;
  private Double grandTotalPrice;
  private String deliveryAddress;
  private String customerId;
  private String customerName;
  private String mobileNo;
  private String allottedTime;
  private Date receivedTime;
  private Date dateToDelivery;
  private Shop shop;
  private StatusCatalogue status;
  private Currency currency;

  public String getReceivedTime() {
    if (receivedTime != null)
      return new SimpleDateFormat("dd MMM yyyy HH:mm").format(receivedTime);
    return null;
  }

  public String getDateToDelivery() {
    if (dateToDelivery != null)
      return new SimpleDateFormat("dd MMM yyyy").format(dateToDelivery);
    return null;
  }
}
