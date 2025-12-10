/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.geography.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.xplaza.backend.geography.domain.entity.State;
import com.xplaza.backend.geography.dto.request.StateRequest;
import com.xplaza.backend.geography.dto.response.StateResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StateMapper {
  State toEntity(StateRequest request);

  @Mapping(target = "countryId", source = "country.countryId")
  @Mapping(target = "countryName", source = "country.countryName")
  StateResponse toResponse(State entity);
}
