/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.xplaza.backend.domain.Coupon;
import com.xplaza.backend.http.dto.request.CouponRequest;
import com.xplaza.backend.http.dto.response.CouponResponse;
import com.xplaza.backend.jpa.dao.CouponDao;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CouponMapper {
  Coupon toEntity(CouponRequest dao);

  CouponResponse toResponse(Coupon coupon);

  CouponDao toDao(Coupon entity);

  Coupon toEntityFromDao(CouponDao dao);
}