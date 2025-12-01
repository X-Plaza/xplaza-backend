/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;

import com.xplaza.backend.domain.City;
import com.xplaza.backend.http.dto.request.CityRequest;
import com.xplaza.backend.http.dto.response.CityResponse;
import com.xplaza.backend.jpa.dao.CityDao;

@Mapper(componentModel = "spring")
public interface CityMapper {
  CityDao toDao(City entity);

  City toEntityFromDao(CityDao dao);

  City toEntity(CityRequest request);

  CityResponse toResponse(City entity);
}