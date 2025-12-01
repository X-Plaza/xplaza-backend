/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller;

import java.util.List;
import java.util.stream.Collectors;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.http.dto.request.CountryRequest;
import com.xplaza.backend.http.dto.response.CountryResponse;
import com.xplaza.backend.mapper.CountryMapper;
import com.xplaza.backend.service.CountryService;
import com.xplaza.backend.service.entity.Country;

@Deprecated(since = "1.0", forRemoval = true)
@RestController
@RequestMapping("/api/v1/countries")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Country Management [DEPRECATED]", description = "APIs for managing countries. Use V2 API instead.")
public class CountryController extends BaseController {
  private final CountryService countryService;
  private final CountryMapper countryMapper;
  private final ObjectMapper objectMapper;

  @GetMapping
  @Operation(summary = "Get all countries", description = "Retrieves a list of all countries in the system", responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved countries", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ApiResponse> getAllCountries() {
    try {
      List<Country> countries = countryService.listCountries();
      List<CountryResponse> responses = countries.stream()
          .map(countryMapper::toResponse)
          .collect(Collectors.toList());

      ApiResponse response = new ApiResponse(
          System.currentTimeMillis(),
          "success",
          200,
          "Countries retrieved successfully",
          "Countries retrieved successfully",
          objectMapper.writeValueAsString(responses));

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error retrieving countries", e);
      ApiResponse response = new ApiResponse(
          System.currentTimeMillis(),
          "error",
          500,
          "Failed to retrieve countries",
          "Failed to retrieve countries: " + e.getMessage(),
          null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get country by ID", description = "Retrieves a specific country by their ID", parameters = {
      @Parameter(name = "id", description = "Country ID", required = true, example = "1")
  }, responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved country", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Country not found"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ApiResponse> getCountryById(@PathVariable Long id) {
    try {
      Country country = countryService.listCountry(id);
      CountryResponse response = countryMapper.toResponse(country);

      ApiResponse apiResponse = new ApiResponse(
          System.currentTimeMillis(),
          "success",
          200,
          "Country retrieved successfully",
          "Country retrieved successfully",
          objectMapper.writeValueAsString(response));

      return ResponseEntity.ok(apiResponse);
    } catch (Exception e) {
      log.error("Error retrieving country with id: {}", id, e);
      ApiResponse response = new ApiResponse(
          System.currentTimeMillis(),
          "error",
          500,
          "Failed to retrieve country",
          "Failed to retrieve country: " + e.getMessage(),
          null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @PostMapping
  @Operation(summary = "Create new country", description = "Creates a new country in the system", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Country data", required = true, content = @Content(schema = @Schema(implementation = CountryRequest.class))), responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Country created successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ApiResponse> createCountry(@Valid @RequestBody CountryRequest request) {
    try {
      Country country = countryMapper.toEntity(request);
      Country savedCountry = countryService.addCountry(country);
      CountryResponse response = countryMapper.toResponse(savedCountry);

      ApiResponse apiResponse = new ApiResponse(
          System.currentTimeMillis(),
          "success",
          201,
          "Country created successfully",
          "Country created successfully",
          objectMapper.writeValueAsString(response));

      return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    } catch (Exception e) {
      log.error("Error creating country", e);
      ApiResponse response = new ApiResponse(
          System.currentTimeMillis(),
          "error",
          500,
          "Failed to create country",
          "Failed to create country: " + e.getMessage(),
          null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update country", description = "Updates an existing country by ID", parameters = {
      @Parameter(name = "id", description = "Country ID", required = true, example = "1")
  }, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated country data", required = true, content = @Content(schema = @Schema(implementation = CountryRequest.class))), responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Country updated successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Country not found"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ApiResponse> updateCountry(@PathVariable Long id, @Valid @RequestBody CountryRequest request) {
    try {
      Country country = countryMapper.toEntity(request);
      country.setCountryId(id);
      Country updatedCountry = countryService.updateCountry(country);
      CountryResponse response = countryMapper.toResponse(updatedCountry);

      ApiResponse apiResponse = new ApiResponse(
          System.currentTimeMillis(),
          "success",
          200,
          "Country updated successfully",
          "Country updated successfully",
          objectMapper.writeValueAsString(response));

      return ResponseEntity.ok(apiResponse);
    } catch (Exception e) {
      log.error("Error updating country with id: {}", id, e);
      ApiResponse response = new ApiResponse(
          System.currentTimeMillis(),
          "error",
          500,
          "Failed to update country",
          "Failed to update country: " + e.getMessage(),
          null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete country", description = "Deletes a country by ID", parameters = {
      @Parameter(name = "id", description = "Country ID", required = true, example = "1")
  }, responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Country deleted successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Country not found"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ApiResponse> deleteCountry(@PathVariable Long id) {
    try {
      countryService.deleteCountry(id);

      ApiResponse response = new ApiResponse(
          System.currentTimeMillis(),
          "success",
          200,
          "Country deleted successfully",
          "Country deleted successfully",
          null);

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error deleting country with id: {}", id, e);
      ApiResponse response = new ApiResponse(
          System.currentTimeMillis(),
          "error",
          500,
          "Failed to delete country",
          "Failed to delete country: " + e.getMessage(),
          null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  // Keep existing methods for backward compatibility
  public ResponseEntity<ApiResponse> getCountries() throws Exception {
    List<Country> countries = countryService.listCountries();
    List<CountryResponse> countryResponses = countries.stream().map(countryMapper::toResponse).toList();
    ApiResponse response = new ApiResponse(System.currentTimeMillis(), "success", 200,
        "Countries retrieved successfully", "Countries retrieved successfully",
        objectMapper.writeValueAsString(countryResponses));
    return ResponseEntity.ok(response);
  }

  public ResponseEntity<ApiResponse> addCountry(@RequestBody @Valid CountryRequest countryRequest) throws Exception {
    Country country = countryMapper.toEntity(countryRequest);
    Country savedCountry = countryService.addCountry(country);
    CountryResponse countryResponse = countryMapper.toResponse(savedCountry);
    ApiResponse response = new ApiResponse(System.currentTimeMillis(), "success", 201, "Country added successfully",
        "Country added successfully", objectMapper.writeValueAsString(countryResponse));
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  public ResponseEntity<ApiResponse> updateCountry(@RequestBody @Valid CountryRequest countryRequest) throws Exception {
    Country country = countryMapper.toEntity(countryRequest);
    Country updatedCountry = countryService.updateCountry(country);
    CountryResponse countryResponse = countryMapper.toResponse(updatedCountry);
    ApiResponse response = new ApiResponse(System.currentTimeMillis(), "success", 200, "Country updated successfully",
        "Country updated successfully", objectMapper.writeValueAsString(countryResponse));
    return ResponseEntity.ok(response);
  }
}