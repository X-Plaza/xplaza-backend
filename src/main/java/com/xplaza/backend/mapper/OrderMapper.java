/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;

import com.xplaza.backend.domain.Order;
import com.xplaza.backend.http.dto.request.OrderRequest;
import com.xplaza.backend.http.dto.response.OrderResponse;
import com.xplaza.backend.jpa.dao.OrderDao;

@Mapper(componentModel = "spring")
public interface OrderMapper {
  Order toEntity(OrderRequest request);

  OrderResponse toResponse(Order entity);

  OrderDao toDao(Order entity);

  Order toEntityFromDao(OrderDao dao);
}