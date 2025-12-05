/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.xplaza.backend.domain.DeliveryCost;
import com.xplaza.backend.http.dto.request.DeliveryCostRequest;
import com.xplaza.backend.http.dto.response.DeliveryCostResponse;
import com.xplaza.backend.jpa.dao.DeliveryCostDao;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeliveryCostMapper {
  DeliveryCost toEntity(DeliveryCostRequest request);

  DeliveryCostResponse toResponse(DeliveryCost entity);

  DeliveryCostDao toDao(DeliveryCost entity);

  DeliveryCost toEntityFromDao(DeliveryCostDao dao);
}