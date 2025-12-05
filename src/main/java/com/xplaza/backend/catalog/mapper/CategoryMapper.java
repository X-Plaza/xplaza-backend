/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.catalog.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.xplaza.backend.catalog.domain.entity.Category;
import com.xplaza.backend.catalog.dto.request.CategoryRequest;
import com.xplaza.backend.catalog.dto.response.CategoryResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {
  Category toEntity(CategoryRequest request);

  CategoryResponse toResponse(Category entity);
}
