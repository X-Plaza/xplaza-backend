/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller.v2;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.common.util.ApiResponseV2;
import com.xplaza.backend.http.dto.response.PlatformInfoResponse;
import com.xplaza.backend.mapper.PlatformInfoMapper;
import com.xplaza.backend.service.PlatformInfoService;
import com.xplaza.backend.service.entity.PlatformInfo;

/**
 * V2 Platform Info Controller - Clean REST API design.
 */
@RestController
@RequestMapping("/api/v2/platform-info")
@RequiredArgsConstructor
@Validated
@Tag(name = "Platform Info V2", description = "V2 APIs for platform information")
public class PlatformInfoControllerV2 {

  private final PlatformInfoService platformInfoService;
  private final PlatformInfoMapper platformInfoMapper;

  @GetMapping
  @Operation(summary = "Get platform info", description = "Get the platform information")
  public ResponseEntity<ApiResponseV2<PlatformInfoResponse>> getPlatformInfo() {
    PlatformInfo platformInfo = platformInfoService.listPlatform();
    PlatformInfoResponse dto = platformInfoMapper.toResponse(platformInfo);
    return ResponseEntity.ok(ApiResponseV2.ok(dto));
  }

  @PutMapping
  @Operation(summary = "Update platform info", description = "Update the platform information")
  public ResponseEntity<ApiResponseV2<Void>> updatePlatformInfo(@RequestBody @Valid PlatformInfo platformInfo) {
    platformInfoService.updatePlatformInfo(platformInfo);
    return ResponseEntity.ok(ApiResponseV2.ok("Platform information updated successfully"));
  }
}
