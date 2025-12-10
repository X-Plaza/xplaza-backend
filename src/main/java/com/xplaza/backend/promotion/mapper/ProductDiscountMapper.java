/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.promotion.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.xplaza.backend.promotion.domain.entity.ProductDiscount;
import com.xplaza.backend.promotion.dto.request.ProductDiscountRequest;
import com.xplaza.backend.promotion.dto.response.ProductDiscountResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductDiscountMapper {
  ProductDiscount toEntity(ProductDiscountRequest request);

  @Mapping(target = "productId", source = "product.productId")
  @Mapping(target = "productName", source = "product.productName")
  @Mapping(target = "discountTypeId", source = "discountType.discountTypeId")
  @Mapping(target = "discountTypeName", source = "discountType.discountTypeName")
  ProductDiscountResponse toResponse(ProductDiscount entity);
}
