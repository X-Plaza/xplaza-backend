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
import com.xplaza.backend.geography.domain.entity.Location;
import com.xplaza.backend.geography.dto.request.LocationRequest;
import com.xplaza.backend.geography.dto.response.LocationResponse;
import com.xplaza.backend.geography.mapper.LocationMapper;
import com.xplaza.backend.geography.service.LocationService;

@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@Tag(name = "Location Management", description = "APIs for managing locations")
public class LocationController {
  private final LocationService locationService;
  private final LocationMapper locationMapper;

  @GetMapping
  @Operation(summary = "List all locations")
  public ResponseEntity<ApiResponse<List<LocationResponse>>> listLocations(
      @RequestParam(required = false) Long cityId) {
    List<Location> locations = cityId != null ? locationService.listLocationsByCity(cityId)
        : locationService.listLocations();
    List<LocationResponse> responses = locations.stream().map(locationMapper::toResponse).toList();
    return ResponseEntity.ok(ApiResponse.ok(responses));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get location by ID")
  public ResponseEntity<ApiResponse<LocationResponse>> getLocation(@PathVariable Long id) {
    Location location = locationService.getLocation(id);
    return ResponseEntity.ok(ApiResponse.ok(locationMapper.toResponse(location)));
  }

  @PostMapping
  @Operation(summary = "Create a new location")
  public ResponseEntity<ApiResponse<LocationResponse>> createLocation(@RequestBody @Valid LocationRequest request) {
    Location location = locationMapper.toEntity(request);
    Location saved = locationService.createLocation(location, request.getCityId());
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(locationMapper.toResponse(saved)));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update an existing location")
  public ResponseEntity<ApiResponse<LocationResponse>> updateLocation(@PathVariable Long id,
      @RequestBody @Valid LocationRequest request) {
    Location locationDetails = locationMapper.toEntity(request);
    Location updated = locationService.updateLocation(id, locationDetails);
    return ResponseEntity.ok(ApiResponse.ok(locationMapper.toResponse(updated)));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a location")
  public ResponseEntity<ApiResponse<Void>> deleteLocation(@PathVariable Long id) {
    locationService.deleteLocation(id);
    return ResponseEntity.ok(ApiResponse.ok("Location deleted"));
  }
}
