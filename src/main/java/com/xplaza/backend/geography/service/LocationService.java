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
import com.xplaza.backend.geography.domain.entity.Location;
import com.xplaza.backend.geography.repository.CityRepository;
import com.xplaza.backend.geography.repository.LocationRepository;

@Service
@RequiredArgsConstructor
public class LocationService {
  private final LocationRepository locationRepository;
  private final CityRepository cityRepository;

  public List<Location> listLocations() {
    return locationRepository.findAll();
  }

  public List<Location> listLocationsByCity(Long cityId) {
    return locationRepository.findByCityCityId(cityId);
  }

  public Location getLocation(Long id) {
    return locationRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Location not found with id: " + id));
  }

  @Transactional
  public Location createLocation(Location location, Long cityId) {
    City city = cityRepository.findById(cityId)
        .orElseThrow(() -> new ResourceNotFoundException("City not found with id: " + cityId));
    location.setCity(city);
    return locationRepository.save(location);
  }

  @Transactional
  public Location updateLocation(Long id, Location locationDetails) {
    Location location = getLocation(id);
    location.setLocationName(locationDetails.getLocationName());
    location.setPostalCode(locationDetails.getPostalCode());
    location.setLatitude(locationDetails.getLatitude());
    location.setLongitude(locationDetails.getLongitude());
    if (locationDetails.getCity() != null && locationDetails.getCity().getCityId() != null) {
      City city = cityRepository.findById(locationDetails.getCity().getCityId())
          .orElseThrow(() -> new ResourceNotFoundException("City not found"));
      location.setCity(city);
    }
    return locationRepository.save(location);
  }

  @Transactional
  public void deleteLocation(Long id) {
    if (!locationRepository.existsById(id)) {
      throw new ResourceNotFoundException("Location not found with id: " + id);
    }
    locationRepository.deleteById(id);
  }
}
