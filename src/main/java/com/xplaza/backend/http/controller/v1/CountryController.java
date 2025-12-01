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
import com.xplaza.backend.domain.Country;
import com.xplaza.backend.http.dto.request.CountryRequest;
import com.xplaza.backend.http.dto.response.CountryResponse;
import com.xplaza.backend.mapper.CountryMapper;
import com.xplaza.backend.service.CountryService;

/**
 * Country Controller - Clean REST API design for geographic reference data.
 */
@RestController
@RequestMapping("/api/v1/countries")
@RequiredArgsConstructor
@Validated
@Tag(name = "Country Management", description = "APIs for managing countries with pagination")
public class CountryController {

  private final CountryService countryService;
  private final CountryMapper countryMapper;

  @GetMapping
  @Operation(summary = "List countries", description = "Get paginated list of countries with optional search")
  public ResponseEntity<ApiResponse<List<CountryResponse>>> getCountries(
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "50") @Min(1) int size,
      @RequestParam(defaultValue = "countryName") String sort,
      @RequestParam(defaultValue = "ASC") Sort.Direction direction) {

    size = Math.min(size, 300); // Countries are reference data, allow larger page sizes
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

    // For now, use in-memory pagination since CountryService doesn't have paginated
    // methods
    List<Country> allCountries = countryService.listCountries();

    // Filter by search if provided
    if (search != null && !search.isBlank()) {
      String searchLower = search.toLowerCase().trim();
      allCountries = allCountries.stream()
          .filter(c -> c.getCountryName().toLowerCase().contains(searchLower) ||
              (c.getIso() != null && c.getIso().toLowerCase().contains(searchLower)))
          .toList();
    }

    // Paginate
    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), allCountries.size());
    List<Country> pageContent = start < allCountries.size() ? allCountries.subList(start, end) : List.of();

    List<CountryResponse> dtos = pageContent.stream()
        .map(countryMapper::toResponse)
        .toList();

    Page<CountryResponse> responsePage = new PageImpl<>(dtos, pageable, allCountries.size());
    PageMeta pageMeta = PageMeta.from(responsePage);

    return ResponseEntity.ok(ApiResponse.ok(dtos, pageMeta));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get country by ID", description = "Retrieve a specific country by its ID")
  public ResponseEntity<ApiResponse<CountryResponse>> getCountry(@PathVariable @Positive Long id) {
    Country country = countryService.listCountry(id);
    CountryResponse dto = countryMapper.toResponse(country);
    return ResponseEntity.ok(ApiResponse.ok(dto));
  }

  @PostMapping
  @Operation(summary = "Create country", description = "Create a new country")
  public ResponseEntity<ApiResponse<CountryResponse>> createCountry(
      @RequestBody @Valid CountryRequest request) {
    Country entity = countryMapper.toEntity(request);
    Country saved = countryService.addCountry(entity);
    CountryResponse dto = countryMapper.toResponse(saved);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.created(dto));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update country", description = "Update an existing country by ID")
  public ResponseEntity<ApiResponse<CountryResponse>> updateCountry(
      @PathVariable @Positive Long id,
      @RequestBody @Valid CountryRequest request) {
    Country entity = countryMapper.toEntity(request);
    entity.setCountryId(id);
    Country updated = countryService.updateCountry(entity);
    CountryResponse dto = countryMapper.toResponse(updated);
    return ResponseEntity.ok(ApiResponse.ok(dto));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete country", description = "Delete a country by ID")
  public ResponseEntity<ApiResponse<Void>> deleteCountry(@PathVariable @Positive Long id) {
    countryService.deleteCountry(id);
    return ResponseEntity.ok(ApiResponse.ok("Country has been deleted"));
  }
}
