/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller.v1;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.common.util.ApiResponse.PageMeta;
import com.xplaza.backend.domain.State;
import com.xplaza.backend.http.dto.request.StateRequest;
import com.xplaza.backend.http.dto.response.StateResponse;
import com.xplaza.backend.mapper.StateMapper;
import com.xplaza.backend.service.StateService;

/**
 * State Controller - Clean REST API design.
 */
@RestController
@RequestMapping("/api/v1/states")
@RequiredArgsConstructor
@Validated
@Tag(name = "State Management", description = "APIs for managing states")
public class StateController {

  private final StateService stateService;
  private final StateMapper stateMapper;

  @GetMapping
  @Operation(summary = "List states", description = "Get paginated list of states with optional search")
  public ResponseEntity<ApiResponse<List<StateResponse>>> getStates(
      @RequestParam(required = false) Long countryId,
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "50") @Min(1) int size,
      @RequestParam(defaultValue = "stateName") String sort,
      @RequestParam(defaultValue = "ASC") Sort.Direction direction) {

    size = Math.min(size, 500);
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

    List<State> allStates = stateService.listStates();

    // Filter by search
    if (search != null && !search.isBlank()) {
      String searchLower = search.toLowerCase().trim();
      allStates = allStates.stream()
          .filter(s -> s.getStateName().toLowerCase().contains(searchLower))
          .toList();
    }

    // Paginate
    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), allStates.size());
    List<State> pageContent = start < allStates.size() ? allStates.subList(start, end) : List.of();

    List<StateResponse> dtos = pageContent.stream()
        .map(stateMapper::toResponse)
        .toList();

    Page<StateResponse> responsePage = new PageImpl<>(dtos, pageable, allStates.size());
    PageMeta pageMeta = PageMeta.from(responsePage);

    return ResponseEntity.ok(ApiResponse.ok(dtos, pageMeta));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get state by ID", description = "Retrieve a specific state")
  public ResponseEntity<ApiResponse<StateResponse>> getState(@PathVariable @Positive Long id) {
    State state = stateService.listState(id);
    StateResponse dto = stateMapper.toResponse(state);
    return ResponseEntity.ok(ApiResponse.ok(dto));
  }

  @PostMapping
  @Operation(summary = "Create state", description = "Create a new state")
  public ResponseEntity<ApiResponse<Void>> createState(@RequestBody @Valid StateRequest request) {
    State entity = stateMapper.toEntity(request);
    stateService.addState(entity);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("State created successfully"));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update state", description = "Update an existing state")
  public ResponseEntity<ApiResponse<Void>> updateState(
      @PathVariable @Positive Long id,
      @RequestBody @Valid StateRequest request) {
    State entity = stateMapper.toEntity(request);
    entity.setStateId(id);
    stateService.updateState(entity);
    return ResponseEntity.ok(ApiResponse.ok("State updated successfully"));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete state", description = "Delete a state")
  public ResponseEntity<ApiResponse<Void>> deleteState(@PathVariable @Positive Long id) {
    stateService.deleteState(id);
    return ResponseEntity.ok(ApiResponse.ok("State deleted successfully"));
  }
}
