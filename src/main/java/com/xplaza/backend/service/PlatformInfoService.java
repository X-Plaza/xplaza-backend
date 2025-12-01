/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.service;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.domain.PlatformInfo;
import com.xplaza.backend.jpa.repository.PlatformInfoRepository;
import com.xplaza.backend.mapper.PlatformInfoMapper;

@Service
@RequiredArgsConstructor
public class PlatformInfoService {
  private final PlatformInfoRepository platformInfoRepo;
  private final PlatformInfoMapper platformInfoMapper;

  public PlatformInfo listPlatform() {
    return platformInfoMapper.toEntityFromDao(platformInfoRepo.findAll().getFirst());
  }

  @Transactional
  public void updatePlatformInfo(PlatformInfo platformInfo) {
    var existingPlatformInfo = platformInfoRepo.findById(platformInfo.getId())
        .orElseThrow(() -> new IllegalArgumentException("Platform info not found"));
    existingPlatformInfo.setName(platformInfo.getName());
    existingPlatformInfo.setInvoice(platformInfo.getInvoicePrefix());
    existingPlatformInfo.setAdditionalInfo(platformInfo.getAdditionalInfo());
    existingPlatformInfo.setCellNo(platformInfo.getCellNo());
    existingPlatformInfo.setBannerImage(platformInfo.getBannerImage());
    existingPlatformInfo.setBannerImagePath(platformInfo.getBannerImagePath());
    platformInfoRepo.save(existingPlatformInfo);
  }

  @Transactional
  public void addPlatformInfo(@Valid PlatformInfo platformInfo) {
    platformInfoRepo.save(platformInfoMapper.toDao(platformInfo));
  }
}
