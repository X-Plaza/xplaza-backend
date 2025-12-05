/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.delivery.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.xplaza.backend.delivery.domain.entity.DeliveryCost;
import com.xplaza.backend.delivery.dto.request.DeliveryCostRequest;
import com.xplaza.backend.delivery.dto.response.DeliveryCostResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeliveryCostMapper {
  DeliveryCost toEntity(DeliveryCostRequest request);

  DeliveryCostResponse toResponse(DeliveryCost entity);
}
