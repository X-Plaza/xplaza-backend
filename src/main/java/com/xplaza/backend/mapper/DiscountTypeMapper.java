/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.xplaza.backend.domain.DiscountType;
import com.xplaza.backend.http.dto.request.DiscountTypeRequest;
import com.xplaza.backend.http.dto.response.DiscountTypeResponse;
import com.xplaza.backend.jpa.dao.DiscountTypeDao;

@Mapper(componentModel = "spring")
public interface DiscountTypeMapper {
  @Mapping(target = "discountTypeId", source = "discountTypeId")
  @Mapping(target = "discountTypeName", source = "discountTypeName")
  DiscountTypeDao toDao(DiscountType entity);

  @Mapping(target = "discountTypeId", source = "discountTypeId")
  @Mapping(target = "discountTypeName", source = "discountTypeName")
  DiscountType toEntityFromDao(DiscountTypeDao dao);

  @Mapping(target = "discountTypeId", source = "discountTypeId")
  @Mapping(target = "discountTypeName", source = "discountTypeName")
  DiscountType toEntity(DiscountTypeRequest request);

  @Mapping(target = "discountTypeId", source = "discountTypeId")
  @Mapping(target = "discountTypeName", source = "discountTypeName")
  @Mapping(target = "discountTypeDescription", source = "discountTypeDescription")
  DiscountTypeResponse toResponse(DiscountType entity);
}