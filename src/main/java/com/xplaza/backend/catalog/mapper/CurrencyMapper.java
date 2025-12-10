/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.catalog.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.xplaza.backend.catalog.domain.entity.Currency;
import com.xplaza.backend.catalog.dto.request.CurrencyRequest;
import com.xplaza.backend.catalog.dto.response.CurrencyResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CurrencyMapper {
  Currency toEntity(CurrencyRequest request);

  CurrencyResponse toResponse(Currency entity);
}
