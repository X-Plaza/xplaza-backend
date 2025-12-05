/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.xplaza.backend.domain.State;
import com.xplaza.backend.http.dto.request.StateRequest;
import com.xplaza.backend.http.dto.response.StateResponse;
import com.xplaza.backend.jpa.dao.StateDao;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface StateMapper {
  @Mapping(target = "stateId", ignore = true)
  State toEntity(StateRequest request);

  StateResponse toResponse(State entity);

  @Mapping(target = "stateId", ignore = true)
  StateDao toDao(State entity);

  State toEntityFromDao(StateDao dao);
}