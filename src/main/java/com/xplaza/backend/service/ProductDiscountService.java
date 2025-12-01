/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.service;

import java.util.Date;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.common.util.DateConverter;
import com.xplaza.backend.domain.ProductDiscount;
import com.xplaza.backend.exception.ResourceNotFoundException;
import com.xplaza.backend.exception.ValidationException;
import com.xplaza.backend.jpa.dao.ProductDao;
import com.xplaza.backend.jpa.dao.ProductDiscountDao;
import com.xplaza.backend.jpa.repository.ProductDiscountRepository;
import com.xplaza.backend.jpa.repository.ProductRepository;
import com.xplaza.backend.mapper.ProductDiscountMapper;
// import com.xplaza.backend.domain.ProductDiscountList;

@Service
@RequiredArgsConstructor
public class ProductDiscountService {
  private final ProductDiscountRepository productDiscountRepository;
  private final ProductRepository productRepo;
  private final ProductDiscountMapper productDiscountMapper;
  private final DateConverter dateConverter;

  public boolean checkDiscountValidity(ProductDiscount entity) {
    if (entity == null) {
      throw new ValidationException("Product discount entity cannot be null");
    }
    if (entity.getProduct() == null || entity.getProduct().getProductId() == null) {
      throw new ValidationException("Product information is required for discount validation");
    }
    if (entity.getDiscountAmount() == null || entity.getDiscountAmount() < 0) {
      throw new ValidationException("Discount amount must be a positive value");
    }

    ProductDao product = productRepo.findProductById(entity.getProduct().getProductId());
    if (product == null) {
      throw new ResourceNotFoundException("Product not found with id: " + entity.getProduct().getProductId());
    }

    Double originalSellingPrice = product.getProductSellingPrice();
    if (entity.getDiscountAmount() > originalSellingPrice) {
      return false;
    }
    return true;
  }

  @Transactional
  public ProductDiscount addProductDiscount(ProductDiscount productDiscount) {
    ProductDiscountDao productDiscountDao = productDiscountMapper.toDao(productDiscount);
    ProductDiscountDao savedProductDiscountDao = productDiscountRepository.save(productDiscountDao);
    return productDiscountMapper.toEntityFromDao(savedProductDiscountDao);
  }

  @Transactional
  public ProductDiscount updateProductDiscount(Long id, ProductDiscount productDiscount) {
    ProductDiscountDao existingProductDiscountDao = productDiscountRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product discount not found with id: " + id));
    ProductDiscountDao productDiscountDao = productDiscountMapper.toDao(productDiscount);
    productDiscountDao.setProductDiscountId(existingProductDiscountDao.getProductDiscountId());
    ProductDiscountDao updatedProductDiscountDao = productDiscountRepository.save(productDiscountDao);
    return productDiscountMapper.toEntityFromDao(updatedProductDiscountDao);
  }

  @Transactional
  public void deleteProductDiscount(Long id) {
    productDiscountRepository.deleteById(id);
  }

  public List<ProductDiscount> listProductDiscounts() {
    List<ProductDiscountDao> productDiscountDaos = productDiscountRepository.findAll();
    return productDiscountDaos.stream()
        .map(productDiscountMapper::toEntityFromDao)
        .toList();
  }

  public ProductDiscount listProductDiscount(Long id) {
    ProductDiscountDao productDiscountDao = productDiscountRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product discount not found with id: " + id));
    return productDiscountMapper.toEntityFromDao(productDiscountDao);
  }

  public String getProductNameByID(Long id) {
    return productDiscountRepository.getName(id);
  }

  // public List<ProductDiscountList> listProductDiscountsByProduct(Long
  // productId) {
  // return productDiscountListRepo.findByProductId(productId).stream()
  // .map(productDiscountListMapper::toEntity)
  // .collect(Collectors.toList());
  // }

  public boolean checkDiscountDateValidity(ProductDiscount entity) {
    Date currentDate = new Date();
    currentDate = dateConverter.convertDateToStartOfTheDay(currentDate);
    Date startDate = entity.getValidFrom();
    Date endDate = entity.getValidTo();
    if (currentDate.after(startDate))
      return false;
    if (currentDate.after(endDate))
      return false;
    if (startDate.after(endDate))
      return false;
    return true;
  }

  public ProductDiscount getProductDiscountByProduct(Long productId) {
    ProductDiscountDao productDiscountDao = productDiscountRepository.findByProductId(productId);
    return productDiscountMapper.toEntityFromDao(productDiscountDao);
  }
}
