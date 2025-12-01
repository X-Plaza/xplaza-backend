/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.domain.Product;
import com.xplaza.backend.exception.ResourceNotFoundException;
import com.xplaza.backend.jpa.dao.ProductDao;
import com.xplaza.backend.jpa.repository.ProductRepository;
import com.xplaza.backend.mapper.ProductMapper;

@Service
@RequiredArgsConstructor
public class ProductService {
  private final ProductRepository productRepo;
  private final ProductMapper productMapper;

  @Transactional
  public Product addProduct(Product product) {
    ProductDao productDao = productMapper.toDao(product);
    ProductDao savedProductDao = productRepo.save(productDao);
    return productMapper.toEntityFromDao(savedProductDao);
  }

  @Transactional
  public Product updateProduct(Product product) {
    productRepo.findById(product.getProductId())
        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + product.getProductId()));
    ProductDao productDao = productMapper.toDao(product);
    ProductDao updatedProductDao = productRepo.save(productDao);
    return productMapper.toEntityFromDao(updatedProductDao);
  }

  @Transactional
  public void deleteProduct(Long id) {
    if (!productRepo.existsById(id)) {
      throw new ResourceNotFoundException("Product not found with id: " + id);
    }
    productRepo.deleteById(id);
  }

  // ===== List Methods (Non-Paginated - for backward compatibility) =====

  public List<Product> listProducts() {
    List<ProductDao> productDaos = productRepo.findAll();
    return productDaos.stream()
        .map(productMapper::toEntityFromDao)
        .collect(Collectors.toList());
  }

  public Product listProduct(Long id) {
    ProductDao productDao = productRepo.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
    return productMapper.toEntityFromDao(productDao);
  }

  public List<Product> listProductsByShop(Long shopId) {
    List<ProductDao> productDaos = productRepo.findByShopId(shopId);
    return productDaos.stream()
        .map(productMapper::toEntityFromDao)
        .collect(Collectors.toList());
  }

  public List<Product> listProductsByCategory(Long categoryId) {
    List<ProductDao> productDaos = productRepo.findByCategoryId(categoryId);
    return productDaos.stream()
        .map(productMapper::toEntityFromDao)
        .collect(Collectors.toList());
  }

  public List<Product> listProductsByBrand(Long brandId) {
    List<ProductDao> productDaos = productRepo.findByBrandId(brandId);
    return productDaos.stream()
        .map(productMapper::toEntityFromDao)
        .collect(Collectors.toList());
  }

  // ===== Paginated Methods (API) =====

  public Page<Product> findProducts(Pageable pageable) {
    return productRepo.findAll(pageable)
        .map(productMapper::toEntityFromDao);
  }

  public Page<Product> findProductsByShop(Long shopId, Pageable pageable) {
    return productRepo.findByShopShopId(shopId, pageable)
        .map(productMapper::toEntityFromDao);
  }

  public Page<Product> findProductsByCategory(Long categoryId, Pageable pageable) {
    return productRepo.findByCategoryCategoryId(categoryId, pageable)
        .map(productMapper::toEntityFromDao);
  }

  public Page<Product> findProductsByBrand(Long brandId, Pageable pageable) {
    return productRepo.findByBrandBrandId(brandId, pageable)
        .map(productMapper::toEntityFromDao);
  }

  public Page<Product> searchProductsByName(String name, Pageable pageable) {
    return productRepo.findByProductNameContainingIgnoreCase(name, pageable)
        .map(productMapper::toEntityFromDao);
  }

  public Page<Product> findProductsByShopAndCategory(Long shopId, Long categoryId, Pageable pageable) {
    return productRepo.findByShopShopIdAndCategoryCategoryId(shopId, categoryId, pageable)
        .map(productMapper::toEntityFromDao);
  }

  // ===== Utility Methods =====

  public String getProductNameByID(Long id) {
    return productRepo.getName(id);
  }

  @Transactional
  public void updateProductInventory(Long id, int quantity) {
    if (!productRepo.existsById(id)) {
      throw new ResourceNotFoundException("Product not found with id: " + id);
    }
    productRepo.updateInventory(id, quantity);
  }

  public boolean exists(Long id) {
    return productRepo.existsById(id);
  }
}
