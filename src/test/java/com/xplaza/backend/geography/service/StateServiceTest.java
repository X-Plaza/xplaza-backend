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
import com.xplaza.backend.geography.domain.entity.State;
import com.xplaza.backend.geography.repository.CountryRepository;
import com.xplaza.backend.geography.repository.StateRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("StateService Unit Tests")
class StateServiceTest {

  @Mock
  private StateRepository stateRepository;

  @Mock
  private CountryRepository countryRepository;

  @InjectMocks
  private StateService stateService;

  private State testState;
  private Country testCountry;

  @BeforeEach
  void setUp() {
    testCountry = new Country();
    testCountry.setCountryId(1L);
    testCountry.setCountryName("Test Country");

    testState = new State();
    testState.setStateId(1L);
    testState.setStateName("Test State");
    testState.setCountry(testCountry);
  }

  @Nested
  @DisplayName("listStates Tests")
  class ListStatesTests {

    @Test
    @DisplayName("Should return list of all states")
    void shouldReturnAllStates() {
      when(stateRepository.findAll()).thenReturn(Arrays.asList(testState));

      List<State> result = stateService.listStates();

      assertNotNull(result);
      assertEquals(1, result.size());
      verify(stateRepository, times(1)).findAll();
    }

    @Test
    @DisplayName("Should return states by country")
    void shouldReturnStatesByCountry() {
      when(stateRepository.findByCountryCountryId(1L)).thenReturn(Arrays.asList(testState));

      List<State> result = stateService.listStatesByCountry(1L);

      assertNotNull(result);
      assertEquals(1, result.size());
      verify(stateRepository, times(1)).findByCountryCountryId(1L);
    }
  }

  @Nested
  @DisplayName("getState Tests")
  class GetStateTests {

    @Test
    @DisplayName("Should return state by id")
    void shouldReturnStateById() {
      when(stateRepository.findById(1L)).thenReturn(Optional.of(testState));

      State result = stateService.getState(1L);

      assertNotNull(result);
      assertEquals(1L, result.getStateId());
      assertEquals("Test State", result.getStateName());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when state not found")
    void shouldThrowExceptionWhenStateNotFound() {
      when(stateRepository.findById(999L)).thenReturn(Optional.empty());

      assertThrows(ResourceNotFoundException.class, () -> stateService.getState(999L));
    }
  }

  @Nested
  @DisplayName("createState Tests")
  class CreateStateTests {

    @Test
    @DisplayName("Should successfully create a new state")
    void shouldCreateState() {
      when(countryRepository.findById(1L)).thenReturn(Optional.of(testCountry));
      when(stateRepository.save(any(State.class))).thenReturn(testState);

      State result = stateService.createState(testState, 1L);

      assertNotNull(result);
      assertEquals("Test State", result.getStateName());
      verify(stateRepository, times(1)).save(any(State.class));
    }

    @Test
    @DisplayName("Should throw exception when country not found")
    void shouldThrowExceptionWhenCountryNotFound() {
      when(countryRepository.findById(999L)).thenReturn(Optional.empty());

      assertThrows(ResourceNotFoundException.class, () -> stateService.createState(testState, 999L));
    }
  }

  @Nested
  @DisplayName("deleteState Tests")
  class DeleteStateTests {

    @Test
    @DisplayName("Should delete state by id")
    void shouldDeleteState() {
      when(stateRepository.existsById(1L)).thenReturn(true);
      doNothing().when(stateRepository).deleteById(1L);

      stateService.deleteState(1L);

      verify(stateRepository, times(1)).existsById(1L);
      verify(stateRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when state does not exist")
    void shouldThrowExceptionWhenStateDoesNotExist() {
      when(stateRepository.existsById(999L)).thenReturn(false);

      assertThrows(ResourceNotFoundException.class, () -> stateService.deleteState(999L));
    }
  }
}
