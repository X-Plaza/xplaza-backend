/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.delivery.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.xplaza.backend.delivery.domain.entity.Day;
import com.xplaza.backend.delivery.dto.request.DayRequest;
import com.xplaza.backend.delivery.dto.response.DayResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DayMapper {
  Day toEntity(DayRequest request);

  DayResponse toResponse(Day entity);
}
