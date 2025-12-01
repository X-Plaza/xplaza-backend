/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.xplaza.backend.exception.ResourceNotFoundException;
import com.xplaza.backend.jpa.dao.ProductDao;
import com.xplaza.backend.jpa.repository.ProductRepository;
import com.xplaza.backend.mapper.ProductMapper;
import com.xplaza.backend.service.entity.Product;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

  @Mock
  private ProductRepository productRepo;

  @Mock
  private ProductMapper productMapper;

  @InjectMocks
  private ProductService productService;

  private Product testProduct;
  private ProductDao testProductDao;

  @BeforeEach
  void setUp() {
    testProduct = new Product();
    testProduct.setProductId(1L);
    testProduct.setProductName("Test Product");
    testProduct.setProductDescription("Test Description");

    testProductDao = new ProductDao();
    testProductDao.setProductId(1L);
    testProductDao.setProductName("Test Product");
    testProductDao.setProductDescription("Test Description");
  }

  @Nested
  @DisplayName("addProduct Tests")
  class AddProductTests {

    @Test
    @DisplayName("Should successfully add a new product")
    void shouldAddProduct() {
      when(productMapper.toDao(testProduct)).thenReturn(testProductDao);
      when(productRepo.save(testProductDao)).thenReturn(testProductDao);
      when(productMapper.toEntityFromDao(testProductDao)).thenReturn(testProduct);

      Product result = productService.addProduct(testProduct);

      assertNotNull(result);
      assertEquals("Test Product", result.getProductName());
      verify(productRepo, times(1)).save(testProductDao);
    }
  }

  @Nested
  @DisplayName("updateProduct Tests")
  class UpdateProductTests {

    @Test
    @DisplayName("Should successfully update an existing product")
    void shouldUpdateProduct() {
      when(productRepo.findById(1L)).thenReturn(Optional.of(testProductDao));
      when(productMapper.toDao(testProduct)).thenReturn(testProductDao);
      when(productRepo.save(testProductDao)).thenReturn(testProductDao);
      when(productMapper.toEntityFromDao(testProductDao)).thenReturn(testProduct);

      Product result = productService.updateProduct(testProduct);

      assertNotNull(result);
      assertEquals("Test Product", result.getProductName());
      verify(productRepo, times(1)).findById(1L);
      verify(productRepo, times(1)).save(testProductDao);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product not found")
    void shouldThrowExceptionWhenProductNotFound() {
      when(productRepo.findById(1L)).thenReturn(Optional.empty());

      assertThrows(ResourceNotFoundException.class, () -> productService.updateProduct(testProduct));
      verify(productRepo, times(1)).findById(1L);
      verify(productRepo, never()).save(any());
    }
  }

  @Nested
  @DisplayName("deleteProduct Tests")
  class DeleteProductTests {

    @Test
    @DisplayName("Should delete product by id")
    void shouldDeleteProduct() {
      when(productRepo.existsById(1L)).thenReturn(true);
      doNothing().when(productRepo).deleteById(1L);

      productService.deleteProduct(1L);

      verify(productRepo, times(1)).existsById(1L);
      verify(productRepo, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product does not exist")
    void shouldThrowExceptionWhenProductDoesNotExist() {
      when(productRepo.existsById(999L)).thenReturn(false);

      assertThrows(ResourceNotFoundException.class, () -> productService.deleteProduct(999L));
      verify(productRepo, times(1)).existsById(999L);
      verify(productRepo, never()).deleteById(any());
    }
  }

  @Nested
  @DisplayName("listProducts Tests")
  class ListProductsTests {

    @Test
    @DisplayName("Should return list of all products")
    void shouldReturnAllProducts() {
      ProductDao secondProductDao = new ProductDao();
      secondProductDao.setProductId(2L);
      secondProductDao.setProductName("Second Product");

      Product secondProduct = new Product();
      secondProduct.setProductId(2L);
      secondProduct.setProductName("Second Product");

      when(productRepo.findAll()).thenReturn(Arrays.asList(testProductDao, secondProductDao));
      when(productMapper.toEntityFromDao(testProductDao)).thenReturn(testProduct);
      when(productMapper.toEntityFromDao(secondProductDao)).thenReturn(secondProduct);

      List<Product> result = productService.listProducts();

      assertNotNull(result);
      assertEquals(2, result.size());
      verify(productRepo, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no products exist")
    void shouldReturnEmptyList() {
      when(productRepo.findAll()).thenReturn(Arrays.asList());

      List<Product> result = productService.listProducts();

      assertNotNull(result);
      assertTrue(result.isEmpty());
    }
  }

  @Nested
  @DisplayName("listProduct Tests")
  class ListProductTests {

    @Test
    @DisplayName("Should return product by id")
    void shouldReturnProductById() {
      when(productRepo.findById(1L)).thenReturn(Optional.of(testProductDao));
      when(productMapper.toEntityFromDao(testProductDao)).thenReturn(testProduct);

      Product result = productService.listProduct(1L);

      assertNotNull(result);
      assertEquals(1L, result.getProductId());
      assertEquals("Test Product", result.getProductName());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product not found")
    void shouldThrowExceptionWhenProductNotFound() {
      when(productRepo.findById(999L)).thenReturn(Optional.empty());

      assertThrows(ResourceNotFoundException.class, () -> productService.listProduct(999L));
    }
  }

  @Nested
  @DisplayName("listProductsByShop Tests")
  class ListProductsByShopTests {

    @Test
    @DisplayName("Should return products by shop id")
    void shouldReturnProductsByShop() {
      when(productRepo.findByShopId(1L)).thenReturn(Arrays.asList(testProductDao));
      when(productMapper.toEntityFromDao(testProductDao)).thenReturn(testProduct);

      List<Product> result = productService.listProductsByShop(1L);

      assertNotNull(result);
      assertEquals(1, result.size());
      verify(productRepo, times(1)).findByShopId(1L);
    }
  }

  @Nested
  @DisplayName("listProductsByCategory Tests")
  class ListProductsByCategoryTests {

    @Test
    @DisplayName("Should return products by category id")
    void shouldReturnProductsByCategory() {
      when(productRepo.findByCategoryId(1L)).thenReturn(Arrays.asList(testProductDao));
      when(productMapper.toEntityFromDao(testProductDao)).thenReturn(testProduct);

      List<Product> result = productService.listProductsByCategory(1L);

      assertNotNull(result);
      assertEquals(1, result.size());
      verify(productRepo, times(1)).findByCategoryId(1L);
    }
  }

  @Nested
  @DisplayName("listProductsByBrand Tests")
  class ListProductsByBrandTests {

    @Test
    @DisplayName("Should return products by brand id")
    void shouldReturnProductsByBrand() {
      when(productRepo.findByBrandId(1L)).thenReturn(Arrays.asList(testProductDao));
      when(productMapper.toEntityFromDao(testProductDao)).thenReturn(testProduct);

      List<Product> result = productService.listProductsByBrand(1L);

      assertNotNull(result);
      assertEquals(1, result.size());
      verify(productRepo, times(1)).findByBrandId(1L);
    }
  }

  @Nested
  @DisplayName("getProductNameByID Tests")
  class GetProductNameByIDTests {

    @Test
    @DisplayName("Should return product name by id")
    void shouldReturnProductName() {
      when(productRepo.getName(1L)).thenReturn("Test Product");

      String result = productService.getProductNameByID(1L);

      assertEquals("Test Product", result);
    }

    @Test
    @DisplayName("Should return null when product not found")
    void shouldReturnNullWhenProductNotFound() {
      when(productRepo.getName(999L)).thenReturn(null);

      String result = productService.getProductNameByID(999L);

      assertNull(result);
    }
  }

  @Nested
  @DisplayName("updateProductInventory Tests")
  class UpdateProductInventoryTests {

    @Test
    @DisplayName("Should update product inventory")
    void shouldUpdateInventory() {
      when(productRepo.existsById(1L)).thenReturn(true);
      doNothing().when(productRepo).updateInventory(1L, 100);

      productService.updateProductInventory(1L, 100);

      verify(productRepo, times(1)).existsById(1L);
      verify(productRepo, times(1)).updateInventory(1L, 100);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product does not exist")
    void shouldThrowExceptionWhenProductDoesNotExist() {
      when(productRepo.existsById(999L)).thenReturn(false);

      assertThrows(ResourceNotFoundException.class, () -> productService.updateProductInventory(999L, 100));
      verify(productRepo, times(1)).existsById(999L);
      verify(productRepo, never()).updateInventory(any(), anyInt());
    }
  }
}
