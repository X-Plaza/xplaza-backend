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
import com.xplaza.backend.http.dto.request.CityRequest;
import com.xplaza.backend.http.dto.response.CityResponse;
import com.xplaza.backend.mapper.CityMapper;
import com.xplaza.backend.service.CityService;
import com.xplaza.backend.service.entity.City;

@Deprecated(since = "1.0", forRemoval = true)
@RestController
@RequestMapping("/api/v1/cities")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "City Management [DEPRECATED]", description = "APIs for managing cities. Use V2 API instead.")
public class CityController extends BaseController {
  private final CityService cityService;
  private final CityMapper cityMapper;
  private final ObjectMapper objectMapper;

  @GetMapping
  @Operation(summary = "Get all cities", description = "Retrieves a list of all cities in the system", responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved cities", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ApiResponse> getAllCities() {
    try {
      List<City> cities = cityService.listCities();
      List<CityResponse> responses = cities.stream()
          .map(cityMapper::toResponse)
          .collect(Collectors.toList());

      ApiResponse response = new ApiResponse(
          System.currentTimeMillis(),
          "success",
          200,
          "Cities retrieved successfully",
          "Cities retrieved successfully",
          objectMapper.writeValueAsString(responses));

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error retrieving cities", e);
      ApiResponse response = new ApiResponse(
          System.currentTimeMillis(),
          "error",
          500,
          "Failed to retrieve cities",
          "Failed to retrieve cities: " + e.getMessage(),
          null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get city by ID", description = "Retrieves a specific city by their ID", parameters = {
      @Parameter(name = "id", description = "City ID", required = true, example = "1")
  }, responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved city", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "City not found"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ApiResponse> getCityById(@PathVariable Long id) {
    try {
      City city = cityService.listCity(id);
      CityResponse response = cityMapper.toResponse(city);

      ApiResponse apiResponse = new ApiResponse(
          System.currentTimeMillis(),
          "success",
          200,
          "City retrieved successfully",
          "City retrieved successfully",
          objectMapper.writeValueAsString(response));

      return ResponseEntity.ok(apiResponse);
    } catch (Exception e) {
      log.error("Error retrieving city with id: {}", id, e);
      ApiResponse response = new ApiResponse(
          System.currentTimeMillis(),
          "error",
          500,
          "Failed to retrieve city",
          "Failed to retrieve city: " + e.getMessage(),
          null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @PostMapping
  @Operation(summary = "Create new city", description = "Creates a new city in the system", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "City data", required = true, content = @Content(schema = @Schema(implementation = CityRequest.class))), responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "City created successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ApiResponse> createCity(@Valid @RequestBody CityRequest request) {
    try {
      City city = cityMapper.toEntity(request);
      City savedCity = cityService.addCity(city);
      CityResponse response = cityMapper.toResponse(savedCity);

      ApiResponse apiResponse = new ApiResponse(
          System.currentTimeMillis(),
          "success",
          201,
          "City created successfully",
          "City created successfully",
          objectMapper.writeValueAsString(response));

      return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    } catch (Exception e) {
      log.error("Error creating city", e);
      ApiResponse response = new ApiResponse(
          System.currentTimeMillis(),
          "error",
          500,
          "Failed to create city",
          "Failed to create city: " + e.getMessage(),
          null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update city", description = "Updates an existing city by ID", parameters = {
      @Parameter(name = "id", description = "City ID", required = true, example = "1")
  }, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated city data", required = true, content = @Content(schema = @Schema(implementation = CityRequest.class))), responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "City updated successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "City not found"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ApiResponse> updateCity(@PathVariable Long id, @Valid @RequestBody CityRequest request) {
    try {
      City city = cityMapper.toEntity(request);
      city.setCityId(id);
      City updatedCity = cityService.updateCity(city);
      CityResponse response = cityMapper.toResponse(updatedCity);

      ApiResponse apiResponse = new ApiResponse(
          System.currentTimeMillis(),
          "success",
          200,
          "City updated successfully",
          "City updated successfully",
          objectMapper.writeValueAsString(response));

      return ResponseEntity.ok(apiResponse);
    } catch (Exception e) {
      log.error("Error updating city with id: {}", id, e);
      ApiResponse response = new ApiResponse(
          System.currentTimeMillis(),
          "error",
          500,
          "Failed to update city",
          "Failed to update city: " + e.getMessage(),
          null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete city", description = "Deletes a city by ID", parameters = {
      @Parameter(name = "id", description = "City ID", required = true, example = "1")
  }, responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "City deleted successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "City not found"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ApiResponse> deleteCity(@PathVariable Long id) {
    try {
      cityService.deleteCity(id);

      ApiResponse response = new ApiResponse(
          System.currentTimeMillis(),
          "success",
          200,
          "City deleted successfully",
          "City deleted successfully",
          null);

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error deleting city with id: {}", id, e);
      ApiResponse response = new ApiResponse(
          System.currentTimeMillis(),
          "error",
          500,
          "Failed to delete city",
          "Failed to delete city: " + e.getMessage(),
          null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  // Keep existing methods for backward compatibility
  public ResponseEntity<ApiResponse> getCities() throws Exception {
    List<City> cities = cityService.listCities();
    List<CityResponse> cityResponses = cities.stream().map(cityMapper::toResponse).toList();
    ApiResponse response = new ApiResponse(System.currentTimeMillis(), "success", 200, "Cities retrieved successfully",
        "Cities retrieved successfully", objectMapper.writeValueAsString(cityResponses));
    return ResponseEntity.ok(response);
  }

  public ResponseEntity<ApiResponse> addCity(@RequestBody @Valid CityRequest cityRequest) throws Exception {
    City city = cityMapper.toEntity(cityRequest);
    City savedCity = cityService.addCity(city);
    CityResponse cityResponse = cityMapper.toResponse(savedCity);
    ApiResponse response = new ApiResponse(System.currentTimeMillis(), "success", 201, "City added successfully",
        "City added successfully", objectMapper.writeValueAsString(cityResponse));
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  public ResponseEntity<ApiResponse> updateCity(@RequestBody @Valid CityRequest cityRequest) throws Exception {
    City city = cityMapper.toEntity(cityRequest);
    City updatedCity = cityService.updateCity(city);
    CityResponse cityResponse = cityMapper.toResponse(updatedCity);
    ApiResponse response = new ApiResponse(System.currentTimeMillis(), "success", 200, "City updated successfully",
        "City updated successfully", objectMapper.writeValueAsString(cityResponse));
    return ResponseEntity.ok(response);
  }
}
