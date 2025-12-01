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
import com.xplaza.backend.domain.Location;
import com.xplaza.backend.http.dto.response.LocationResponse;
import com.xplaza.backend.mapper.LocationMapper;
import com.xplaza.backend.service.LocationService;

/**
 * Location Controller - Clean REST API design.
 */
@RestController
@RequestMapping("/api/v1/locations")
@RequiredArgsConstructor
@Validated
@Tag(name = "Location Management", description = "APIs for managing locations")
public class LocationController {

  private final LocationService locationService;
  private final LocationMapper locationMapper;

  @GetMapping
  @Operation(summary = "List locations", description = "Get paginated list of locations")
  public ResponseEntity<ApiResponse<List<LocationResponse>>> getLocations(
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "50") @Min(1) int size,
      @RequestParam(defaultValue = "locationId") String sort,
      @RequestParam(defaultValue = "ASC") Sort.Direction direction) {

    size = Math.min(size, 200);
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

    List<Location> allLocations = locationService.listLocations();

    if (search != null && !search.isBlank()) {
      String searchLower = search.toLowerCase().trim();
      allLocations = allLocations.stream()
          .filter(l -> l.getLocationName() != null && l.getLocationName().toLowerCase().contains(searchLower))
          .toList();
    }

    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), allLocations.size());
    List<Location> pageContent = start < allLocations.size() ? allLocations.subList(start, end) : List.of();

    List<LocationResponse> dtos = pageContent.stream()
        .map(locationMapper::toResponse)
        .toList();

    Page<LocationResponse> responsePage = new PageImpl<>(dtos, pageable, allLocations.size());
    PageMeta pageMeta = PageMeta.from(responsePage);

    return ResponseEntity.ok(ApiResponse.ok(dtos, pageMeta));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get location by ID", description = "Retrieve a specific location")
  public ResponseEntity<ApiResponse<LocationResponse>> getLocation(@PathVariable @Positive Long id) {
    Location location = locationService.listLocation(id);
    LocationResponse dto = locationMapper.toResponse(location);
    return ResponseEntity.ok(ApiResponse.ok(dto));
  }

  @PostMapping
  @Operation(summary = "Create location", description = "Create a new location")
  public ResponseEntity<ApiResponse<Void>> createLocation(@RequestBody @Valid Location location) {
    locationService.addLocation(location);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Location created successfully"));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update location", description = "Update an existing location")
  public ResponseEntity<ApiResponse<Void>> updateLocation(
      @PathVariable @Positive Long id,
      @RequestBody @Valid Location location) {
    locationService.updateLocation(id, location);
    return ResponseEntity.ok(ApiResponse.ok("Location updated successfully"));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete location", description = "Delete a location")
  public ResponseEntity<ApiResponse<Void>> deleteLocation(@PathVariable @Positive Long id) {
    locationService.deleteLocation(id);
    return ResponseEntity.ok(ApiResponse.ok("Location deleted successfully"));
  }
}
