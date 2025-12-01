/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.exception.ResourceNotFoundException;
import com.xplaza.backend.jpa.dao.ProductVariationTypeDao;
import com.xplaza.backend.jpa.repository.ProductVariationTypeRepository;
import com.xplaza.backend.mapper.ProductVariationTypeMapper;
import com.xplaza.backend.service.entity.ProductVariationType;

@Service
public class ProductVariationTypeService {

  private final ProductVariationTypeRepository productVariationTypeRepository;
  private final ProductVariationTypeMapper productVariationTypeMapper;

  @Autowired
  public ProductVariationTypeService(ProductVariationTypeRepository productVariationTypeRepository,
      ProductVariationTypeMapper productVariationTypeMapper) {
    this.productVariationTypeRepository = productVariationTypeRepository;
    this.productVariationTypeMapper = productVariationTypeMapper;
  }

  @Transactional
  public ProductVariationType addProductVariationType(ProductVariationType productVariationType) {
    ProductVariationTypeDao productVariationTypeDao = productVariationTypeMapper.toDao(productVariationType);
    ProductVariationTypeDao savedProductVariationTypeDao = productVariationTypeRepository.save(productVariationTypeDao);
    return productVariationTypeMapper.toEntityFromDao(savedProductVariationTypeDao);
  }

  @Transactional
  public ProductVariationType updateProductVariationType(ProductVariationType productVariationType) {
    ProductVariationTypeDao existingProductVariationTypeDao = productVariationTypeRepository
        .findById(productVariationType.getProductVarTypeId())
        .orElseThrow(() -> new ResourceNotFoundException(
            "Product variation type not found with id: " + productVariationType.getProductVarTypeId()));
    ProductVariationTypeDao productVariationTypeDao = productVariationTypeMapper.toDao(productVariationType);
    productVariationTypeDao.setProductVarTypeId(existingProductVariationTypeDao.getProductVarTypeId());
    ProductVariationTypeDao updatedProductVariationTypeDao = productVariationTypeRepository
        .save(productVariationTypeDao);
    return productVariationTypeMapper.toEntityFromDao(updatedProductVariationTypeDao);
  }

  @Transactional
  public void deleteProductVariationType(Long id) {
    productVariationTypeRepository.deleteById(id);
  }

  public List<ProductVariationType> listProductVariationTypes() {
    List<ProductVariationTypeDao> productVariationTypeDaos = productVariationTypeRepository.findAll();
    return productVariationTypeDaos.stream()
        .map(productVariationTypeMapper::toEntityFromDao)
        .toList();
  }

  public ProductVariationType listProductVariationType(Long id) {
    ProductVariationTypeDao productVariationTypeDao = productVariationTypeRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product variation type not found with id: " + id));
    return productVariationTypeMapper.toEntityFromDao(productVariationTypeDao);
  }
}
