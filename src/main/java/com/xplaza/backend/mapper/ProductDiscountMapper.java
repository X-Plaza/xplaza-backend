/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import com.xplaza.backend.domain.ProductDiscount;
import com.xplaza.backend.http.dto.request.ProductDiscountRequest;
import com.xplaza.backend.http.dto.response.ProductDiscountResponse;
import com.xplaza.backend.jpa.dao.ProductDiscountDao;

@Mapper(componentModel = "spring", uses = { ProductMapper.class, DiscountTypeMapper.class,
    CurrencyMapper.class }, unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductDiscountMapper {
  @Mapping(target = "productDiscountId", source = "productDiscountId")
  @Mapping(target = "discountType", source = "discountType")
  @Mapping(target = "product", source = "product")
  @Mapping(target = "currency", source = "currency")
  @Mapping(target = "discountAmount", source = "discountAmount")
  @Mapping(target = "discountStartDate", source = "validFrom")
  @Mapping(target = "discountEndDate", source = "validTo")
  ProductDiscountDao toDao(ProductDiscount entity);

  @Mapping(target = "productDiscountId", source = "productDiscountId")
  @Mapping(target = "discountType", source = "discountType")
  @Mapping(target = "product", source = "product")
  @Mapping(target = "currency", source = "currency")
  @Mapping(target = "discountAmount", source = "discountAmount")
  @Mapping(target = "validFrom", source = "discountStartDate")
  @Mapping(target = "validTo", source = "discountEndDate")
  ProductDiscount toEntityFromDao(ProductDiscountDao dao);

  @Mapping(target = "product.productId", source = "productId")
  @Mapping(target = "discountType.discountTypeId", source = "discountTypeId")
  @Mapping(target = "discountAmount", source = "discountAmount")
  @Mapping(target = "currency.currencyId", source = "currencyId")
  @Mapping(target = "validFrom", source = "startDate")
  @Mapping(target = "validTo", source = "endDate")
  ProductDiscount toEntity(ProductDiscountRequest request);

  @Mapping(target = "id", source = "productDiscountId")
  @Mapping(target = "productId", source = "product.productId")
  @Mapping(target = "productName", source = "product.productName")
  @Mapping(target = "discountTypeId", source = "discountType.discountTypeId")
  @Mapping(target = "discountTypeName", source = "discountType.discountTypeName")
  @Mapping(target = "discountAmount", source = "discountAmount")
  @Mapping(target = "currencyId", source = "currency.currencyId")
  @Mapping(target = "currencyName", source = "currency.currencyName")
  @Mapping(target = "currencySign", source = "currency.currencySign")
  @Mapping(target = "startDate", source = "validFrom")
  @Mapping(target = "endDate", source = "validTo")
  ProductDiscountResponse toResponse(ProductDiscount entity);
}