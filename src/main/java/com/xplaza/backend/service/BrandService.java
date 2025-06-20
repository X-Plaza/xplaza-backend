/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.jpa.dao.BrandDao;
import com.xplaza.backend.jpa.repository.BrandRepository;
import com.xplaza.backend.mapper.BrandMapper;
import com.xplaza.backend.service.entity.Brand;

@Service
public class BrandService {
  private final BrandRepository brandRepo;
  private final BrandMapper brandMapper;

  @Autowired
  public BrandService(BrandRepository brandRepo, BrandMapper brandMapper) {
    this.brandRepo = brandRepo;
    this.brandMapper = brandMapper;
  }

  @Transactional
  public Brand addBrand(Brand brand) {
    BrandDao brandDao = brandMapper.toDao(brand);
    BrandDao savedBrandDao = brandRepo.save(brandDao);
    return brandMapper.toEntityFromDao(savedBrandDao);
  }

  @Transactional
  public Brand updateBrand(Brand brand) {
    BrandDao existingBrandDao = brandRepo.findById(brand.getBrandId())
        .orElseThrow(() -> new RuntimeException("Brand not found with id: " + brand.getBrandId()));

    // Update fields from the provided brand entity
    existingBrandDao.setBrandName(brand.getBrandName());
    existingBrandDao.setBrandDescription(brand.getBrandDescription());
    BrandDao updatedBrandDao = brandRepo.save(existingBrandDao);
    return brandMapper.toEntityFromDao(updatedBrandDao);
  }

  @Transactional
  public void deleteBrand(Long id) {
    brandRepo.deleteById(id);
  }

  public List<Brand> listBrands() {
    List<BrandDao> brandDaos = brandRepo.findAll();
    return brandDaos.stream()
        .map(brandMapper::toEntityFromDao)
        .collect(Collectors.toList());
  }

  public Brand listBrand(Long id) {
    BrandDao brandDao = brandRepo.findById(id)
        .orElseThrow(() -> new RuntimeException("Brand not found with id: " + id));
    return brandMapper.toEntityFromDao(brandDao);
  }

  public String getBrandNameByID(Long id) {
    return brandRepo.getName(id);
  }

  public boolean isExist(Brand entity) {
    BrandDao brand = brandMapper.toDao(entity);
    return brandRepo.existsByName(brand.getBrandName());
  }
}
