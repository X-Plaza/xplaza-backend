/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.catalog.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import com.xplaza.backend.catalog.domain.entity.Product;
import com.xplaza.backend.catalog.domain.repository.ProductRepository;
import com.xplaza.backend.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

  @Mock
  private ProductRepository productRepository;

  @InjectMocks
  private ProductService productService;

  @Test
  void addProduct_ShouldReturnSavedProduct() {
    Product product = new Product();
    product.setProductName("Test Product");
    when(productRepository.save(any(Product.class))).thenReturn(product);

    Product savedProduct = productService.addProduct(product);

    assertNotNull(savedProduct);
    assertEquals("Test Product", savedProduct.getProductName());
    verify(productRepository, times(1)).save(product);
  }

  @Test
  void updateProduct_ShouldReturnUpdatedProduct_WhenProductExists() {
    Long productId = 1L;
    Product product = new Product();
    product.setProductId(productId);
    product.setProductName("Updated Product");

    when(productRepository.findById(productId)).thenReturn(Optional.of(product));
    when(productRepository.save(any(Product.class))).thenReturn(product);

    Product updatedProduct = productService.updateProduct(product);

    assertNotNull(updatedProduct);
    assertEquals("Updated Product", updatedProduct.getProductName());
    verify(productRepository, times(1)).findById(productId);
    verify(productRepository, times(1)).save(product);
  }

  @Test
  void updateProduct_ShouldThrowException_WhenProductDoesNotExist() {
    Long productId = 1L;
    Product product = new Product();
    product.setProductId(productId);

    when(productRepository.findById(productId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> productService.updateProduct(product));
    verify(productRepository, times(1)).findById(productId);
    verify(productRepository, times(0)).save(any(Product.class));
  }

  @Test
  void deleteProduct_ShouldDeleteProduct_WhenProductExists() {
    Long productId = 1L;
    when(productRepository.existsById(productId)).thenReturn(true);

    productService.deleteProduct(productId);

    verify(productRepository, times(1)).existsById(productId);
    verify(productRepository, times(1)).deleteById(productId);
  }

  @Test
  void deleteProduct_ShouldThrowException_WhenProductDoesNotExist() {
    Long productId = 1L;
    when(productRepository.existsById(productId)).thenReturn(false);

    assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(productId));
    verify(productRepository, times(1)).existsById(productId);
    verify(productRepository, times(0)).deleteById(productId);
  }

  @Test
  void listProducts_ShouldReturnListOfProducts() {
    Product product1 = new Product();
    Product product2 = new Product();
    when(productRepository.findAll()).thenReturn(List.of(product1, product2));

    List<Product> products = productService.listProducts();

    assertEquals(2, products.size());
    verify(productRepository, times(1)).findAll();
  }

  @Test
  void listProduct_ShouldReturnProduct_WhenProductExists() {
    Long productId = 1L;
    Product product = new Product();
    product.setProductId(productId);
    when(productRepository.findById(productId)).thenReturn(Optional.of(product));

    Product foundProduct = productService.listProduct(productId);

    assertNotNull(foundProduct);
    assertEquals(productId, foundProduct.getProductId());
    verify(productRepository, times(1)).findById(productId);
  }

  @Test
  void listProduct_ShouldThrowException_WhenProductDoesNotExist() {
    Long productId = 1L;
    when(productRepository.findById(productId)).thenReturn(Optional.empty());

    assertThrows(ResourceNotFoundException.class, () -> productService.listProduct(productId));
    verify(productRepository, times(1)).findById(productId);
  }

  @Test
  void findProducts_ShouldReturnPageOfProducts() {
    Pageable pageable = PageRequest.of(0, 10);
    Product product = new Product();
    Page<Product> page = new PageImpl<>(List.of(product));
    when(productRepository.findAll(pageable)).thenReturn(page);

    Page<Product> result = productService.findProducts(pageable);

    assertEquals(1, result.getTotalElements());
    verify(productRepository, times(1)).findAll(pageable);
  }
}
