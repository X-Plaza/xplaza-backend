/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.xplaza.backend.domain.ProductImage;
import com.xplaza.backend.http.dto.request.ProductImageRequest;
import com.xplaza.backend.http.dto.response.ProductImageResponse;
import com.xplaza.backend.jpa.dao.ProductImageDao;

@Mapper(componentModel = "spring")
public interface ProductImageMapper {
  @Mapping(target = "productImageName", source = "name")
  @Mapping(target = "productImagePath", source = "path")
  @Mapping(target = "product.productId", source = "productId")
  ProductImage toEntity(ProductImageRequest request);

  @Mapping(target = "productImageId", source = "productImageId")
  @Mapping(target = "productImageName", source = "productImageName")
  @Mapping(target = "productImageUrl", source = "productImagePath")
  @Mapping(target = "productId", source = "product.productId")
  @Mapping(target = "productName", source = "product.productName")
  ProductImageResponse toResponse(ProductImage entity);

  @Mapping(target = "productImagesId", source = "productImageId")
  @Mapping(target = "productImageName", source = "productImageName")
  @Mapping(target = "productImagePath", source = "productImagePath")
  @Mapping(target = "product.productId", source = "product.productId")
  ProductImageDao toDao(ProductImage entity);

  @Mapping(target = "productImageId", source = "productImagesId")
  @Mapping(target = "productImageName", source = "productImageName")
  @Mapping(target = "productImagePath", source = "productImagePath")
  @Mapping(target = "product.productId", source = "product.productId")
  ProductImage toEntityFromDao(ProductImageDao dao);
}