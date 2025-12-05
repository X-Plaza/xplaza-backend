/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.delivery.service;

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

import com.xplaza.backend.delivery.domain.entity.Day;
import com.xplaza.backend.delivery.repository.DayRepository;
import com.xplaza.backend.exception.ResourceNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("DayService Unit Tests")
class DayServiceTest {

  @Mock
  private DayRepository dayRepository;

  @InjectMocks
  private DayService dayService;

  private Day testDay;

  @BeforeEach
  void setUp() {
    testDay = new Day();
    testDay.setDayId(1L);
    testDay.setDayName("Monday");
    testDay.setDayNumber(1);
  }

  @Nested
  @DisplayName("listDays Tests")
  class ListDaysTests {

    @Test
    @DisplayName("Should return list of all days")
    void shouldReturnAllDays() {
      Day tuesday = new Day();
      tuesday.setDayId(2L);
      tuesday.setDayName("Tuesday");
      tuesday.setDayNumber(2);

      when(dayRepository.findAll()).thenReturn(Arrays.asList(testDay, tuesday));

      List<Day> result = dayService.listDays();

      assertNotNull(result);
      assertEquals(2, result.size());
      verify(dayRepository, times(1)).findAll();
    }
  }

  @Nested
  @DisplayName("getDay Tests")
  class GetDayTests {

    @Test
    @DisplayName("Should return day by id")
    void shouldReturnDayById() {
      when(dayRepository.findById(1L)).thenReturn(Optional.of(testDay));

      Day result = dayService.getDay(1L);

      assertNotNull(result);
      assertEquals(1L, result.getDayId());
      assertEquals("Monday", result.getDayName());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when day not found")
    void shouldThrowExceptionWhenDayNotFound() {
      when(dayRepository.findById(999L)).thenReturn(Optional.empty());

      assertThrows(ResourceNotFoundException.class, () -> dayService.getDay(999L));
    }
  }

  @Nested
  @DisplayName("createDay Tests")
  class CreateDayTests {

    @Test
    @DisplayName("Should successfully create a new day")
    void shouldCreateDay() {
      when(dayRepository.save(testDay)).thenReturn(testDay);

      Day result = dayService.createDay(testDay);

      assertNotNull(result);
      assertEquals("Monday", result.getDayName());
      verify(dayRepository, times(1)).save(testDay);
    }
  }

  @Nested
  @DisplayName("updateDay Tests")
  class UpdateDayTests {

    @Test
    @DisplayName("Should successfully update an existing day")
    void shouldUpdateDay() {
      Day updatedDetails = new Day();
      updatedDetails.setDayName("Updated Monday");
      updatedDetails.setDayNumber(1);

      when(dayRepository.findById(1L)).thenReturn(Optional.of(testDay));
      when(dayRepository.save(any(Day.class))).thenReturn(testDay);

      Day result = dayService.updateDay(1L, updatedDetails);

      assertNotNull(result);
      verify(dayRepository, times(1)).findById(1L);
      verify(dayRepository, times(1)).save(any(Day.class));
    }
  }

  @Nested
  @DisplayName("deleteDay Tests")
  class DeleteDayTests {

    @Test
    @DisplayName("Should delete day by id")
    void shouldDeleteDay() {
      when(dayRepository.existsById(1L)).thenReturn(true);
      doNothing().when(dayRepository).deleteById(1L);

      dayService.deleteDay(1L);

      verify(dayRepository, times(1)).existsById(1L);
      verify(dayRepository, times(1)).deleteById(1L);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when day does not exist")
    void shouldThrowExceptionWhenDayDoesNotExist() {
      when(dayRepository.existsById(999L)).thenReturn(false);

      assertThrows(ResourceNotFoundException.class, () -> dayService.deleteDay(999L));
    }
  }
}
