/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.xplaza.backend.domain.ProductVariationType;
import com.xplaza.backend.http.dto.request.ProductVariationTypeRequest;
import com.xplaza.backend.http.dto.response.ProductVariationTypeResponse;
import com.xplaza.backend.jpa.dao.ProductVariationTypeDao;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductVariationTypeMapper {
  @Mapping(target = "productVarTypeId", source = "productVarTypeId")
  @Mapping(target = "varTypeName", source = "varTypeName")
  @Mapping(target = "varTypeDescription", source = "varTypeDescription")
  ProductVariationTypeDao toDao(ProductVariationType entity);

  @Mapping(target = "productVarTypeId", source = "productVarTypeId")
  @Mapping(target = "varTypeName", source = "varTypeName")
  @Mapping(target = "varTypeDescription", source = "varTypeDescription")
  ProductVariationType toEntityFromDao(ProductVariationTypeDao dao);

  @Mapping(target = "productVarTypeId", source = "productVarTypeId")
  @Mapping(target = "varTypeName", source = "varTypeName")
  @Mapping(target = "varTypeDescription", source = "varTypeDescription")
  ProductVariationType toEntity(ProductVariationTypeRequest request);

  @Mapping(target = "productVarTypeId", source = "productVarTypeId")
  @Mapping(target = "varTypeName", source = "varTypeName")
  @Mapping(target = "varTypeDescription", source = "varTypeDescription")
  ProductVariationTypeResponse toResponse(ProductVariationType entity);
}