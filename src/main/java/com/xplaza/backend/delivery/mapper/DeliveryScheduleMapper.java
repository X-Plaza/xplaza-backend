/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.delivery.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.xplaza.backend.delivery.domain.entity.DeliverySchedule;
import com.xplaza.backend.delivery.dto.request.DeliveryScheduleRequest;
import com.xplaza.backend.delivery.dto.response.DeliveryScheduleResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeliveryScheduleMapper {
  DeliverySchedule toEntity(DeliveryScheduleRequest request);

  @Mapping(target = "dayId", source = "day.dayId")
  @Mapping(target = "dayName", source = "day.dayName")
  DeliveryScheduleResponse toResponse(DeliverySchedule entity);
}
