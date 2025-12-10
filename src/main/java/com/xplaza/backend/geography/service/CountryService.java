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
import com.xplaza.backend.geography.domain.entity.Country;
import com.xplaza.backend.geography.repository.CountryRepository;

@Service
@RequiredArgsConstructor
public class CountryService {
  private final CountryRepository countryRepository;

  public List<Country> listCountries() {
    return countryRepository.findAll();
  }

  public Country getCountry(Long id) {
    return countryRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Country not found with id: " + id));
  }

  @Transactional
  public Country createCountry(Country country) {
    return countryRepository.save(country);
  }

  @Transactional
  public Country updateCountry(Long id, Country countryDetails) {
    Country country = getCountry(id);
    country.setCountryName(countryDetails.getCountryName());
    country.setCountryCode(countryDetails.getCountryCode());
    return countryRepository.save(country);
  }

  @Transactional
  public void deleteCountry(Long id) {
    if (!countryRepository.existsById(id)) {
      throw new ResourceNotFoundException("Country not found with id: " + id);
    }
    countryRepository.deleteById(id);
  }
}
