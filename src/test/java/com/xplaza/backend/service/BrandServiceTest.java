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
import com.xplaza.backend.jpa.dao.BrandDao;
import com.xplaza.backend.jpa.repository.BrandRepository;
import com.xplaza.backend.mapper.BrandMapper;
import com.xplaza.backend.service.entity.Brand;

@ExtendWith(MockitoExtension.class)
@DisplayName("BrandService Unit Tests")
class BrandServiceTest {

  @Mock
  private BrandRepository brandRepo;

  @Mock
  private BrandMapper brandMapper;

  @InjectMocks
  private BrandService brandService;

  private Brand testBrand;
  private BrandDao testBrandDao;

  @BeforeEach
  void setUp() {
    testBrand = new Brand();
    testBrand.setBrandId(1L);
    testBrand.setBrandName("Test Brand");
    testBrand.setBrandDescription("Test Description");

    testBrandDao = new BrandDao();
    testBrandDao.setBrandId(1L);
    testBrandDao.setBrandName("Test Brand");
    testBrandDao.setBrandDescription("Test Description");
  }

  @Nested
  @DisplayName("addBrand Tests")
  class AddBrandTests {

    @Test
    @DisplayName("Should successfully add a new brand")
    void shouldAddBrand() {
      when(brandMapper.toDao(testBrand)).thenReturn(testBrandDao);
      when(brandRepo.save(testBrandDao)).thenReturn(testBrandDao);
      when(brandMapper.toEntityFromDao(testBrandDao)).thenReturn(testBrand);

      Brand result = brandService.addBrand(testBrand);

      assertNotNull(result);
      assertEquals("Test Brand", result.getBrandName());
      verify(brandRepo, times(1)).save(testBrandDao);
    }
  }

  @Nested
  @DisplayName("updateBrand Tests")
  class UpdateBrandTests {

    @Test
    @DisplayName("Should successfully update an existing brand")
    void shouldUpdateBrand() {
      when(brandRepo.findById(1L)).thenReturn(Optional.of(testBrandDao));
      when(brandRepo.save(any(BrandDao.class))).thenReturn(testBrandDao);
      when(brandMapper.toEntityFromDao(testBrandDao)).thenReturn(testBrand);

      Brand result = brandService.updateBrand(testBrand);

      assertNotNull(result);
      assertEquals("Test Brand", result.getBrandName());
      verify(brandRepo, times(1)).findById(1L);
      verify(brandRepo, times(1)).save(any(BrandDao.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when brand not found")
    void shouldThrowExceptionWhenBrandNotFound() {
      when(brandRepo.findById(1L)).thenReturn(Optional.empty());

      assertThrows(ResourceNotFoundException.class, () -> brandService.updateBrand(testBrand));
      verify(brandRepo, times(1)).findById(1L);
      verify(brandRepo, never()).save(any());
    }
  }

  @Nested
  @DisplayName("deleteBrand Tests")
  class DeleteBrandTests {

    @Test
    @DisplayName("Should delete brand by id")
    void shouldDeleteBrand() {
      when(brandRepo.existsById(1L)).thenReturn(true);
      doNothing().when(brandRepo).deleteById(1L);

      brandService.deleteBrand(1L);

      verify(brandRepo, times(1)).existsById(1L);
      verify(brandRepo, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when brand does not exist")
    void shouldThrowExceptionWhenBrandDoesNotExist() {
      when(brandRepo.existsById(999L)).thenReturn(false);

      assertThrows(ResourceNotFoundException.class, () -> brandService.deleteBrand(999L));
      verify(brandRepo, times(1)).existsById(999L);
      verify(brandRepo, never()).deleteById(any());
    }
  }

  @Nested
  @DisplayName("listBrands Tests")
  class ListBrandsTests {

    @Test
    @DisplayName("Should return list of all brands")
    void shouldReturnAllBrands() {
      BrandDao secondBrandDao = new BrandDao();
      secondBrandDao.setBrandId(2L);
      secondBrandDao.setBrandName("Second Brand");

      Brand secondBrand = new Brand();
      secondBrand.setBrandId(2L);
      secondBrand.setBrandName("Second Brand");

      when(brandRepo.findAll()).thenReturn(Arrays.asList(testBrandDao, secondBrandDao));
      when(brandMapper.toEntityFromDao(testBrandDao)).thenReturn(testBrand);
      when(brandMapper.toEntityFromDao(secondBrandDao)).thenReturn(secondBrand);

      List<Brand> result = brandService.listBrands();

      assertNotNull(result);
      assertEquals(2, result.size());
      verify(brandRepo, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return empty list when no brands exist")
    void shouldReturnEmptyList() {
      when(brandRepo.findAll()).thenReturn(Arrays.asList());

      List<Brand> result = brandService.listBrands();

      assertNotNull(result);
      assertTrue(result.isEmpty());
    }
  }

  @Nested
  @DisplayName("listBrand Tests")
  class ListBrandTests {

    @Test
    @DisplayName("Should return brand by id")
    void shouldReturnBrandById() {
      when(brandRepo.findById(1L)).thenReturn(Optional.of(testBrandDao));
      when(brandMapper.toEntityFromDao(testBrandDao)).thenReturn(testBrand);

      Brand result = brandService.listBrand(1L);

      assertNotNull(result);
      assertEquals(1L, result.getBrandId());
      assertEquals("Test Brand", result.getBrandName());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when brand not found")
    void shouldThrowExceptionWhenBrandNotFound() {
      when(brandRepo.findById(999L)).thenReturn(Optional.empty());

      assertThrows(ResourceNotFoundException.class, () -> brandService.listBrand(999L));
    }
  }

  @Nested
  @DisplayName("getBrandNameByID Tests")
  class GetBrandNameByIDTests {

    @Test
    @DisplayName("Should return brand name by id")
    void shouldReturnBrandName() {
      when(brandRepo.getName(1L)).thenReturn("Test Brand");

      String result = brandService.getBrandNameByID(1L);

      assertEquals("Test Brand", result);
    }
  }

  @Nested
  @DisplayName("isExist Tests")
  class IsExistTests {

    @Test
    @DisplayName("Should return true when brand exists")
    void shouldReturnTrueWhenBrandExists() {
      when(brandMapper.toDao(testBrand)).thenReturn(testBrandDao);
      when(brandRepo.existsByName("Test Brand")).thenReturn(true);

      boolean result = brandService.isExist(testBrand);

      assertTrue(result);
    }

    @Test
    @DisplayName("Should return false when brand does not exist")
    void shouldReturnFalseWhenBrandDoesNotExist() {
      when(brandMapper.toDao(testBrand)).thenReturn(testBrandDao);
      when(brandRepo.existsByName("Test Brand")).thenReturn(false);

      boolean result = brandService.isExist(testBrand);

      assertFalse(result);
    }
  }
}
