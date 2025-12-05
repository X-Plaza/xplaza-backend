/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.delivery.service;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.delivery.domain.entity.Day;
import com.xplaza.backend.delivery.repository.DayRepository;
import com.xplaza.backend.exception.ResourceNotFoundException;

@Service
@RequiredArgsConstructor
public class DayService {
  private final DayRepository dayRepository;

  public List<Day> listDays() {
    return dayRepository.findAll();
  }

  public Day getDay(Long id) {
    return dayRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Day not found: " + id));
  }

  @Transactional
  public Day createDay(Day day) {
    return dayRepository.save(day);
  }

  @Transactional
  public Day updateDay(Long id, Day details) {
    Day day = getDay(id);
    day.setDayName(details.getDayName());
    day.setDayNumber(details.getDayNumber());
    return dayRepository.save(day);
  }

  @Transactional
  public void deleteDay(Long id) {
    if (!dayRepository.existsById(id)) {
      throw new ResourceNotFoundException("Day not found: " + id);
    }
    dayRepository.deleteById(id);
  }
}
