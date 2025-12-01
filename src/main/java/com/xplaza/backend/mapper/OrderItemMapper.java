/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;

import com.xplaza.backend.domain.OrderItem;
import com.xplaza.backend.http.dto.request.OrderItemRequest;
import com.xplaza.backend.http.dto.response.OrderItemResponse;
import com.xplaza.backend.jpa.dao.OrderItemDao;

@Mapper(componentModel = "spring")
public interface OrderItemMapper {
  OrderItem toEntity(OrderItemRequest request);

  OrderItemResponse toResponse(OrderItem entity);

  OrderItemDao toDao(OrderItem entity);

  OrderItem toEntityFromDao(OrderItemDao dao);
}