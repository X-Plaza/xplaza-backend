/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.xplaza.backend.domain.Module;
import com.xplaza.backend.http.dto.request.ModuleRequest;
import com.xplaza.backend.http.dto.response.ModuleResponse;
import com.xplaza.backend.jpa.dao.ModuleDao;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ModuleMapper {
  Module toEntity(ModuleRequest request);

  ModuleResponse toResponse(Module entity);

  ModuleDao toDao(Module entity);

  Module toEntityFromDao(ModuleDao dao);
}