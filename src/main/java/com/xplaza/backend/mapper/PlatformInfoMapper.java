/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;

import com.xplaza.backend.domain.PlatformInfo;
import com.xplaza.backend.http.dto.request.PlatformInfoRequest;
import com.xplaza.backend.http.dto.response.PlatformInfoResponse;
import com.xplaza.backend.jpa.dao.PlatformInfoDao;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface PlatformInfoMapper {
  PlatformInfo toEntity(PlatformInfoRequest request);

  PlatformInfoResponse toResponse(PlatformInfo entity);

  PlatformInfoDao toDao(PlatformInfo entity);

  PlatformInfo toEntityFromDao(PlatformInfoDao dao);
}
