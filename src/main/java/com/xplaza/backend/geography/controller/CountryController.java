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
import com.xplaza.backend.geography.domain.entity.Country;
import com.xplaza.backend.geography.dto.request.CountryRequest;
import com.xplaza.backend.geography.dto.response.CountryResponse;
import com.xplaza.backend.geography.mapper.CountryMapper;
import com.xplaza.backend.geography.service.CountryService;

@RestController
@RequestMapping("/api/v1/countries")
@RequiredArgsConstructor
@Tag(name = "Country Management", description = "APIs for managing countries")
public class CountryController {
  private final CountryService countryService;
  private final CountryMapper countryMapper;

  @GetMapping
  @Operation(summary = "List all countries")
  public ResponseEntity<ApiResponse<List<CountryResponse>>> listCountries() {
    List<CountryResponse> countries = countryService.listCountries().stream()
        .map(countryMapper::toResponse)
        .toList();
    return ResponseEntity.ok(ApiResponse.ok(countries));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get country by ID")
  public ResponseEntity<ApiResponse<CountryResponse>> getCountry(@PathVariable Long id) {
    Country country = countryService.getCountry(id);
    return ResponseEntity.ok(ApiResponse.ok(countryMapper.toResponse(country)));
  }

  @PostMapping
  @Operation(summary = "Create a new country")
  public ResponseEntity<ApiResponse<CountryResponse>> createCountry(@RequestBody @Valid CountryRequest request) {
    Country country = countryMapper.toEntity(request);
    Country saved = countryService.createCountry(country);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(countryMapper.toResponse(saved)));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update an existing country")
  public ResponseEntity<ApiResponse<CountryResponse>> updateCountry(@PathVariable Long id,
      @RequestBody @Valid CountryRequest request) {
    Country countryDetails = countryMapper.toEntity(request);
    Country updated = countryService.updateCountry(id, countryDetails);
    return ResponseEntity.ok(ApiResponse.ok(countryMapper.toResponse(updated)));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a country")
  public ResponseEntity<ApiResponse<Void>> deleteCountry(@PathVariable Long id) {
    countryService.deleteCountry(id);
    return ResponseEntity.ok(ApiResponse.ok("Country deleted"));
  }
}
