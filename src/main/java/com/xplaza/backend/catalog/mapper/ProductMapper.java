/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.catalog.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.xplaza.backend.catalog.domain.entity.Product;
import com.xplaza.backend.catalog.domain.entity.ProductImage;
import com.xplaza.backend.catalog.dto.request.ProductRequest;
import com.xplaza.backend.catalog.dto.response.ProductImageResponse;
import com.xplaza.backend.catalog.dto.response.ProductResponse;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {
  @Mapping(target = "productSellingPrice", source = "productPrice")
  @Mapping(target = "shop.shopId", source = "shopId")
  @Mapping(target = "category.categoryId", source = "categoryId")
  @Mapping(target = "brand.brandId", source = "brandId")
  Product toEntity(ProductRequest request);

  @Mapping(target = "productPrice", source = "productSellingPrice")
  @Mapping(target = "shopId", source = "shop.shopId")
  @Mapping(target = "shopName", source = "shop.shopName")
  @Mapping(target = "categoryId", source = "category.categoryId")
  @Mapping(target = "categoryName", source = "category.categoryName")
  @Mapping(target = "brandId", source = "brand.brandId")
  @Mapping(target = "brandName", source = "brand.brandName")
  ProductResponse toResponse(Product entity);

  @Mapping(target = "productImageId", source = "productImagesId")
  @Mapping(target = "productImageUrl", source = "productImagePath")
  @Mapping(target = "productId", source = "product.productId")
  @Mapping(target = "productName", source = "product.productName")
  ProductImageResponse toImageResponse(ProductImage entity);
}
