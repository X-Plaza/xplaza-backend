/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.xplaza.backend.domain.Revenue;
import com.xplaza.backend.jpa.dao.RevenueDao;

@Mapper(componentModel = "spring")
public interface RevenueMapper {
  @Mapping(target = "totalExpense", source = "totalExpense")
  @Mapping(target = "totalIncome", source = "totalIncome")
  @Mapping(target = "totalRevenue", source = "totalRevenue")
  @Mapping(target = "shop", source = "shop")
  Revenue toEntity(RevenueDao dao);
}