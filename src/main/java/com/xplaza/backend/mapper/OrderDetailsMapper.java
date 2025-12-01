/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.xplaza.backend.domain.OrderDetails;
import com.xplaza.backend.http.dto.request.OrderDetailsRequest;
import com.xplaza.backend.http.dto.response.OrderDetailsResponse;
import com.xplaza.backend.jpa.dao.OrderDetailsDao;

@Mapper(componentModel = "spring")
public interface OrderDetailsMapper {
  OrderDetails toEntity(OrderDetailsRequest request);

  OrderDetailsResponse toResponse(OrderDetails entity);

  OrderDetailsDao toDao(OrderDetails entity);

  @Mapping(target = "id", source = "id")
  @Mapping(target = "orderId", source = "orderId")
  @Mapping(target = "totalPrice", source = "totalPrice")
  @Mapping(target = "discountAmount", source = "discountAmount")
  @Mapping(target = "netTotal", source = "netTotal")
  @Mapping(target = "grandTotalPrice", source = "grandTotalPrice")
  @Mapping(target = "deliveryAddress", source = "deliveryAddress")
  @Mapping(target = "customerId", source = "customerId")
  @Mapping(target = "shopId", source = "shopId")
  @Mapping(target = "paymentTypeId", source = "paymentTypeId")
  @Mapping(target = "statusId", source = "statusId")
  @Mapping(target = "couponId", source = "couponId")
  @Mapping(target = "receivedTime", source = "receivedTime")
  @Mapping(target = "dateToDeliver", source = "dateToDeliver")
  @Mapping(target = "currencyId", source = "currencyId")
  @Mapping(target = "additionalInfo", source = "additionalInfo")
  @Mapping(target = "remarks", source = "remarks")
  @Mapping(target = "customerName", source = "customerName")
  @Mapping(target = "mobileNo", source = "mobileNo")
  @Mapping(target = "shopName", source = "shopName")
  @Mapping(target = "statusName", source = "statusName")
  @Mapping(target = "allottedTime", source = "allottedTime")
  @Mapping(target = "deliveryCost", source = "deliveryCost")
  @Mapping(target = "paymentTypeName", source = "paymentTypeName")
  @Mapping(target = "deliveryId", source = "deliveryId")
  @Mapping(target = "personName", source = "personName")
  @Mapping(target = "contactNo", source = "contactNo")
  @Mapping(target = "couponCode", source = "couponCode")
  @Mapping(target = "couponAmount", source = "couponAmount")
  @Mapping(target = "orderItemName", source = "orderItemName")
  @Mapping(target = "orderItemCategory", source = "orderItemCategory")
  @Mapping(target = "orderItemQuantity", source = "orderItemQuantity")
  @Mapping(target = "orderItemQuantityType", source = "orderItemQuantityType")
  @Mapping(target = "orderItemUnitPrice", source = "orderItemUnitPrice")
  @Mapping(target = "orderItemTotalPrice", source = "orderItemTotalPrice")
  @Mapping(target = "orderItemImage", source = "orderItemImage")
  @Mapping(target = "orderItemId", source = "orderItemId")
  @Mapping(target = "currencyName", source = "currencyName")
  @Mapping(target = "currencySign", source = "currencySign")
  OrderDetails toEntity(OrderDetailsDao dao);
}