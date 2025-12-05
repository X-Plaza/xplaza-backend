/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.xplaza.backend.domain.Product;
import com.xplaza.backend.http.dto.request.ProductRequest;
import com.xplaza.backend.http.dto.response.ProductResponse;
import com.xplaza.backend.jpa.dao.ProductDao;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {
  @Mapping(target = "productId", source = "productId")
  @Mapping(target = "productName", source = "productName")
  @Mapping(target = "productDescription", source = "productDescription")
  @Mapping(target = "productSellingPrice", source = "productPrice")
  @Mapping(target = "quantity", source = "quantity")
  @Mapping(target = "shop.shopId", source = "shopId")
  @Mapping(target = "category.categoryId", source = "categoryId")
  @Mapping(target = "brand.brandId", source = "brandId")
  Product toEntity(ProductRequest request);

  @Mapping(target = "productId", source = "productId")
  @Mapping(target = "productName", source = "productName")
  @Mapping(target = "productDescription", source = "productDescription")
  @Mapping(target = "productPrice", source = "productSellingPrice")
  @Mapping(target = "quantity", source = "quantity")
  @Mapping(target = "shopId", source = "shop.shopId")
  @Mapping(target = "shopName", source = "shop.shopName")
  @Mapping(target = "categoryId", source = "category.categoryId")
  @Mapping(target = "categoryName", source = "category.categoryName")
  @Mapping(target = "brandId", source = "brand.brandId")
  @Mapping(target = "brandName", source = "brand.brandName")
  ProductResponse toResponse(Product entity);

  @Mapping(target = "productId", source = "productId")
  @Mapping(target = "productName", source = "productName")
  @Mapping(target = "productDescription", source = "productDescription")
  @Mapping(target = "productSellingPrice", source = "productSellingPrice")
  @Mapping(target = "quantity", source = "quantity")
  @Mapping(target = "shop.shopId", source = "shop.shopId")
  @Mapping(target = "category.categoryId", source = "category.categoryId")
  @Mapping(target = "brand.brandId", source = "brand.brandId")
  ProductDao toDao(Product entity);

  @Mapping(target = "productId", source = "productId")
  @Mapping(target = "productName", source = "productName")
  @Mapping(target = "productDescription", source = "productDescription")
  @Mapping(target = "productSellingPrice", source = "productSellingPrice")
  @Mapping(target = "quantity", source = "quantity")
  @Mapping(target = "shop.shopId", source = "shop.shopId")
  @Mapping(target = "category.categoryId", source = "category.categoryId")
  @Mapping(target = "brand.brandId", source = "brand.brandId")
  Product toEntityFromDao(ProductDao dao);
}