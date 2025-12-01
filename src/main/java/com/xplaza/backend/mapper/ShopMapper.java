/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;

import com.xplaza.backend.domain.Shop;
import com.xplaza.backend.http.dto.request.ShopRequest;
import com.xplaza.backend.http.dto.response.ShopResponse;
import com.xplaza.backend.jpa.dao.ShopDao;

@Mapper(componentModel = "spring")
public interface ShopMapper {
  ShopDao toDao(Shop entity);

  Shop toEntityFromDao(ShopDao dao);

  Shop toEntity(ShopRequest request);

  ShopResponse toResponse(Shop entity);
}