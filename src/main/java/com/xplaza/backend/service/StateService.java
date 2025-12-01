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

import com.xplaza.backend.domain.State;
import com.xplaza.backend.exception.ResourceNotFoundException;
import com.xplaza.backend.jpa.dao.StateDao;
import com.xplaza.backend.jpa.repository.StateRepository;
import com.xplaza.backend.mapper.StateMapper;

@Service
@RequiredArgsConstructor
public class StateService {
  private final StateRepository stateRepo;
  private final StateMapper stateMapper;

  @Transactional
  public State addState(State state) {
    StateDao stateDao = stateMapper.toDao(state);
    StateDao savedStateDao = stateRepo.save(stateDao);
    return stateMapper.toEntityFromDao(savedStateDao);
  }

  @Transactional
  public State updateState(State state) {
    StateDao existingStateDao = stateRepo.findById(state.getStateId())
        .orElseThrow(() -> new ResourceNotFoundException("State not found with id: " + state.getStateId()));
    // Update the existing state with new values
    existingStateDao.setStateName(state.getStateName());
    StateDao updatedStateDao = stateRepo.save(existingStateDao);
    return stateMapper.toEntityFromDao(updatedStateDao);
  }

  @Transactional
  public void deleteState(Long id) {
    stateRepo.deleteById(id);
  }

  public List<State> listStates() {
    List<StateDao> stateDaos = stateRepo.findAll();
    return stateDaos.stream()
        .map(stateMapper::toEntityFromDao)
        .collect(Collectors.toList());
  }

  public State listState(Long id) {
    StateDao stateDao = stateRepo.findById(id)
        .orElseThrow(() -> new ResourceNotFoundException("State not found with id: " + id));
    return stateMapper.toEntityFromDao(stateDao);
  }

  public List<State> listStatesByCountry(Long countryId) {
    List<StateDao> stateDaos = stateRepo.findByCountryId(countryId);
    return stateDaos.stream()
        .map(stateMapper::toEntityFromDao)
        .collect(Collectors.toList());
  }
}
