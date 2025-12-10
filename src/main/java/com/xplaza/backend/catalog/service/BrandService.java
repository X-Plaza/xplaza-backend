/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.catalog.service;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.catalog.domain.entity.Brand;
import com.xplaza.backend.catalog.domain.repository.BrandRepository;
import com.xplaza.backend.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class BrandService {
  private final BrandRepository brandRepo;

  @Transactional
  public Brand addBrand(Brand brand) {
    return brandRepo.save(brand);
  }

  @Transactional
  public Brand updateBrand(Brand brand) {
    Brand existingBrand = brandRepo.findById(brand.getBrandId())
        .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + brand.getBrandId()));

    existingBrand.setBrandName(brand.getBrandName());
    existingBrand.setBrandDescription(brand.getBrandDescription());
    return brandRepo.save(existingBrand);
  }

  @Transactional
  public void deleteBrand(Long id) {
    if (!brandRepo.existsById(id)) {
      throw new ResourceNotFoundException("Brand not found with id: " + id);
    }
    brandRepo.deleteById(id);
  }

  public List<Brand> listBrands() {
    return brandRepo.findAll();
  }

  public Brand listBrand(Long id) {
    return brandRepo.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Brand not found with id: " + id));
  }

  public String getBrandNameByID(Long id) {
    return brandRepo.getName(id);
  }

  public boolean isExist(Brand entity) {
    return brandRepo.existsByBrandName(entity.getBrandName());
  }

  // ===== Paginated Methods =====

  public Page<Brand> listBrandsPaginated(Pageable pageable) {
    return brandRepo.findAll(pageable);
  }

  public Page<Brand> searchBrands(String searchTerm, Pageable pageable) {
    return brandRepo.findByBrandNameContainingIgnoreCase(searchTerm, pageable);
  }
}
