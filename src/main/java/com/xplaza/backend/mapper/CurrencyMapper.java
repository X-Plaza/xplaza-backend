/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.xplaza.backend.domain.Currency;
import com.xplaza.backend.http.dto.request.CurrencyRequest;
import com.xplaza.backend.http.dto.response.CurrencyResponse;
import com.xplaza.backend.jpa.dao.CurrencyDao;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CurrencyMapper {
  @Mapping(target = "currencyId", source = "currencyId")
  @Mapping(target = "currencyName", source = "currencyName")
  @Mapping(target = "currencySign", source = "currencySign")
  CurrencyDao toDao(Currency entity);

  @Mapping(target = "currencyId", source = "currencyId")
  @Mapping(target = "currencyName", source = "currencyName")
  @Mapping(target = "currencySign", source = "currencySign")
  Currency toEntityFromDao(CurrencyDao dao);

  @Mapping(target = "currencyId", source = "currencyId")
  @Mapping(target = "currencyName", source = "currencyName")
  @Mapping(target = "currencySign", source = "currencySign")
  Currency toEntity(CurrencyRequest request);

  @Mapping(target = "currencyId", source = "currencyId")
  @Mapping(target = "currencyName", source = "currencyName")
  @Mapping(target = "currencySign", source = "currencySign")
  CurrencyResponse toResponse(Currency entity);
}