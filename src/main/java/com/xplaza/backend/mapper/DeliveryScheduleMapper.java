/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.xplaza.backend.domain.Day;
import com.xplaza.backend.domain.DeliverySchedule;
import com.xplaza.backend.http.dto.request.DeliveryScheduleRequest;
import com.xplaza.backend.http.dto.response.DeliveryScheduleResponse;
import com.xplaza.backend.jpa.dao.DeliveryScheduleDao;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DeliveryScheduleMapper {
  DeliverySchedule toEntity(DeliveryScheduleRequest request);

  DeliveryScheduleResponse toResponse(DeliverySchedule entity);

  DeliveryScheduleDao toDao(DeliverySchedule entity);

  DeliverySchedule toEntityFromDao(DeliveryScheduleDao dao);

  // Custom mapping for Day to String
  default String map(Day value) {
    return value != null ? value.getDayName() : null;
  }
}