/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.xplaza.backend.domain.Category;
import com.xplaza.backend.http.dto.request.CategoryRequest;
import com.xplaza.backend.http.dto.response.CategoryResponse;
import com.xplaza.backend.jpa.dao.CategoryDao;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface CategoryMapper {
  @Mapping(target = "categoryId", source = "categoryId")
  @Mapping(target = "categoryName", source = "categoryName")
  @Mapping(target = "categoryDescription", source = "categoryDescription")
  @Mapping(target = "parentCategory", ignore = true)
  Category toEntity(CategoryRequest request);

  @Mapping(target = "categoryId", source = "categoryId")
  @Mapping(target = "categoryName", source = "categoryName")
  @Mapping(target = "categoryDescription", source = "categoryDescription")
  @Mapping(target = "parentCategoryId", expression = "java(entity.getParentCategory() != null ? entity.getParentCategory().getCategoryId() : null)")
  CategoryResponse toResponse(Category entity);

  @Mapping(target = "categoryId", source = "categoryId")
  @Mapping(target = "categoryName", source = "categoryName")
  @Mapping(target = "categoryDescription", source = "categoryDescription")
  @Mapping(target = "parentCategory", ignore = true)
  CategoryDao toDao(Category entity);

  @Mapping(target = "categoryId", source = "categoryId")
  @Mapping(target = "categoryName", source = "categoryName")
  @Mapping(target = "categoryDescription", source = "categoryDescription")
  @Mapping(target = "parentCategory", ignore = true)
  Category toEntityFromDao(CategoryDao dao);
}