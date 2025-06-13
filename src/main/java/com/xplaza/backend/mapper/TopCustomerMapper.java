/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.xplaza.backend.jpa.dao.TopCustomerDao;
import com.xplaza.backend.service.entity.TopCustomer;

@Mapper(componentModel = "spring")
public interface TopCustomerMapper {
  @Mapping(target = "id", source = "id")
  @Mapping(target = "customerId", source = "customerId")
  @Mapping(target = "customerName", source = "customerName")
  @Mapping(target = "totalOrderAmount", source = "totalOrderAmount")
  @Mapping(target = "shop", source = "shop")
  TopCustomer toEntity(TopCustomerDao dao);
}