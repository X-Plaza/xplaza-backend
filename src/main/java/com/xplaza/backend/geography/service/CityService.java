/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.geography.service;

import java.util.List;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.exception.ResourceNotFoundException;
import com.xplaza.backend.geography.domain.entity.City;
import com.xplaza.backend.geography.domain.entity.State;
import com.xplaza.backend.geography.repository.CityRepository;
import com.xplaza.backend.geography.repository.StateRepository;

@Service
@RequiredArgsConstructor
public class CityService {
  private final CityRepository cityRepository;
  private final StateRepository stateRepository;

  public List<City> listCities() {
    return cityRepository.findAll();
  }

  public List<City> listCitiesByState(Long stateId) {
    return cityRepository.findByStateStateId(stateId);
  }

  public City getCity(Long id) {
    return cityRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + id));
  }

  @Transactional
  public City createCity(City city, Long stateId) {
    State state = stateRepository.findById(stateId)
        .orElseThrow(() -> new ResourceNotFoundException("State not found with id: " + stateId));
    city.setState(state);
    return cityRepository.save(city);
  }

  @Transactional
  public City updateCity(Long id, City cityDetails) {
    City city = getCity(id);
    city.setCityName(cityDetails.getCityName());
    if (cityDetails.getState() != null && cityDetails.getState().getStateId() != null) {
      State state = stateRepository.findById(cityDetails.getState().getStateId())
          .orElseThrow(() -> new ResourceNotFoundException("State not found"));
      city.setState(state);
    }
    return cityRepository.save(city);
  }

  @Transactional
  public void deleteCity(Long id) {
    if (!cityRepository.existsById(id)) {
      throw new ResourceNotFoundException("City not found with id: " + id);
    }
    cityRepository.deleteById(id);
  }
}
