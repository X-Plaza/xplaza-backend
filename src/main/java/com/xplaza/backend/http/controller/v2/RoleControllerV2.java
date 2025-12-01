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
import com.xplaza.backend.http.dto.request.RoleRequest;
import com.xplaza.backend.http.dto.response.RoleResponse;
import com.xplaza.backend.mapper.RoleMapper;
import com.xplaza.backend.service.RoleService;
import com.xplaza.backend.service.entity.Role;

/**
 * V2 Role Controller - Clean REST API design.
 */
@RestController
@RequestMapping("/api/v2/roles")
@RequiredArgsConstructor
@Validated
@Tag(name = "Role Management V2", description = "V2 APIs for managing roles")
public class RoleControllerV2 {

  private final RoleService roleService;
  private final RoleMapper roleMapper;

  @GetMapping
  @Operation(summary = "List roles", description = "Get paginated list of roles (excluding Master Admin)")
  public ResponseEntity<ApiResponseV2<List<RoleResponse>>> getRoles(
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) int size,
      @RequestParam(defaultValue = "roleId") String sort,
      @RequestParam(defaultValue = "ASC") Sort.Direction direction) {

    size = Math.min(size, 100);
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

    // listRoles already excludes Master Admin
    List<Role> allRoles = roleService.listRoles();

    if (search != null && !search.isBlank()) {
      String searchLower = search.toLowerCase().trim();
      allRoles = allRoles.stream()
          .filter(r -> r.getRoleName() != null && r.getRoleName().toLowerCase().contains(searchLower))
          .toList();
    }

    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), allRoles.size());
    List<Role> pageContent = start < allRoles.size() ? allRoles.subList(start, end) : List.of();

    List<RoleResponse> dtos = pageContent.stream()
        .map(roleMapper::toResponse)
        .toList();

    Page<RoleResponse> responsePage = new PageImpl<>(dtos, pageable, allRoles.size());
    PageMeta pageMeta = PageMeta.from(responsePage);

    return ResponseEntity.ok(ApiResponseV2.ok(dtos, pageMeta));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get role by ID", description = "Retrieve a specific role")
  public ResponseEntity<ApiResponseV2<RoleResponse>> getRole(@PathVariable @Positive Long id) {
    Role role = roleService.listRole(id);
    RoleResponse dto = roleMapper.toResponse(role);
    return ResponseEntity.ok(ApiResponseV2.ok(dto));
  }

  @PostMapping
  @Operation(summary = "Create role", description = "Create a new role")
  public ResponseEntity<ApiResponseV2<Void>> createRole(@RequestBody @Valid RoleRequest request) {
    Role entity = roleMapper.toEntity(request);
    roleService.addRole(entity);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseV2.ok("Role created successfully"));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update role", description = "Update an existing role")
  public ResponseEntity<ApiResponseV2<Void>> updateRole(
      @PathVariable @Positive Long id,
      @RequestBody @Valid RoleRequest request) {
    Role entity = roleMapper.toEntity(request);
    entity.setRoleId(id);
    roleService.updateRole(entity);
    return ResponseEntity.ok(ApiResponseV2.ok("Role updated successfully"));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete role", description = "Delete a role")
  public ResponseEntity<ApiResponseV2<Void>> deleteRole(@PathVariable @Positive Long id) {
    roleService.deleteRole(id);
    return ResponseEntity.ok(ApiResponseV2.ok("Role deleted successfully"));
  }
}
