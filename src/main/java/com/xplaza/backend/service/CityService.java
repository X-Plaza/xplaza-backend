/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.service;

import java.util.List;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.domain.City;
import com.xplaza.backend.exception.ResourceNotFoundException;
import com.xplaza.backend.jpa.dao.CityDao;
import com.xplaza.backend.jpa.repository.CityRepository;
import com.xplaza.backend.mapper.CityMapper;
import com.xplaza.backend.mapper.LocationMapper;

@Service
@RequiredArgsConstructor
public class CityService {
  private final CityRepository cityRepo;
  private final CityMapper cityMapper;
  private final LocationMapper locationMapper;

  @Transactional
  public City addCity(City city) {
    CityDao cityDao = cityMapper.toDao(city);
    CityDao savedCityDao = cityRepo.save(cityDao);
    return cityMapper.toEntityFromDao(savedCityDao);
  }

  @Transactional
  public City updateCity(City city) {
    CityDao existingCityDao = cityRepo.findById(city.getCityId())
        .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + city.getCityId()));
    // Update the existing city with new values
    existingCityDao.setCityName(city.getCityName());
    existingCityDao.setLocations(city.getLocations().stream().map(locationMapper::toDao).toList());
    CityDao updatedCityDao = cityRepo.save(existingCityDao);
    return cityMapper.toEntityFromDao(updatedCityDao);
  }

  @Transactional
  public void deleteCity(Long id) {
    cityRepo.deleteById(id);
  }

  public List<City> listCities() {
    List<CityDao> cityDaos = cityRepo.findAll();
    return cityDaos.stream()
        .map(cityMapper::toEntityFromDao)
        .collect(Collectors.toList());
  }

  public City listCity(Long id) {
    CityDao cityDao = cityRepo.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + id));
    return cityMapper.toEntityFromDao(cityDao);
  }

  public List<City> listCitiesByState(Long stateId) {
    List<CityDao> cityDaos = cityRepo.findByStateId(stateId);
    return cityDaos.stream()
        .map(cityMapper::toEntityFromDao)
        .collect(Collectors.toList());
  }

  public List<City> listCitiesByCountry(Long countryId) {
    List<CityDao> cityDaos = cityRepo.findByCountryId(countryId);
    return cityDaos.stream()
        .map(cityMapper::toEntityFromDao)
        .collect(Collectors.toList());
  }
}
