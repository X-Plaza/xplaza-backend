/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.geography.controller;

import java.util.List;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.geography.domain.entity.State;
import com.xplaza.backend.geography.dto.request.StateRequest;
import com.xplaza.backend.geography.dto.response.StateResponse;
import com.xplaza.backend.geography.mapper.StateMapper;
import com.xplaza.backend.geography.service.StateService;

@RestController
@RequestMapping("/api/v1/states")
@RequiredArgsConstructor
@Tag(name = "State Management", description = "APIs for managing states")
public class StateController {
  private final StateService stateService;
  private final StateMapper stateMapper;

  @GetMapping
  @Operation(summary = "List all states")
  public ResponseEntity<ApiResponse<List<StateResponse>>> listStates(
      @RequestParam(required = false) Long countryId) {
    List<State> states = countryId != null ? stateService.listStatesByCountry(countryId)
        : stateService.listStates();
    List<StateResponse> responses = states.stream().map(stateMapper::toResponse).toList();
    return ResponseEntity.ok(ApiResponse.ok(responses));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get state by ID")
  public ResponseEntity<ApiResponse<StateResponse>> getState(@PathVariable Long id) {
    State state = stateService.getState(id);
    return ResponseEntity.ok(ApiResponse.ok(stateMapper.toResponse(state)));
  }

  @PostMapping
  @Operation(summary = "Create a new state")
  public ResponseEntity<ApiResponse<StateResponse>> createState(@RequestBody @Valid StateRequest request) {
    State state = stateMapper.toEntity(request);
    State saved = stateService.createState(state, request.getCountryId());
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(stateMapper.toResponse(saved)));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update an existing state")
  public ResponseEntity<ApiResponse<StateResponse>> updateState(@PathVariable Long id,
      @RequestBody @Valid StateRequest request) {
    State stateDetails = stateMapper.toEntity(request);
    State updated = stateService.updateState(id, stateDetails);
    return ResponseEntity.ok(ApiResponse.ok(stateMapper.toResponse(updated)));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a state")
  public ResponseEntity<ApiResponse<Void>> deleteState(@PathVariable Long id) {
    stateService.deleteState(id);
    return ResponseEntity.ok(ApiResponse.ok("State deleted"));
  }
}
