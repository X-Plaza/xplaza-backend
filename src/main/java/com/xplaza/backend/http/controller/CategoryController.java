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
import com.xplaza.backend.http.dto.request.CategoryRequest;
import com.xplaza.backend.http.dto.response.CategoryResponse;
import com.xplaza.backend.mapper.CategoryMapper;
import com.xplaza.backend.service.CategoryService;
import com.xplaza.backend.service.entity.Category;

@Deprecated(since = "1.0", forRemoval = true)
@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Category Management [DEPRECATED]", description = "APIs for managing product categories. Use V2 API instead.")
public class CategoryController extends BaseController {
  private final CategoryService categoryService;
  private final CategoryMapper categoryMapper;
  private final ObjectMapper objectMapper;

  @GetMapping
  @Operation(summary = "Get all categories", description = "Retrieves a list of all categories in the system", responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved categories", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ApiResponse> getAllCategories() {
    try {
      List<Category> categories = categoryService.listCategories();
      List<CategoryResponse> responses = categories.stream()
          .map(categoryMapper::toResponse)
          .collect(Collectors.toList());

      ApiResponse response = new ApiResponse(
          System.currentTimeMillis(),
          "success",
          200,
          "Categories retrieved successfully",
          "Categories retrieved successfully",
          objectMapper.writeValueAsString(responses));

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error retrieving categories", e);
      ApiResponse response = new ApiResponse(
          System.currentTimeMillis(),
          "error",
          500,
          "Failed to retrieve categories",
          "Failed to retrieve categories: " + e.getMessage(),
          null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get category by ID", description = "Retrieves a specific category by their ID", parameters = {
      @Parameter(name = "id", description = "Category ID", required = true, example = "1")
  }, responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved category", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ApiResponse> getCategoryById(@PathVariable Long id) {
    try {
      Category category = categoryService.listCategory(id);
      CategoryResponse response = categoryMapper.toResponse(category);

      ApiResponse apiResponse = new ApiResponse(
          System.currentTimeMillis(),
          "success",
          200,
          "Category retrieved successfully",
          "Category retrieved successfully",
          objectMapper.writeValueAsString(response));

      return ResponseEntity.ok(apiResponse);
    } catch (Exception e) {
      log.error("Error retrieving category with id: {}", id, e);
      ApiResponse response = new ApiResponse(
          System.currentTimeMillis(),
          "error",
          500,
          "Failed to retrieve category",
          "Failed to retrieve category: " + e.getMessage(),
          null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @PostMapping
  @Operation(summary = "Create new category", description = "Creates a new category in the system", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Category data", required = true, content = @Content(schema = @Schema(implementation = CategoryRequest.class))), responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Category created successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ApiResponse> createCategory(@Valid @RequestBody CategoryRequest request) {
    try {
      Category category = categoryMapper.toEntity(request);
      Category savedCategory = categoryService.addCategory(category);
      CategoryResponse response = categoryMapper.toResponse(savedCategory);

      ApiResponse apiResponse = new ApiResponse(
          System.currentTimeMillis(),
          "success",
          201,
          "Category created successfully",
          "Category created successfully",
          objectMapper.writeValueAsString(response));

      return ResponseEntity.status(HttpStatus.CREATED).body(apiResponse);
    } catch (Exception e) {
      log.error("Error creating category", e);
      ApiResponse response = new ApiResponse(
          System.currentTimeMillis(),
          "error",
          500,
          "Failed to create category",
          "Failed to create category: " + e.getMessage(),
          null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update category", description = "Updates an existing category by ID", parameters = {
      @Parameter(name = "id", description = "Category ID", required = true, example = "1")
  }, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated category data", required = true, content = @Content(schema = @Schema(implementation = CategoryRequest.class))), responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category updated successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ApiResponse> updateCategory(@PathVariable Long id,
      @Valid @RequestBody CategoryRequest request) {
    try {
      Category category = categoryMapper.toEntity(request);
      Category updatedCategory = categoryService.updateCategory(id, category);
      CategoryResponse response = categoryMapper.toResponse(updatedCategory);

      ApiResponse apiResponse = new ApiResponse(
          System.currentTimeMillis(),
          "success",
          200,
          "Category updated successfully",
          "Category updated successfully",
          objectMapper.writeValueAsString(response));

      return ResponseEntity.ok(apiResponse);
    } catch (Exception e) {
      log.error("Error updating category with id: {}", id, e);
      ApiResponse response = new ApiResponse(
          System.currentTimeMillis(),
          "error",
          500,
          "Failed to update category",
          "Failed to update category: " + e.getMessage(),
          null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete category", description = "Deletes a category by ID", parameters = {
      @Parameter(name = "id", description = "Category ID", required = true, example = "1")
  }, responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Category deleted successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Category not found"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ApiResponse> deleteCategory(@PathVariable Long id) {
    try {
      categoryService.deleteCategory(id);

      ApiResponse response = new ApiResponse(
          System.currentTimeMillis(),
          "success",
          200,
          "Category deleted successfully",
          "Category deleted successfully",
          null);

      return ResponseEntity.ok(response);
    } catch (Exception e) {
      log.error("Error deleting category with id: {}", id, e);
      ApiResponse response = new ApiResponse(
          System.currentTimeMillis(),
          "error",
          500,
          "Failed to delete category",
          "Failed to delete category: " + e.getMessage(),
          null);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }
  }

  // Keep existing methods for backward compatibility
  public ResponseEntity<ApiResponse> getCategories() throws Exception {
    List<Category> categories = categoryService.listCategories();
    List<CategoryResponse> categoryResponses = categories.stream().map(categoryMapper::toResponse).toList();
    ApiResponse response = new ApiResponse(System.currentTimeMillis(), "success", 200,
        "Categories retrieved successfully", "Categories retrieved successfully",
        objectMapper.writeValueAsString(categoryResponses));
    return ResponseEntity.ok(response);
  }

  public ResponseEntity<ApiResponse> addCategory(@RequestBody @Valid CategoryRequest categoryRequest) throws Exception {
    Category category = categoryMapper.toEntity(categoryRequest);
    Category savedCategory = categoryService.addCategory(category);
    CategoryResponse categoryResponse = categoryMapper.toResponse(savedCategory);
    ApiResponse response = new ApiResponse(System.currentTimeMillis(), "success", 201, "Category added successfully",
        "Category added successfully", objectMapper.writeValueAsString(categoryResponse));
    return ResponseEntity.status(HttpStatus.CREATED).body(response);
  }
}
