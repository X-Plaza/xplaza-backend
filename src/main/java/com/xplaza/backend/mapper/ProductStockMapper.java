/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.xplaza.backend.domain.ProductToStock;
import com.xplaza.backend.http.dto.request.ProductStockRequest;
import com.xplaza.backend.http.dto.response.ProductStockResponse;
import com.xplaza.backend.jpa.dao.ProductToStockDao;

@Mapper(componentModel = "spring")
public interface ProductStockMapper {
  @Mapping(target = "remainingUnit", source = "quantity")
  @Mapping(target = "shop.shopId", source = "shopId")
  ProductToStock toEntity(ProductStockRequest request);

  @Mapping(target = "quantity", source = "remainingUnit")
  @Mapping(target = "shopId", source = "shop.shopId")
  ProductStockResponse toResponse(ProductToStock entity);

  @Mapping(target = "remainingUnit", source = "remainingUnit")
  @Mapping(target = "shop", source = "shop")
  ProductToStockDao toDao(ProductToStock entity);

  @Mapping(target = "remainingUnit", source = "remainingUnit")
  @Mapping(target = "shop", source = "shop")
  ProductToStock toEntityFromDao(ProductToStockDao dao);
}