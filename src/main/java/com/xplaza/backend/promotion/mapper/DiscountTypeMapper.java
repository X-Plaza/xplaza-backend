/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.promotion.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.xplaza.backend.promotion.domain.entity.DiscountType;
import com.xplaza.backend.promotion.dto.request.DiscountTypeRequest;
import com.xplaza.backend.promotion.dto.response.DiscountTypeResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface DiscountTypeMapper {
  DiscountType toEntity(DiscountTypeRequest request);

  DiscountTypeResponse toResponse(DiscountType entity);
}
