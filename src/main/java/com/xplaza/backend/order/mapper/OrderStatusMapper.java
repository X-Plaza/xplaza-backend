/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.order.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.xplaza.backend.order.domain.entity.OrderStatus;
import com.xplaza.backend.order.dto.request.OrderStatusRequest;
import com.xplaza.backend.order.dto.response.OrderStatusResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface OrderStatusMapper {
  OrderStatus toEntity(OrderStatusRequest request);

  OrderStatusResponse toResponse(OrderStatus entity);
}
