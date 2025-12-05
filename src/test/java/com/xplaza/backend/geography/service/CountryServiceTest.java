/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.geography.service;

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
import com.xplaza.backend.geography.domain.entity.Country;
import com.xplaza.backend.geography.repository.CountryRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("CountryService Unit Tests")
class CountryServiceTest {

  @Mock
  private CountryRepository countryRepository;

  @InjectMocks
  private CountryService countryService;

  private Country testCountry;

  @BeforeEach
  void setUp() {
    testCountry = new Country();
    testCountry.setCountryId(1L);
    testCountry.setCountryName("Test Country");
    testCountry.setCountryCode("TC");
  }

  @Nested
  @DisplayName("listCountries Tests")
  class ListCountriesTests {

    @Test
    @DisplayName("Should return list of all countries")
    void shouldReturnAllCountries() {
      Country secondCountry = new Country();
      secondCountry.setCountryId(2L);
      secondCountry.setCountryName("Second Country");

      when(countryRepository.findAll()).thenReturn(Arrays.asList(testCountry, secondCountry));

      List<Country> result = countryService.listCountries();

      assertNotNull(result);
      assertEquals(2, result.size());
      verify(countryRepository, times(1)).findAll();
    }
  }

  @Nested
  @DisplayName("getCountry Tests")
  class GetCountryTests {

    @Test
    @DisplayName("Should return country by id")
    void shouldReturnCountryById() {
      when(countryRepository.findById(1L)).thenReturn(Optional.of(testCountry));

      Country result = countryService.getCountry(1L);

      assertNotNull(result);
      assertEquals(1L, result.getCountryId());
      assertEquals("Test Country", result.getCountryName());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when country not found")
    void shouldThrowExceptionWhenCountryNotFound() {
      when(countryRepository.findById(999L)).thenReturn(Optional.empty());

      assertThrows(ResourceNotFoundException.class, () -> countryService.getCountry(999L));
    }
  }

  @Nested
  @DisplayName("createCountry Tests")
  class CreateCountryTests {

    @Test
    @DisplayName("Should successfully create a new country")
    void shouldCreateCountry() {
      when(countryRepository.save(testCountry)).thenReturn(testCountry);

      Country result = countryService.createCountry(testCountry);

      assertNotNull(result);
      assertEquals("Test Country", result.getCountryName());
      verify(countryRepository, times(1)).save(testCountry);
    }
  }

  @Nested
  @DisplayName("updateCountry Tests")
  class UpdateCountryTests {

    @Test
    @DisplayName("Should successfully update an existing country")
    void shouldUpdateCountry() {
      Country updatedDetails = new Country();
      updatedDetails.setCountryName("Updated Country");
      updatedDetails.setCountryCode("UC");

      when(countryRepository.findById(1L)).thenReturn(Optional.of(testCountry));
      when(countryRepository.save(any(Country.class))).thenReturn(testCountry);

      Country result = countryService.updateCountry(1L, updatedDetails);

      assertNotNull(result);
      verify(countryRepository, times(1)).findById(1L);
      verify(countryRepository, times(1)).save(any(Country.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when country not found")
    void shouldThrowExceptionWhenCountryNotFound() {
      when(countryRepository.findById(999L)).thenReturn(Optional.empty());

      assertThrows(ResourceNotFoundException.class, () -> countryService.updateCountry(999L, testCountry));
      verify(countryRepository, never()).save(any());
    }
  }

  @Nested
  @DisplayName("deleteCountry Tests")
  class DeleteCountryTests {

    @Test
    @DisplayName("Should delete country by id")
    void shouldDeleteCountry() {
      when(countryRepository.existsById(1L)).thenReturn(true);
      doNothing().when(countryRepository).deleteById(1L);

      countryService.deleteCountry(1L);

      verify(countryRepository, times(1)).existsById(1L);
      verify(countryRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when country does not exist")
    void shouldThrowExceptionWhenCountryDoesNotExist() {
      when(countryRepository.existsById(999L)).thenReturn(false);

      assertThrows(ResourceNotFoundException.class, () -> countryService.deleteCountry(999L));
      verify(countryRepository, never()).deleteById(any());
    }
  }
}
