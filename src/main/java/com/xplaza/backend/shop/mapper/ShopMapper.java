/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.shop.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.xplaza.backend.shop.domain.entity.Shop;
import com.xplaza.backend.shop.dto.request.ShopRequest;
import com.xplaza.backend.shop.dto.response.ShopResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ShopMapper {
  Shop toEntity(ShopRequest request);

  ShopResponse toResponse(Shop entity);
}
