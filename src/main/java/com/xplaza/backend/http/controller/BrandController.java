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
import com.xplaza.backend.http.dto.request.BrandRequest;
import com.xplaza.backend.http.dto.response.BrandResponse;
import com.xplaza.backend.mapper.BrandMapper;
import com.xplaza.backend.service.BrandService;
import com.xplaza.backend.service.entity.Brand;

@Deprecated(since = "1.0", forRemoval = true)
@RestController
@RequestMapping("/api/v1/brands")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Brand Management [DEPRECATED]", description = "APIs for managing product brands. Use V2 API instead.")
public class BrandController extends BaseController {
  private final BrandService brandService;
  private final BrandMapper brandMapper;
  private final ObjectMapper objectMapper;

  @GetMapping
  @Operation(summary = "Get all brands", description = "Retrieves a list of all brands in the system", responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved brands", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ApiResponse> getAllBrands() {
    try {
      List<Brand> brands = brandService.listBrands();
      List<BrandResponse> responses = brands.stream()
          .map(brandMapper::toResponse)
          .collect(Collectors.toList());

      ApiResponse response = new ApiResponse(
          System.currentTimeMillis(),
          "success",
          200,
          "Brands retrieved successfully",
          "Brands retrieved successfully",
          objectMapper.writeValueAsString(responses));

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error retrieving brands", e);
      ApiResponse response = new ApiResponse(
          System.currentTimeMillis(),
          "error",
          500,
          "Failed to retrieve brands",
          "Failed to retrieve brands: " + e.getMessage(),
          null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get brand by ID", description = "Retrieves a specific brand by their ID", parameters = {
      @Parameter(name = "id", description = "Brand ID", required = true, example = "1")
  }, responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved brand", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Brand not found"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ApiResponse> getBrandById(@PathVariable Long id) {
    try {
      Brand brand = brandService.listBrand(id);
      BrandResponse response = brandMapper.toResponse(brand);

      ApiResponse apiResponse = new ApiResponse(
          System.currentTimeMillis(),
          "success",
          200,
          "Brand retrieved successfully",
          "Brand retrieved successfully",
          objectMapper.writeValueAsString(response));

      return ResponseEntity.ok(apiResponse);
    } catch (Exception e) {
      log.error("Error retrieving brand with id: {}", id, e);
      ApiResponse response = new ApiResponse(
          System.currentTimeMillis(),
          "error",
          500,
          "Failed to retrieve brand",
          "Failed to retrieve brand: " + e.getMessage(),
          null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @PostMapping
  @Operation(summary = "Create new brand", description = "Creates a new brand in the system", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Brand data", required = true, content = @Content(schema = @Schema(implementation = BrandRequest.class))), responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Brand created successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ApiResponse> createBrand(@Valid @RequestBody BrandRequest request) {
    try {
      Brand brand = brandMapper.toEntity(request);
      Brand savedBrand = brandService.addBrand(brand);
      BrandResponse response = brandMapper.toResponse(savedBrand);

      ApiResponse apiResponse = new ApiResponse(
          System.currentTimeMillis(),
          "success",
          201,
          "Brand created successfully",
          "Brand created successfully",
          objectMapper.writeValueAsString(response));

      return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    } catch (Exception e) {
      log.error("Error creating brand", e);
      ApiResponse response = new ApiResponse(
          System.currentTimeMillis(),
          "error",
          500,
          "Failed to create brand",
          "Failed to create brand: " + e.getMessage(),
          null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update brand", description = "Updates an existing brand by ID", parameters = {
      @Parameter(name = "id", description = "Brand ID", required = true, example = "1")
  }, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated brand data", required = true, content = @Content(schema = @Schema(implementation = BrandRequest.class))), responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Brand updated successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Brand not found"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ApiResponse> updateBrand(@PathVariable Long id, @Valid @RequestBody BrandRequest request) {
    try {
      Brand brand = brandMapper.toEntity(request);
      brand.setBrandId(id);
      Brand updatedBrand = brandService.updateBrand(brand);
      BrandResponse response = brandMapper.toResponse(updatedBrand);

      ApiResponse apiResponse = new ApiResponse(
          System.currentTimeMillis(),
          "success",
          200,
          "Brand updated successfully",
          "Brand updated successfully",
          objectMapper.writeValueAsString(response));

      return ResponseEntity.ok(apiResponse);
    } catch (Exception e) {
      log.error("Error updating brand with id: {}", id, e);
      ApiResponse response = new ApiResponse(
          System.currentTimeMillis(),
          "error",
          500,
          "Failed to update brand",
          "Failed to update brand: " + e.getMessage(),
          null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete brand", description = "Deletes a brand by ID", parameters = {
      @Parameter(name = "id", description = "Brand ID", required = true, example = "1")
  }, responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Brand deleted successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Brand not found"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ApiResponse> deleteBrand(@PathVariable Long id) {
    try {
      brandService.deleteBrand(id);

      ApiResponse response = new ApiResponse(
          System.currentTimeMillis(),
          "success",
          200,
          "Brand deleted successfully",
          "Brand deleted successfully",
          null);

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error deleting brand with id: {}", id, e);
      ApiResponse response = new ApiResponse(
          System.currentTimeMillis(),
          "error",
          500,
          "Failed to delete brand",
          "Failed to delete brand: " + e.getMessage(),
          null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  // Keep existing method for backward compatibility
  public ResponseEntity<ApiResponse> getBrands() throws Exception {
    List<Brand> brands = brandService.listBrands();
    List<BrandResponse> brandResponses = brands.stream().map(brandMapper::toResponse).toList();
    ApiResponse response = new ApiResponse(System.currentTimeMillis(), "success", 200, "Brands retrieved successfully",
        "Brands retrieved successfully", objectMapper.writeValueAsString(brandResponses));
    return ResponseEntity.ok(response);
  }

  public ResponseEntity<ApiResponse> addBrand(@RequestBody @Valid BrandRequest brandRequest) throws Exception {
    Brand brand = brandMapper.toEntity(brandRequest);
    Brand savedBrand = brandService.addBrand(brand);
    BrandResponse brandResponse = brandMapper.toResponse(savedBrand);
    ApiResponse response = new ApiResponse(System.currentTimeMillis(), "success", 201, "Brand added successfully",
        "Brand added successfully", objectMapper.writeValueAsString(brandResponse));
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }

  public ResponseEntity<ApiResponse> updateBrand(@RequestBody @Valid BrandRequest brandRequest) throws Exception {
    Brand brand = brandMapper.toEntity(brandRequest);
    Brand updatedBrand = brandService.updateBrand(brand);
    BrandResponse brandResponse = brandMapper.toResponse(updatedBrand);
    ApiResponse response = new ApiResponse(System.currentTimeMillis(), "success", 200, "Brand updated successfully",
        "Brand updated successfully", objectMapper.writeValueAsString(brandResponse));
    return ResponseEntity.ok(response);
  }
}
