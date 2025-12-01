/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;

import com.xplaza.backend.domain.ProductDiscount;
import com.xplaza.backend.jpa.dao.ProductDiscountDao;

@Mapper(componentModel = "spring")
public interface DiscountListMapper {
  ProductDiscountDao toDao(ProductDiscount entity);

  ProductDiscount toEntityFromDao(ProductDiscountDao dao);
}