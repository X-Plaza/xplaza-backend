/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.geography.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.xplaza.backend.geography.domain.entity.Location;
import com.xplaza.backend.geography.dto.request.LocationRequest;
import com.xplaza.backend.geography.dto.response.LocationResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface LocationMapper {
  Location toEntity(LocationRequest request);

  @Mapping(target = "cityId", source = "city.cityId")
  @Mapping(target = "cityName", source = "city.cityName")
  LocationResponse toResponse(Location entity);
}
