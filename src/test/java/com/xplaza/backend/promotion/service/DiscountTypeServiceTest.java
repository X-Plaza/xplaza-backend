/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.promotion.service;

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
import com.xplaza.backend.promotion.domain.entity.DiscountType;
import com.xplaza.backend.promotion.repository.DiscountTypeRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("DiscountTypeService Unit Tests")
class DiscountTypeServiceTest {

  @Mock
  private DiscountTypeRepository discountTypeRepository;

  @InjectMocks
  private DiscountTypeService discountTypeService;

  private DiscountType testDiscountType;

  @BeforeEach
  void setUp() {
    testDiscountType = new DiscountType();
    testDiscountType.setDiscountTypeId(1L);
    testDiscountType.setDiscountTypeName("Percentage");
    testDiscountType.setDescription("Percentage discount");
  }

  @Nested
  @DisplayName("listDiscountTypes Tests")
  class ListDiscountTypesTests {

    @Test
    @DisplayName("Should return list of all discount types")
    void shouldReturnAllDiscountTypes() {
      when(discountTypeRepository.findAll()).thenReturn(Arrays.asList(testDiscountType));

      List<DiscountType> result = discountTypeService.listDiscountTypes();

      assertNotNull(result);
      assertEquals(1, result.size());
      verify(discountTypeRepository, times(1)).findAll();
    }
  }

  @Nested
  @DisplayName("getDiscountType Tests")
  class GetDiscountTypeTests {

    @Test
    @DisplayName("Should return discount type by id")
    void shouldReturnDiscountTypeById() {
      when(discountTypeRepository.findById(1L)).thenReturn(Optional.of(testDiscountType));

      DiscountType result = discountTypeService.getDiscountType(1L);

      assertNotNull(result);
      assertEquals(1L, result.getDiscountTypeId());
      assertEquals("Percentage", result.getDiscountTypeName());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when discount type not found")
    void shouldThrowExceptionWhenDiscountTypeNotFound() {
      when(discountTypeRepository.findById(999L)).thenReturn(Optional.empty());

      assertThrows(ResourceNotFoundException.class, () -> discountTypeService.getDiscountType(999L));
    }
  }

  @Nested
  @DisplayName("createDiscountType Tests")
  class CreateDiscountTypeTests {

    @Test
    @DisplayName("Should successfully create a new discount type")
    void shouldCreateDiscountType() {
      when(discountTypeRepository.save(testDiscountType)).thenReturn(testDiscountType);

      DiscountType result = discountTypeService.createDiscountType(testDiscountType);

      assertNotNull(result);
      assertEquals("Percentage", result.getDiscountTypeName());
      verify(discountTypeRepository, times(1)).save(testDiscountType);
    }
  }

  @Nested
  @DisplayName("updateDiscountType Tests")
  class UpdateDiscountTypeTests {

    @Test
    @DisplayName("Should successfully update an existing discount type")
    void shouldUpdateDiscountType() {
      DiscountType updatedDetails = new DiscountType();
      updatedDetails.setDiscountTypeName("Fixed Amount");
      updatedDetails.setDescription("Fixed amount discount");

      when(discountTypeRepository.findById(1L)).thenReturn(Optional.of(testDiscountType));
      when(discountTypeRepository.save(any(DiscountType.class))).thenReturn(testDiscountType);

      DiscountType result = discountTypeService.updateDiscountType(1L, updatedDetails);

      assertNotNull(result);
      verify(discountTypeRepository, times(1)).findById(1L);
      verify(discountTypeRepository, times(1)).save(any(DiscountType.class));
    }
  }

  @Nested
  @DisplayName("deleteDiscountType Tests")
  class DeleteDiscountTypeTests {

    @Test
    @DisplayName("Should delete discount type by id")
    void shouldDeleteDiscountType() {
      when(discountTypeRepository.existsById(1L)).thenReturn(true);
      doNothing().when(discountTypeRepository).deleteById(1L);

      discountTypeService.deleteDiscountType(1L);

      verify(discountTypeRepository, times(1)).existsById(1L);
      verify(discountTypeRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when discount type does not exist")
    void shouldThrowExceptionWhenDiscountTypeDoesNotExist() {
      when(discountTypeRepository.existsById(999L)).thenReturn(false);

      assertThrows(ResourceNotFoundException.class, () -> discountTypeService.deleteDiscountType(999L));
    }
  }
}
