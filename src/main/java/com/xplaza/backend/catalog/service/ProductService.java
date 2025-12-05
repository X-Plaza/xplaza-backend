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

import com.xplaza.backend.catalog.domain.entity.Product;
import com.xplaza.backend.catalog.domain.repository.ProductRepository;
import com.xplaza.backend.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class ProductService {
  private final ProductRepository productRepository;

  @Transactional
  public Product addProduct(Product product) {
    return productRepository.save(product);
  }

  @Transactional
  public Product updateProduct(Product product) {
    productRepository.findById(product.getProductId())
        .orElseThrow(
            () -> new ResourceNotFoundException("Product not found with id: " + product.getProductId()));
    return productRepository.save(product);
  }

  @Transactional
  public void deleteProduct(Long id) {
    if (!productRepository.existsById(id)) {
      throw new ResourceNotFoundException("Product not found with id: " + id);
    }
    productRepository.deleteById(id);
  }

  public List<Product> listProducts() {
    return productRepository.findAll();
  }

  public Product listProduct(Long id) {
    return productRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Product not found with id: " + id));
  }

  public List<Product> listProductsByShop(Long shopId) {
    return productRepository.findByShopId(shopId);
  }

  public List<Product> listProductsByCategory(Long categoryId) {
    return productRepository.findByCategoryId(categoryId);
  }

  public List<Product> listProductsByBrand(Long brandId) {
    return productRepository.findByBrandId(brandId);
  }

  public Page<Product> findProducts(Pageable pageable) {
    return productRepository.findAll(pageable);
  }

  public Page<Product> findProductsByShop(Long shopId, Pageable pageable) {
    return productRepository.findByShopShopId(shopId, pageable);
  }

  public Page<Product> findProductsByCategory(Long categoryId, Pageable pageable) {
    return productRepository.findByCategoryCategoryId(categoryId, pageable);
  }

  public Page<Product> findProductsByBrand(Long brandId, Pageable pageable) {
    return productRepository.findByBrandBrandId(brandId, pageable);
  }

  public Page<Product> searchProductsByName(String name, Pageable pageable) {
    return productRepository.findByProductNameContainingIgnoreCase(name, pageable);
  }

  public Page<Product> findProductsByShopAndCategory(Long shopId, Long categoryId, Pageable pageable) {
    return productRepository.findByShopShopIdAndCategoryCategoryId(shopId, categoryId, pageable);
  }

  public String getProductNameByID(Long id) {
    return productRepository.getName(id);
  }

  @Transactional
  public void updateProductInventory(Long id, int quantity) {
    if (!productRepository.existsById(id)) {
      throw new ResourceNotFoundException("Product not found with id: " + id);
    }
    productRepository.updateInventory(id, quantity);
  }

  public boolean exists(Long id) {
    return productRepository.existsById(id);
  }
}
