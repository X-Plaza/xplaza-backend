/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.catalog.service;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.catalog.domain.entity.ProductVariationType;
import com.xplaza.backend.catalog.domain.repository.ProductVariationTypeRepository;
import com.xplaza.backend.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class ProductVariationTypeService {

  private final ProductVariationTypeRepository repository;

  @Transactional(readOnly = true)
  public List<ProductVariationType> listProductVariationTypes() {
    return repository.findAll();
  }

  @Transactional(readOnly = true)
  public ProductVariationType listProductVariationType(Long id) {
    return repository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product Variation Type not found with id: " + id));
  }

  @Transactional
  public ProductVariationType addProductVariationType(ProductVariationType type) {
    return repository.save(type);
  }

  @Transactional
  public ProductVariationType updateProductVariationType(ProductVariationType type) {
    ProductVariationType existing = listProductVariationType(type.getProductVarTypeId());
    existing.setVarTypeName(type.getVarTypeName());
    existing.setVarTypeDescription(type.getVarTypeDescription());
    return repository.save(existing);
  }

  @Transactional
  public void deleteProductVariationType(Long id) {
    ProductVariationType existing = listProductVariationType(id);
    repository.delete(existing);
  }
}
