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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.common.util.ApiResponseV2;
import com.xplaza.backend.common.util.ApiResponseV2.PageMeta;
import com.xplaza.backend.http.dto.request.ModuleRequest;
import com.xplaza.backend.http.dto.response.ModuleResponse;
import com.xplaza.backend.mapper.ModuleMapper;
import com.xplaza.backend.service.ModuleService;
import com.xplaza.backend.service.entity.Module;

/**
 * V2 Module Controller - Clean REST API design.
 */
@RestController
@RequestMapping("/api/v2/modules")
@RequiredArgsConstructor
@Validated
@Tag(name = "Module Management V2", description = "V2 APIs for managing modules")
public class ModuleControllerV2 {

  private final ModuleService moduleService;
  private final ModuleMapper moduleMapper;

  @GetMapping
  @Operation(summary = "List modules", description = "Get paginated list of modules")
  public ResponseEntity<ApiResponseV2<List<ModuleResponse>>> getModules(
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) int size,
      @RequestParam(defaultValue = "moduleId") String sort,
      @RequestParam(defaultValue = "ASC") Sort.Direction direction) {

    size = Math.min(size, 100);
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

    List<Module> allModules = moduleService.listModules();

    if (search != null && !search.isBlank()) {
      String searchLower = search.toLowerCase().trim();
      allModules = allModules.stream()
          .filter(m -> m.getModuleName() != null && m.getModuleName().toLowerCase().contains(searchLower))
          .toList();
    }

    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), allModules.size());
    List<Module> pageContent = start < allModules.size() ? allModules.subList(start, end) : List.of();

    List<ModuleResponse> dtos = pageContent.stream()
        .map(moduleMapper::toResponse)
        .toList();

    Page<ModuleResponse> responsePage = new PageImpl<>(dtos, pageable, allModules.size());
    PageMeta pageMeta = PageMeta.from(responsePage);

    return ResponseEntity.ok(ApiResponseV2.ok(dtos, pageMeta));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get module by ID", description = "Retrieve a specific module")
  public ResponseEntity<ApiResponseV2<ModuleResponse>> getModule(@PathVariable @Positive Long id) {
    Module module = moduleService.listModule(id);
    ModuleResponse dto = moduleMapper.toResponse(module);
    return ResponseEntity.ok(ApiResponseV2.ok(dto));
  }

  @PostMapping
  @Operation(summary = "Create module", description = "Create a new module")
  public ResponseEntity<ApiResponseV2<Void>> createModule(@RequestBody @Valid ModuleRequest request) {
    Module entity = moduleMapper.toEntity(request);
    moduleService.addModule(entity);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseV2.ok("Module created successfully"));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update module", description = "Update an existing module")
  public ResponseEntity<ApiResponseV2<Void>> updateModule(
      @PathVariable @Positive Long id,
      @RequestBody @Valid ModuleRequest request) {
    Module entity = moduleMapper.toEntity(request);
    entity.setModuleId(id);
    moduleService.updateModule(entity);
    return ResponseEntity.ok(ApiResponseV2.ok("Module updated successfully"));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete module", description = "Delete a module")
  public ResponseEntity<ApiResponseV2<Void>> deleteModule(@PathVariable @Positive Long id) {
    moduleService.deleteModule(id);
    return ResponseEntity.ok(ApiResponseV2.ok("Module deleted successfully"));
  }
}
