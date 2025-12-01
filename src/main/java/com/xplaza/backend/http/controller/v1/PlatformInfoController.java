/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller.v1;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.domain.PlatformInfo;
import com.xplaza.backend.http.dto.response.PlatformInfoResponse;
import com.xplaza.backend.mapper.PlatformInfoMapper;
import com.xplaza.backend.service.PlatformInfoService;

/**
 * Platform Info Controller - Clean REST API design.
 */
@RestController
@RequestMapping("/api/v1/platform-info")
@RequiredArgsConstructor
@Validated
@Tag(name = "Platform Info", description = "APIs for platform information")
public class PlatformInfoController {

  private final PlatformInfoService platformInfoService;
  private final PlatformInfoMapper platformInfoMapper;

  @GetMapping
  @Operation(summary = "Get platform info", description = "Get the platform information")
  public ResponseEntity<ApiResponse<PlatformInfoResponse>> getPlatformInfo() {
    PlatformInfo platformInfo = platformInfoService.listPlatform();
    PlatformInfoResponse dto = platformInfoMapper.toResponse(platformInfo);
    return ResponseEntity.ok(ApiResponse.ok(dto));
  }

  @PutMapping
  @Operation(summary = "Update platform info", description = "Update the platform information")
  public ResponseEntity<ApiResponse<Void>> updatePlatformInfo(@RequestBody @Valid PlatformInfo platformInfo) {
    platformInfoService.updatePlatformInfo(platformInfo);
    return ResponseEntity.ok(ApiResponse.ok("Platform information updated successfully"));
  }
}
