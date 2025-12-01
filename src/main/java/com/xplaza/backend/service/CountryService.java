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

import com.xplaza.backend.domain.Country;
import com.xplaza.backend.exception.ResourceNotFoundException;
import com.xplaza.backend.jpa.dao.CountryDao;
import com.xplaza.backend.jpa.repository.CountryRepository;
import com.xplaza.backend.mapper.CountryMapper;
import com.xplaza.backend.mapper.StateMapper;

@Service
@RequiredArgsConstructor
public class CountryService {
  private final CountryRepository countryRepo;
  private final CountryMapper countryMapper;
  private final StateMapper stateMapper;

  @Transactional
  public Country addCountry(Country country) {
    CountryDao countryDao = countryMapper.toDao(country);
    CountryDao savedCountryDao = countryRepo.save(countryDao);
    return countryMapper.toEntityFromDao(savedCountryDao);
  }

  @Transactional
  public Country updateCountry(Country country) {
    CountryDao existingCountryDao = countryRepo.findById(country.getCountryId())
        .orElseThrow(() -> new ResourceNotFoundException("Country not found with id: " + country.getCountryId()));
    existingCountryDao.setCountryName(country.getCountryName());
    existingCountryDao.setIso(country.getIso());
    existingCountryDao.setIso3(country.getIso3());
    existingCountryDao.setNumcode(country.getNumCode());
    existingCountryDao.setPhonecode(country.getPhoneCode());
    existingCountryDao.setNicename(country.getNiceName());
    existingCountryDao.setStates(country.getStates().stream().map(stateMapper::toDao).toList());
    CountryDao updatedCountryDao = countryRepo.save(existingCountryDao);
    return countryMapper.toEntityFromDao(updatedCountryDao);
  }

  @Transactional
  public void deleteCountry(Long id) {
    countryRepo.deleteById(id);
  }

  public List<Country> listCountries() {
    List<CountryDao> countryDaos = countryRepo.findAll();
    return countryDaos.stream()
        .map(countryMapper::toEntityFromDao)
        .collect(Collectors.toList());
  }

  public Country listCountry(Long id) {
    CountryDao countryDao = countryRepo.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("Country not found with id: " + id));
    return countryMapper.toEntityFromDao(countryDao);
  }
}
