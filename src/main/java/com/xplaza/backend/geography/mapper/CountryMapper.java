/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.geography.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.xplaza.backend.geography.domain.entity.Country;
import com.xplaza.backend.geography.dto.request.CountryRequest;
import com.xplaza.backend.geography.dto.response.CountryResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CountryMapper {
  Country toEntity(CountryRequest request);

  CountryResponse toResponse(Country entity);
}
