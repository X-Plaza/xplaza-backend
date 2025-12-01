/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller.v2;

import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.common.util.ApiResponseV2;
import com.xplaza.backend.common.util.ApiResponseV2.PageMeta;
import com.xplaza.backend.http.dto.request.CategoryRequest;
import com.xplaza.backend.http.dto.response.CategoryResponse;
import com.xplaza.backend.mapper.CategoryMapper;
import com.xplaza.backend.service.CategoryService;
import com.xplaza.backend.service.entity.Category;

/**
 * V2 Category Controller - Clean REST API design.
 * 
 * Endpoints: - GET /api/v2/categories - List all categories with pagination -
 * GET /api/v2/categories/{id} - Get single category - POST /api/v2/categories -
 * Create category - PUT /api/v2/categories/{id} - Update category - DELETE
 * /api/v2/categories/{id} - Delete category
 */
@RestController
@RequestMapping("/api/v2/categories")
@RequiredArgsConstructor
@Validated
@Tag(name = "Category Management V2", description = "V2 APIs for managing product categories with pagination")
public class CategoryControllerV2 {

  private final CategoryService categoryService;
  private final CategoryMapper categoryMapper;

  /**
   * GET /api/v2/categories
   * 
   * List categories with pagination and optional filters.
   */
  @GetMapping
  @Operation(summary = "List categories", description = "Get paginated list of categories with optional search and parent filter")
  public ResponseEntity<ApiResponseV2<List<CategoryResponse>>> getCategories(
      @RequestParam(required = false) Long parentId,
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) int size,
      @RequestParam(defaultValue = "categoryId") String sort,
      @RequestParam(defaultValue = "ASC") Sort.Direction direction) {

    size = Math.min(size, 100);
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

    Page<Category> categoryPage;
    if (search != null && !search.isBlank()) {
      categoryPage = categoryService.searchCategories(search.trim(), pageable);
    } else if (parentId != null) {
      categoryPage = categoryService.listCategoriesByParent(parentId, pageable);
    } else {
      categoryPage = categoryService.listCategoriesPaginated(pageable);
    }

    List<CategoryResponse> dtos = categoryPage.getContent().stream()
        .map(categoryMapper::toResponse)
        .toList();

    PageMeta pageMeta = PageMeta.from(categoryPage);

    return ResponseEntity.ok(ApiResponseV2.ok(dtos, pageMeta));
  }

  /**
   * GET /api/v2/categories/{id}
   * 
   * Get single category by ID.
   */
  @GetMapping("/{id}")
  @Operation(summary = "Get category by ID", description = "Retrieve a specific category by its ID")
  public ResponseEntity<ApiResponseV2<CategoryResponse>> getCategory(
      @PathVariable @Positive Long id) {

    Category category = categoryService.listCategory(id);
    CategoryResponse dto = categoryMapper.toResponse(category);

    return ResponseEntity.ok(ApiResponseV2.ok(dto));
  }

  /**
   * POST /api/v2/categories
   * 
   * Create a new category.
   */
  @PostMapping
  @Operation(summary = "Create category", description = "Create a new category")
  public ResponseEntity<ApiResponseV2<CategoryResponse>> createCategory(
      @RequestBody @Valid CategoryRequest request) {

    Category entity = categoryMapper.toEntity(request);
    Category saved = categoryService.addCategory(entity);
    CategoryResponse dto = categoryMapper.toResponse(saved);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponseV2.created(dto));
  }

  /**
   * PUT /api/v2/categories/{id}
   * 
   * Update an existing category.
   */
  @PutMapping("/{id}")
  @Operation(summary = "Update category", description = "Update an existing category by ID")
  public ResponseEntity<ApiResponseV2<CategoryResponse>> updateCategory(
      @PathVariable @Positive Long id,
      @RequestBody @Valid CategoryRequest request) {

    Category entity = categoryMapper.toEntity(request);
    Category updated = categoryService.updateCategory(id, entity);
    CategoryResponse dto = categoryMapper.toResponse(updated);

    return ResponseEntity.ok(ApiResponseV2.ok(dto));
  }

  /**
   * DELETE /api/v2/categories/{id}
   * 
   * Delete a category.
   */
  @DeleteMapping("/{id}")
  @Operation(summary = "Delete category", description = "Delete a category by ID")
  public ResponseEntity<ApiResponseV2<Void>> deleteCategory(
      @PathVariable @Positive Long id) {

    String categoryName = categoryService.getCategoryNameByID(id);
    categoryService.deleteCategory(id);

    return ResponseEntity.ok(ApiResponseV2.ok(categoryName + " has been deleted"));
  }
}
