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
import com.xplaza.backend.domain.City;
import com.xplaza.backend.http.dto.request.CityRequest;
import com.xplaza.backend.http.dto.response.CityResponse;
import com.xplaza.backend.mapper.CityMapper;
import com.xplaza.backend.service.CityService;

/**
 * City Controller - Clean REST API design.
 */
@RestController
@RequestMapping("/api/v1/cities")
@RequiredArgsConstructor
@Validated
@Tag(name = "City Management", description = "APIs for managing cities")
public class CityController {

  private final CityService cityService;
  private final CityMapper cityMapper;

  @GetMapping
  @Operation(summary = "List cities", description = "Get paginated list of cities with optional search")
  public ResponseEntity<ApiResponse<List<CityResponse>>> getCities(
      @RequestParam(required = false) Long stateId,
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "50") @Min(1) int size,
      @RequestParam(defaultValue = "cityName") String sort,
      @RequestParam(defaultValue = "ASC") Sort.Direction direction) {

    size = Math.min(size, 500);
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

    List<City> allCities = cityService.listCities();

    // Filter by search
    if (search != null && !search.isBlank()) {
      String searchLower = search.toLowerCase().trim();
      allCities = allCities.stream()
          .filter(c -> c.getCityName().toLowerCase().contains(searchLower))
          .toList();
    }

    // Paginate
    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), allCities.size());
    List<City> pageContent = start < allCities.size() ? allCities.subList(start, end) : List.of();

    List<CityResponse> dtos = pageContent.stream()
        .map(cityMapper::toResponse)
        .toList();

    Page<CityResponse> responsePage = new PageImpl<>(dtos, pageable, allCities.size());
    PageMeta pageMeta = PageMeta.from(responsePage);

    return ResponseEntity.ok(ApiResponse.ok(dtos, pageMeta));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get city by ID", description = "Retrieve a specific city")
  public ResponseEntity<ApiResponse<CityResponse>> getCity(@PathVariable @Positive Long id) {
    City city = cityService.listCity(id);
    CityResponse dto = cityMapper.toResponse(city);
    return ResponseEntity.ok(ApiResponse.ok(dto));
  }

  @PostMapping
  @Operation(summary = "Create city", description = "Create a new city")
  public ResponseEntity<ApiResponse<CityResponse>> createCity(@RequestBody @Valid CityRequest request) {
    City entity = cityMapper.toEntity(request);
    City saved = cityService.addCity(entity);
    CityResponse dto = cityMapper.toResponse(saved);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(dto));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update city", description = "Update an existing city")
  public ResponseEntity<ApiResponse<CityResponse>> updateCity(
      @PathVariable @Positive Long id,
      @RequestBody @Valid CityRequest request) {
    City entity = cityMapper.toEntity(request);
    entity.setCityId(id);
    City updated = cityService.updateCity(entity);
    CityResponse dto = cityMapper.toResponse(updated);
    return ResponseEntity.ok(ApiResponse.ok(dto));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete city", description = "Delete a city")
  public ResponseEntity<ApiResponse<Void>> deleteCity(@PathVariable @Positive Long id) {
    cityService.deleteCity(id);
    return ResponseEntity.ok(ApiResponse.ok("City deleted successfully"));
  }
}
