/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller;

import java.util.Date;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.http.dto.response.PlatformInfoResponse;
import com.xplaza.backend.service.PlatformInfoService;
import com.xplaza.backend.service.entity.PlatformInfo;

@RestController
@RequestMapping("/api/v1/platform-infos")
public class PlatformInfoController extends BaseController {
  private final PlatformInfoService platformInfoService;

  @Autowired
  public PlatformInfoController(PlatformInfoService platformInfoService) {
    this.platformInfoService = platformInfoService;
  }

  private Date start, end;
  private Long responseTime;

  @GetMapping
  public ResponseEntity<ApiResponse> getPlatformInfo() throws JsonProcessingException {
    start = new Date();
    PlatformInfo platformInfo = platformInfoService.listPlatform();
    PlatformInfoResponse dto = new PlatformInfoResponse();
    end = new Date();
    responseTime = end.getTime() - start.getTime();
    String data = new ObjectMapper().writeValueAsString(dto);
    ApiResponse response = new ApiResponse(responseTime, "Platform Info", HttpStatus.OK.value(), "Success", "", data);
    return ResponseEntity.ok(response);
  }

  @PostMapping
  public ResponseEntity<ApiResponse> addPlatformInfo(@RequestBody @Valid PlatformInfo platformInfo) {
    start = new Date();
    platformInfoService.addPlatformInfo(platformInfo);
    end = new Date();
    responseTime = end.getTime() - start.getTime();
    return new ResponseEntity<>(new ApiResponse(responseTime, "Update Platform Info", HttpStatus.OK.value(),
        "Success", "Platform Info has been updated.", null), HttpStatus.OK);
  }

  @PutMapping
  public ResponseEntity<ApiResponse> updatePlatformInfo(@RequestBody @Valid PlatformInfo platformInfo) {
    start = new Date();
    platformInfoService.updatePlatformInfo(platformInfo);
    end = new Date();
    responseTime = end.getTime() - start.getTime();
    return new ResponseEntity<>(new ApiResponse(responseTime, "Update Platform Info", HttpStatus.OK.value(),
        "Success", "Platform Info has been updated.", null), HttpStatus.OK);
  }
}
