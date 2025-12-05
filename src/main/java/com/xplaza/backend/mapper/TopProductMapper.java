/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.xplaza.backend.domain.TopProduct;
import com.xplaza.backend.jpa.dao.TopProductDao;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface TopProductMapper {
  @Mapping(target = "id", source = "id")
  @Mapping(target = "productId", source = "productId")
  @Mapping(target = "productName", source = "productName")
  @Mapping(target = "monthlySoldUnit", source = "monthlySoldUnit")
  @Mapping(target = "shop", source = "shop")
  TopProduct toEntity(TopProductDao dao);
}