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
import com.xplaza.backend.geography.domain.entity.City;
import com.xplaza.backend.geography.dto.request.CityRequest;
import com.xplaza.backend.geography.dto.response.CityResponse;
import com.xplaza.backend.geography.mapper.CityMapper;
import com.xplaza.backend.geography.service.CityService;

@RestController
@RequestMapping("/api/v1/cities")
@RequiredArgsConstructor
@Tag(name = "City Management", description = "APIs for managing cities")
public class CityController {
  private final CityService cityService;
  private final CityMapper cityMapper;

  @GetMapping
  @Operation(summary = "List all cities")
  public ResponseEntity<ApiResponse<List<CityResponse>>> listCities(
      @RequestParam(required = false) Long stateId) {
    List<City> cities = stateId != null ? cityService.listCitiesByState(stateId) : cityService.listCities();
    List<CityResponse> responses = cities.stream().map(cityMapper::toResponse).toList();
    return ResponseEntity.ok(ApiResponse.ok(responses));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get city by ID")
  public ResponseEntity<ApiResponse<CityResponse>> getCity(@PathVariable Long id) {
    City city = cityService.getCity(id);
    return ResponseEntity.ok(ApiResponse.ok(cityMapper.toResponse(city)));
  }

  @PostMapping
  @Operation(summary = "Create a new city")
  public ResponseEntity<ApiResponse<CityResponse>> createCity(@RequestBody @Valid CityRequest request) {
    City city = cityMapper.toEntity(request);
    City saved = cityService.createCity(city, request.getStateId());
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(cityMapper.toResponse(saved)));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update an existing city")
  public ResponseEntity<ApiResponse<CityResponse>> updateCity(@PathVariable Long id,
      @RequestBody @Valid CityRequest request) {
    City cityDetails = cityMapper.toEntity(request);
    City updated = cityService.updateCity(id, cityDetails);
    return ResponseEntity.ok(ApiResponse.ok(cityMapper.toResponse(updated)));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a city")
  public ResponseEntity<ApiResponse<Void>> deleteCity(@PathVariable Long id) {
    cityService.deleteCity(id);
    return ResponseEntity.ok(ApiResponse.ok("City deleted"));
  }
}
