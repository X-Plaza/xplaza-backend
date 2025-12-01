/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.xplaza.backend.domain.ProductToStock;
import com.xplaza.backend.jpa.dao.ProductToStockDao;

@Mapper(componentModel = "spring")
public interface ProductToStockMapper {
  @Mapping(target = "id", source = "id")
  @Mapping(target = "productId", source = "productId")
  @Mapping(target = "productName", source = "productName")
  @Mapping(target = "remainingUnit", source = "remainingUnit")
  @Mapping(target = "shop", source = "shop")
  ProductToStock toEntity(ProductToStockDao dao);
}