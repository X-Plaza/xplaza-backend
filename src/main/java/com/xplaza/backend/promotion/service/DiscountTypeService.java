/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.promotion.service;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.exception.ResourceNotFoundException;
import com.xplaza.backend.promotion.domain.entity.DiscountType;
import com.xplaza.backend.promotion.repository.DiscountTypeRepository;

@Service
@RequiredArgsConstructor
public class DiscountTypeService {
  private final DiscountTypeRepository discountTypeRepository;

  public List<DiscountType> listDiscountTypes() {
    return discountTypeRepository.findAll();
  }

  public DiscountType getDiscountType(Long id) {
    return discountTypeRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("DiscountType not found: " + id));
  }

  @Transactional
  public DiscountType createDiscountType(DiscountType discountType) {
    return discountTypeRepository.save(discountType);
  }

  @Transactional
  public DiscountType updateDiscountType(Long id, DiscountType details) {
    DiscountType existing = getDiscountType(id);
    existing.setDiscountTypeName(details.getDiscountTypeName());
    existing.setDescription(details.getDescription());
    return discountTypeRepository.save(existing);
  }

  @Transactional
  public void deleteDiscountType(Long id) {
    if (!discountTypeRepository.existsById(id)) {
      throw new ResourceNotFoundException("DiscountType not found: " + id);
    }
    discountTypeRepository.deleteById(id);
  }
}
