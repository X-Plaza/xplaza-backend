/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.catalog.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.xplaza.backend.catalog.domain.entity.ProductVariationType;
import com.xplaza.backend.catalog.dto.request.ProductVariationTypeRequest;
import com.xplaza.backend.catalog.dto.response.ProductVariationTypeResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductVariationTypeMapper {
  ProductVariationType toEntity(ProductVariationTypeRequest request);

  ProductVariationTypeResponse toResponse(ProductVariationType entity);
}
