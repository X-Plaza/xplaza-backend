/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.xplaza.backend.domain.Brand;
import com.xplaza.backend.http.dto.request.BrandRequest;
import com.xplaza.backend.http.dto.response.BrandResponse;
import com.xplaza.backend.jpa.dao.BrandDao;

@Mapper(componentModel = "spring")
public interface BrandMapper {
  @Mapping(target = "brandId", source = "brandId")
  @Mapping(target = "brandName", source = "brandName")
  @Mapping(target = "brandDescription", source = "brandDescription")
  Brand toEntity(BrandRequest request);

  @Mapping(target = "brandId", source = "brandId")
  @Mapping(target = "brandName", source = "brandName")
  @Mapping(target = "brandDescription", source = "brandDescription")
  BrandResponse toResponse(Brand entity);

  @Mapping(target = "brandId", source = "brandId")
  @Mapping(target = "brandName", source = "brandName")
  @Mapping(target = "brandDescription", source = "brandDescription")
  BrandDao toDao(Brand entity);

  @Mapping(target = "brandId", source = "brandId")
  @Mapping(target = "brandName", source = "brandName")
  @Mapping(target = "brandDescription", source = "brandDescription")
  Brand toEntityFromDao(BrandDao dao);
}