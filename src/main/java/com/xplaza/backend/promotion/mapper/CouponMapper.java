/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.promotion.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.xplaza.backend.promotion.domain.entity.Coupon;
import com.xplaza.backend.promotion.dto.request.CouponRequest;
import com.xplaza.backend.promotion.dto.response.CouponResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CouponMapper {
  Coupon toEntity(CouponRequest request);

  @Mapping(target = "discountTypeId", source = "discountType.discountTypeId")
  CouponResponse toResponse(Coupon entity);
}
