/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.geography.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.xplaza.backend.geography.domain.entity.City;
import com.xplaza.backend.geography.dto.request.CityRequest;
import com.xplaza.backend.geography.dto.response.CityResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CityMapper {
  City toEntity(CityRequest request);

  @Mapping(target = "stateId", source = "state.stateId")
  @Mapping(target = "stateName", source = "state.stateName")
  CityResponse toResponse(City entity);
}
