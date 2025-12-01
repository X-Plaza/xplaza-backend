/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;

import com.xplaza.backend.domain.Location;
import com.xplaza.backend.http.dto.request.LocationRequest;
import com.xplaza.backend.http.dto.response.LocationResponse;
import com.xplaza.backend.jpa.dao.LocationDao;

@Mapper(componentModel = "spring")
public interface LocationMapper {
  Location toEntity(LocationRequest request);

  LocationResponse toResponse(Location entity);

  LocationDao toDao(Location entity);

  Location toEntityFromDao(LocationDao dao);
}
