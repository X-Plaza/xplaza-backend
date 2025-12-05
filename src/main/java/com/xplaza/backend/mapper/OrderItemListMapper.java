/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.xplaza.backend.domain.OrderItem;
import com.xplaza.backend.jpa.dao.OrderItemDao;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderItemListMapper {
  OrderItemDao toDao(OrderItem entity);

  OrderItem toEntityFromDao(OrderItemDao dao);
}