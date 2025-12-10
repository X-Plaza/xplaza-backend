/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.promotion.service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.catalog.domain.entity.Product;
import com.xplaza.backend.catalog.domain.repository.ProductRepository;
import com.xplaza.backend.exception.ResourceNotFoundException;
import com.xplaza.backend.promotion.domain.entity.DiscountType;
import com.xplaza.backend.promotion.domain.entity.ProductDiscount;
import com.xplaza.backend.promotion.repository.DiscountTypeRepository;
import com.xplaza.backend.promotion.repository.ProductDiscountRepository;

@Service
@RequiredArgsConstructor
public class ProductDiscountService {
  private final ProductDiscountRepository productDiscountRepository;
  private final ProductRepository productRepository;
  private final DiscountTypeRepository discountTypeRepository;

  public List<ProductDiscount> listProductDiscounts() {
    return productDiscountRepository.findAll();
  }

  public List<ProductDiscount> listActiveProductDiscounts() {
    return productDiscountRepository.findByIsActiveTrue();
  }

  public List<ProductDiscount> listProductDiscountsByProduct(Long productId) {
    return productDiscountRepository.findByProductProductId(productId);
  }

  public ProductDiscount getProductDiscount(Long id) {
    return productDiscountRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("ProductDiscount not found: " + id));
  }

  @Transactional
  public ProductDiscount createProductDiscount(ProductDiscount discount, Long productId, Long discountTypeId) {
    Product product = productRepository.findById(productId)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found: " + productId));
    discount.setProduct(product);

    if (discountTypeId != null) {
      DiscountType discountType = discountTypeRepository.findById(discountTypeId)
          .orElseThrow(() -> new ResourceNotFoundException("DiscountType not found: " + discountTypeId));
      discount.setDiscountType(discountType);
    }

    return productDiscountRepository.save(discount);
  }

  @Transactional
  public ProductDiscount updateProductDiscount(Long id, ProductDiscount details) {
    ProductDiscount existing = getProductDiscount(id);
    existing.setDiscountValue(details.getDiscountValue());
    existing.setStartDate(details.getStartDate());
    existing.setEndDate(details.getEndDate());
    existing.setIsActive(details.getIsActive());
    return productDiscountRepository.save(existing);
  }

  @Transactional
  public void deleteProductDiscount(Long id) {
    if (!productDiscountRepository.existsById(id)) {
      throw new ResourceNotFoundException("ProductDiscount not found: " + id);
    }
    productDiscountRepository.deleteById(id);
  }

  public BigDecimal calculateDiscountedPrice(Product product) {
    List<ProductDiscount> discounts = productDiscountRepository.findByProductProductId(product.getProductId());
    LocalDateTime now = LocalDateTime.now();

    BigDecimal originalPrice = BigDecimal.valueOf(product.getProductSellingPrice());
    BigDecimal bestPrice = originalPrice;

    for (ProductDiscount discount : discounts) {
      if (Boolean.TRUE.equals(discount.getIsActive()) &&
          (discount.getStartDate() == null || !now.isBefore(discount.getStartDate())) &&
          (discount.getEndDate() == null || !now.isAfter(discount.getEndDate()))) {

        BigDecimal currentDiscountedPrice = originalPrice;
        String typeName = discount.getDiscountType() != null
            ? discount.getDiscountType().getDiscountTypeName().toUpperCase()
            : "";

        if (typeName.contains("PERCENT")) {
          BigDecimal percent = BigDecimal.valueOf(discount.getDiscountValue()).divide(BigDecimal.valueOf(100));
          BigDecimal discountAmount = originalPrice.multiply(percent);
          currentDiscountedPrice = originalPrice.subtract(discountAmount);
        } else {
          // Assume fixed amount
          BigDecimal amount = BigDecimal.valueOf(discount.getDiscountValue());
          currentDiscountedPrice = originalPrice.subtract(amount);
        }

        if (currentDiscountedPrice.compareTo(bestPrice) < 0) {
          bestPrice = currentDiscountedPrice;
        }
      }
    }

    return bestPrice.max(BigDecimal.ZERO);
  }
}
