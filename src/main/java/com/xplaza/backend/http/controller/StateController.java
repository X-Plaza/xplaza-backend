/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.http.dto.request.StateRequest;
import com.xplaza.backend.http.dto.response.StateResponse;
import com.xplaza.backend.mapper.StateMapper;
import com.xplaza.backend.service.StateService;
import com.xplaza.backend.service.entity.State;

@Deprecated(since = "1.0", forRemoval = true)
@RestController
@RequestMapping("/api/v1/states")
public class StateController extends BaseController {
  private final StateService stateService;
  private final StateMapper stateMapper;

  @Autowired
  public StateController(StateService stateService, StateMapper stateMapper) {
    this.stateService = stateService;
    this.stateMapper = stateMapper;
  }

  @GetMapping
  public ResponseEntity<ApiResponse> getStates() throws Exception {
    long start = System.currentTimeMillis();
    List<State> states = stateService.listStates();
    List<StateResponse> dtos = states.stream().map(stateMapper::toResponse).toList();
    long end = System.currentTimeMillis();
    long responseTime = end - start;
    String data = new ObjectMapper().writeValueAsString(dtos);
    ApiResponse response = new ApiResponse(responseTime, "State List", HttpStatus.OK.value(), "Success", "", data);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse> getState(@PathVariable @Valid Long id) throws Exception {
    long start = System.currentTimeMillis();
    State state = stateService.listState(id);
    StateResponse dto = stateMapper.toResponse(state);
    long end = System.currentTimeMillis();
    long responseTime = end - start;
    String data = new ObjectMapper().writeValueAsString(dto);
    ApiResponse response = new ApiResponse(responseTime, "State", HttpStatus.OK.value(), "Success", "", data);
    return ResponseEntity.ok(response);
  }

  @PostMapping
  public ResponseEntity<ApiResponse> addState(@RequestBody @Valid StateRequest stateRequest) {
    State state = stateMapper.toEntity(stateRequest);
    stateService.addState(state);
    ApiResponse response = new ApiResponse(0, "Add State", HttpStatus.CREATED.value(), "Success",
        "State has been created.", null);
    return new ResponseEntity<>(response, HttpStatus.CREATED);
  }

  @PutMapping
  public ResponseEntity<ApiResponse> updateState(@RequestBody @Valid StateRequest stateRequest) {
    State state = stateMapper.toEntity(stateRequest);
    stateService.updateState(state);
    ApiResponse response = new ApiResponse(0, "Update State", HttpStatus.OK.value(), "Success",
        "State has been updated.", null);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse> deleteState(@PathVariable @Valid Long id) {
    stateService.deleteState(id);
    ApiResponse response = new ApiResponse(0, "Delete State", HttpStatus.OK.value(), "Success",
        "State has been deleted.", null);
    return ResponseEntity.ok(response);
  }
}
