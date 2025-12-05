/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.catalog.controller;

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

import com.xplaza.backend.catalog.domain.entity.Category;
import com.xplaza.backend.catalog.dto.request.CategoryRequest;
import com.xplaza.backend.catalog.dto.response.CategoryResponse;
import com.xplaza.backend.catalog.mapper.CategoryMapper;
import com.xplaza.backend.catalog.service.CategoryService;
import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.common.util.ApiResponse.PageMeta;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
@Validated
@Tag(name = "Category Management", description = "APIs for managing product categories with pagination")
public class CategoryController {

  private final CategoryService categoryService;
  private final CategoryMapper categoryMapper;

  @GetMapping
  @Operation(summary = "List categories", description = "Get paginated list of categories with optional search and parent filter")
  public ResponseEntity<ApiResponse<List<CategoryResponse>>> getCategories(
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

    return ResponseEntity.ok(ApiResponse.ok(dtos, pageMeta));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get category by ID", description = "Retrieve a specific category by its ID")
  public ResponseEntity<ApiResponse<CategoryResponse>> getCategory(
      @PathVariable @Positive Long id) {

    Category category = categoryService.listCategory(id);
    CategoryResponse dto = categoryMapper.toResponse(category);

    return ResponseEntity.ok(ApiResponse.ok(dto));
  }

  @PostMapping
  @Operation(summary = "Create category", description = "Create a new category")
  public ResponseEntity<ApiResponse<CategoryResponse>> createCategory(
      @RequestBody @Valid CategoryRequest request) {

    Category entity = categoryMapper.toEntity(request);
    Category saved = categoryService.addCategory(entity);
    CategoryResponse dto = categoryMapper.toResponse(saved);

    return ResponseEntity
        .status(HttpStatus.CREATED)
        .body(ApiResponse.created(dto));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update category", description = "Update an existing category by ID")
  public ResponseEntity<ApiResponse<CategoryResponse>> updateCategory(
      @PathVariable @Positive Long id,
      @RequestBody @Valid CategoryRequest request) {

    Category entity = categoryMapper.toEntity(request);
    Category updated = categoryService.updateCategory(id, entity);
    CategoryResponse dto = categoryMapper.toResponse(updated);

    return ResponseEntity.ok(ApiResponse.ok(dto));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete category", description = "Delete a category by ID")
  public ResponseEntity<ApiResponse<Void>> deleteCategory(
      @PathVariable @Positive Long id) {

    String categoryName = categoryService.getCategoryNameByID(id);
    categoryService.deleteCategory(id);

    return ResponseEntity.ok(ApiResponse.ok(categoryName + " has been deleted"));
  }
}
