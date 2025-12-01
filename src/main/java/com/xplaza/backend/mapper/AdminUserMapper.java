/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import java.util.List;
import java.util.stream.Collectors;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Named;

import com.xplaza.backend.domain.AdminUser;
import com.xplaza.backend.domain.AdminUserShopLink;
import com.xplaza.backend.domain.Shop;
import com.xplaza.backend.http.dto.request.AdminUserRequest;
import com.xplaza.backend.http.dto.response.AdminUserResponse;
import com.xplaza.backend.jpa.dao.AdminUserDao;
import com.xplaza.backend.jpa.dao.AdminUserShopLinkDao;
import com.xplaza.backend.jpa.dao.ShopDao;

@Mapper(componentModel = "spring")
public interface AdminUserMapper {
  @Mapping(target = "adminUserId", source = "adminUserId")
  @Mapping(target = "userName", source = "userName")
  @Mapping(target = "password", source = "password")
  @Mapping(target = "salt", source = "salt")
  @Mapping(target = "fullName", source = "fullName")
  @Mapping(target = "confirmationCode", source = "confirmationCode")
  @Mapping(target = "role.roleId", source = "roleId")
  @Mapping(target = "shopLinks", source = "adminUserShopLinks", qualifiedByName = "shopLinksToDao")
  AdminUserDao toDao(AdminUser entity);

  @Mapping(target = "adminUserId", source = "adminUserId")
  @Mapping(target = "userName", source = "userName")
  @Mapping(target = "password", source = "password")
  @Mapping(target = "salt", source = "salt")
  @Mapping(target = "fullName", source = "fullName")
  @Mapping(target = "confirmationCode", source = "confirmationCode")
  @Mapping(target = "roleId", source = "role.roleId")
  @Mapping(target = "adminUserShopLinks", source = "shopLinks", qualifiedByName = "shopLinksToEntity")
  AdminUser toEntityFromDao(AdminUserDao dao);

  @Mapping(target = "id", source = "adminUserId")
  @Mapping(target = "fullName", source = "fullName")
  @Mapping(target = "email", source = "userName")
  @Mapping(target = "roleId", source = "roleId")
  @Mapping(target = "shopIds", source = "adminUserShopLinks", qualifiedByName = "shopLinksToIds")
  AdminUserResponse toResponse(AdminUser entity);

  @Mapping(target = "adminUserId", ignore = true)
  @Mapping(target = "userName", source = "email")
  @Mapping(target = "password", source = "password")
  @Mapping(target = "salt", source = "salt")
  @Mapping(target = "fullName", source = "fullName")
  @Mapping(target = "confirmationCode", source = "confirmationCode")
  @Mapping(target = "roleId", source = "roleId")
  @Mapping(target = "adminUserShopLinks", source = "shopIds", qualifiedByName = "idsToShopLinks")
  AdminUser toEntity(AdminUserRequest request);

  @Named("shopLinksToIds")
  default List<Long> shopLinksToIds(List<AdminUserShopLink> links) {
    if (links == null)
      return null;
    return links.stream()
        .map(link -> link.getShop() != null ? link.getShop().getShopId() : null)
        .collect(Collectors.toList());
  }

  @Named("idsToShopLinks")
  default List<AdminUserShopLink> idsToShopLinks(List<Long> shopIds) {
    if (shopIds == null)
      return null;
    return shopIds.stream()
        .map(shopId -> AdminUserShopLink.builder()
            .shop(Shop.builder().shopId(shopId).build())
            .build())
        .collect(Collectors.toList());
  }

  @Named("shopLinksToDao")
  default List<AdminUserShopLinkDao> shopLinksToDao(List<AdminUserShopLink> links) {
    if (links == null)
      return null;
    return links.stream()
        .map(link -> new AdminUserShopLinkDao(
            null, // adminUserId
            link.getShop().getShopId(), // shopId
            null, // adminUser
            createShopDaoWithId(link.getShop().getShopId())))
        .collect(Collectors.toList());
  }

  @Named("shopLinksToEntity")
  default List<AdminUserShopLink> shopLinksToEntity(List<AdminUserShopLinkDao> links) {
    if (links == null)
      return null;
    return links.stream()
        .map(link -> AdminUserShopLink.builder()
            .shop(Shop.builder()
                .shopId(link.getShop().getShopId())
                .build())
            .build())
        .collect(Collectors.toList());
  }

  default ShopDao createShopDaoWithId(Long shopId) {
    ShopDao shopDao = new ShopDao();
    shopDao.setShopId(shopId);
    return shopDao;
  }
}