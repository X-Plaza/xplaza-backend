/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;

import com.xplaza.backend.domain.Day;
import com.xplaza.backend.http.dto.request.DayRequest;
import com.xplaza.backend.http.dto.response.DayResponse;
import com.xplaza.backend.jpa.dao.DayDao;

@Mapper(componentModel = "spring")
public interface DayMapper {
  Day toEntity(DayRequest request);

  DayResponse toResponse(Day entity);

  DayDao toDao(Day entity);

  Day toEntityFromDao(DayDao dao);
}