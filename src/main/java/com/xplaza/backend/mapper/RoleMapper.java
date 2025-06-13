/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;

import com.xplaza.backend.http.dto.request.RoleRequest;
import com.xplaza.backend.http.dto.response.RoleResponse;
import com.xplaza.backend.jpa.dao.RoleDao;
import com.xplaza.backend.service.entity.Role;

@Mapper(componentModel = "spring")
public interface RoleMapper {
  Role toEntity(RoleRequest entity);

  RoleResponse toResponse(Role entity);

  RoleDao toDao(Role entity);

  Role toEntityFromDao(RoleDao dao);
}