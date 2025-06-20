/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.jpa.dao.DiscountTypeDao;
import com.xplaza.backend.jpa.repository.DiscountTypeRepository;
import com.xplaza.backend.mapper.DiscountTypeMapper;
import com.xplaza.backend.service.entity.DiscountType;

@Service
public class DiscountTypeService {

  private final DiscountTypeRepository discountTypeRepo;
  private final DiscountTypeMapper discountTypeMapper;

  @Autowired
  public DiscountTypeService(DiscountTypeRepository discountTypeRepo, DiscountTypeMapper discountTypeMapper) {
    this.discountTypeRepo = discountTypeRepo;
    this.discountTypeMapper = discountTypeMapper;
  }

  @Transactional
  public DiscountType addDiscountType(DiscountType discountType) {
    DiscountTypeDao discountTypeDao = discountTypeMapper.toDao(discountType);
    DiscountTypeDao savedDiscountTypeDao = discountTypeRepo.save(discountTypeDao);
    return discountTypeMapper.toEntityFromDao(savedDiscountTypeDao);
  }

  @Transactional
  public DiscountType updateDiscountType(Long id, DiscountType discountType) {
    DiscountTypeDao existingDiscountTypeDao = discountTypeRepo.findById(id)
        .orElseThrow(() -> new RuntimeException("Discount type not found with id: " + id));

    DiscountTypeDao discountTypeDao = discountTypeMapper.toDao(discountType);
    discountTypeDao.setDiscountTypeId(existingDiscountTypeDao.getDiscountTypeId());

    DiscountTypeDao updatedDiscountTypeDao = discountTypeRepo.save(discountTypeDao);
    return discountTypeMapper.toEntityFromDao(updatedDiscountTypeDao);
  }

  @Transactional
  public void deleteDiscountType(Long id) {
    discountTypeRepo.deleteById(id);
  }

  public List<DiscountType> listDiscountTypes() {
    List<DiscountTypeDao> discountTypeDaos = discountTypeRepo.findAll();
    return discountTypeDaos.stream()
        .map(discountTypeMapper::toEntityFromDao)
        .toList();
  }

  public DiscountType listDiscountType(Long id) {
    DiscountTypeDao discountTypeDao = discountTypeRepo.findById(id)
        .orElseThrow(() -> new RuntimeException("Discount type not found with id: " + id));
    return discountTypeMapper.toEntityFromDao(discountTypeDao);
  }
}
