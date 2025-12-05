/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.xplaza.backend.domain.Country;
import com.xplaza.backend.http.dto.request.CountryRequest;
import com.xplaza.backend.http.dto.response.CountryResponse;
import com.xplaza.backend.jpa.dao.CountryDao;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CountryMapper {
  @Mapping(target = "countryId", source = "countryId")
  @Mapping(target = "countryName", source = "countryName")
  Country toEntity(CountryRequest request);

  @Mapping(target = "countryId", source = "countryId")
  @Mapping(target = "countryName", source = "countryName")
  CountryResponse toResponse(Country entity);

  @Mapping(target = "countryId", source = "countryId")
  @Mapping(target = "countryName", source = "countryName")
  CountryDao toDao(Country entity);

  @Mapping(target = "countryId", source = "countryId")
  @Mapping(target = "countryName", source = "countryName")
  Country toEntityFromDao(CountryDao dao);
}