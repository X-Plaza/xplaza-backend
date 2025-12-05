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
import com.xplaza.backend.geography.domain.entity.State;
import com.xplaza.backend.geography.repository.CountryRepository;
import com.xplaza.backend.geography.repository.StateRepository;

@Service
@RequiredArgsConstructor
public class StateService {
  private final StateRepository stateRepository;
  private final CountryRepository countryRepository;

  public List<State> listStates() {
    return stateRepository.findAll();
  }

  public List<State> listStatesByCountry(Long countryId) {
    return stateRepository.findByCountryCountryId(countryId);
  }

  public State getState(Long id) {
    return stateRepository.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("State not found with id: " + id));
  }

  @Transactional
  public State createState(State state, Long countryId) {
    Country country = countryRepository.findById(countryId)
        .orElseThrow(() -> new ResourceNotFoundException("Country not found with id: " + countryId));
    state.setCountry(country);
    return stateRepository.save(state);
  }

  @Transactional
  public State updateState(Long id, State stateDetails) {
    State state = getState(id);
    state.setStateName(stateDetails.getStateName());
    if (stateDetails.getCountry() != null && stateDetails.getCountry().getCountryId() != null) {
      Country country = countryRepository.findById(stateDetails.getCountry().getCountryId())
          .orElseThrow(() -> new ResourceNotFoundException("Country not found"));
      state.setCountry(country);
    }
    return stateRepository.save(state);
  }

  @Transactional
  public void deleteState(Long id) {
    if (!stateRepository.existsById(id)) {
      throw new ResourceNotFoundException("State not found with id: " + id);
    }
    stateRepository.deleteById(id);
  }
}
