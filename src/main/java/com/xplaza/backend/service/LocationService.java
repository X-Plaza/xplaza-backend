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

import com.xplaza.backend.domain.Location;
import com.xplaza.backend.exception.ResourceNotFoundException;
import com.xplaza.backend.jpa.dao.LocationDao;
import com.xplaza.backend.jpa.repository.LocationRepository;
import com.xplaza.backend.mapper.LocationMapper;

@Service
@RequiredArgsConstructor
public class LocationService {
  private final LocationRepository locationRepo;
  private final LocationMapper locationMapper;

  @Transactional
  public Location addLocation(Location location) {
    LocationDao locationDao = locationMapper.toDao(location);
    LocationDao savedLocationDao = locationRepo.save(locationDao);
    return locationMapper.toEntityFromDao(savedLocationDao);
  }

  @Transactional
  public Location updateLocation(Long id, Location location) {
    // Check if location exists
    locationRepo.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));

    location.setLocationId(id);
    LocationDao locationDao = locationMapper.toDao(location);
    LocationDao updatedLocationDao = locationRepo.save(locationDao);
    return locationMapper.toEntityFromDao(updatedLocationDao);
  }

  @Transactional
  public void deleteLocation(Long id) {
    locationRepo.deleteById(id);
  }

  public List<Location> listLocations() {
    List<LocationDao> locationDaos = locationRepo.findAll();
    return locationDaos.stream()
        .map(locationMapper::toEntityFromDao)
        .collect(Collectors.toList());
  }

  public Location listLocation(Long id) {
    LocationDao locationDao = locationRepo.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));
    return locationMapper.toEntityFromDao(locationDao);
  }

  public List<Location> listLocationsByCity(Long cityId) {
    List<LocationDao> locationDaos = locationRepo.findByCityId(cityId);
    return locationDaos.stream()
        .map(locationMapper::toEntityFromDao)
        .collect(Collectors.toList());
  }

  public List<Location> listLocationsByState(Long stateId) {
    List<LocationDao> locationDaos = locationRepo.findByStateId(stateId);
    return locationDaos.stream()
        .map(locationMapper::toEntityFromDao)
        .collect(Collectors.toList());
  }

  public List<Location> listLocationsByCountry(Long countryId) {
    List<LocationDao> locationDaos = locationRepo.findByCountryId(countryId);
    return locationDaos.stream()
        .map(locationMapper::toEntityFromDao)
        .collect(Collectors.toList());
  }
}
