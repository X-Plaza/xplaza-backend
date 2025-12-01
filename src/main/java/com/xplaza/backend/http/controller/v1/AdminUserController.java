/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller.v1;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

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
import com.xplaza.backend.domain.AdminUser;
import com.xplaza.backend.http.dto.request.AdminUserRequest;
import com.xplaza.backend.http.dto.response.AdminUserResponse;
import com.xplaza.backend.mapper.AdminUserMapper;
import com.xplaza.backend.service.AdminUserLoginService;
import com.xplaza.backend.service.AdminUserService;
import com.xplaza.backend.service.RoleService;
import com.xplaza.backend.service.SecurityService;

/**
 * Admin User Controller - Clean REST API design.
 */
@RestController
@RequestMapping("/api/v1/admin-users")
@RequiredArgsConstructor
@Validated
@Slf4j
@Tag(name = "Admin User Management", description = "APIs for managing admin users")
public class AdminUserController {

  private final AdminUserService adminUserService;
  private final AdminUserLoginService adminUserLoginService;
  private final SecurityService securityService;
  private final RoleService roleService;
  private final AdminUserMapper adminUserMapper;

  @GetMapping
  @Operation(summary = "List admin users", description = "Get paginated list of admin users")
  public ResponseEntity<ApiResponse<List<AdminUserResponse>>> getAdminUsers(
      @RequestParam(required = false) Long userId,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) int size,
      @RequestParam(defaultValue = "adminUserId") String sort,
      @RequestParam(defaultValue = "ASC") Sort.Direction direction) {

    size = Math.min(size, 100);
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

    List<AdminUser> allUsers;
    if (userId != null) {
      String roleName = roleService.getRoleNameByUserID(userId);
      if (roleName != null && roleName.equals("Master Admin")) {
        allUsers = adminUserService.listAdminUsers();
      } else if (roleName != null) {
        AdminUser user = adminUserService.listAdminUser(userId);
        allUsers = user != null ? List.of(user) : List.of();
      } else {
        allUsers = List.of();
      }
    } else {
      allUsers = adminUserService.listAdminUsers();
    }

    // Paginate in memory
    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), allUsers.size());
    List<AdminUser> pageContent = start < allUsers.size() ? allUsers.subList(start, end) : List.of();

    List<AdminUserResponse> dtos = pageContent.stream()
        .map(adminUserMapper::toResponse)
        .toList();

    Page<AdminUserResponse> responsePage = new PageImpl<>(dtos, pageable, allUsers.size());
    PageMeta pageMeta = PageMeta.from(responsePage);

    return ResponseEntity.ok(ApiResponse.ok(dtos, pageMeta));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get admin user by ID", description = "Retrieve a specific admin user")
  public ResponseEntity<ApiResponse<AdminUserResponse>> getAdminUser(@PathVariable @Positive Long id) {
    AdminUser adminUser = adminUserService.listAdminUser(id);
    AdminUserResponse dto = adminUserMapper.toResponse(adminUser);
    return ResponseEntity.ok(ApiResponse.ok(dto));
  }

  @PostMapping
  @Operation(summary = "Create admin user", description = "Create a new admin user")
  public ResponseEntity<ApiResponse<Void>> createAdminUser(@RequestBody @Valid AdminUserRequest request) {
    AdminUser entity = adminUserMapper.toEntity(request);
    adminUserService.addAdminUser(entity);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Admin user created successfully"));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update admin user", description = "Update an existing admin user")
  public ResponseEntity<ApiResponse<AdminUserResponse>> updateAdminUser(
      @PathVariable @Positive Long id,
      @RequestBody @Valid AdminUserRequest request) {
    AdminUser entity = adminUserMapper.toEntity(request);
    entity.setAdminUserId(id);
    adminUserService.updateAdminUser(entity);
    AdminUserResponse dto = adminUserMapper.toResponse(entity);
    return ResponseEntity.ok(ApiResponse.ok(dto));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete admin user", description = "Delete an admin user")
  public ResponseEntity<ApiResponse<Void>> deleteAdminUser(@PathVariable @Positive Long id) {
    adminUserService.deleteAdminUser(id);
    return ResponseEntity.ok(ApiResponse.ok("Admin user deleted successfully"));
  }

  @PatchMapping("/{id}/password")
  @Operation(summary = "Change password", description = "Change admin user password")
  public ResponseEntity<ApiResponse<Void>> changePassword(
      @PathVariable @Positive Long id,
      @RequestBody @Valid PasswordChangeRequest request) {

    boolean isValidUser = adminUserLoginService.isValidAdminUser(request.username().toLowerCase(),
        request.oldPassword());
    if (!isValidUser) {
      return ResponseEntity.status(HttpStatus.FORBIDDEN)
          .body(ApiResponse.error("INVALID_CREDENTIALS", "Old password does not match"));
    }

    try {
      byte[] byteSalt = securityService.getSalt();
      byte[] byteDigestPsw = securityService.getSaltedHashSHA512(request.newPassword(), byteSalt);
      String strDigestPsw = securityService.toHex(byteDigestPsw);
      String strSalt = securityService.toHex(byteSalt);
      adminUserService.changeAdminUserPassword(strDigestPsw, strSalt, request.username().toLowerCase());
      return ResponseEntity.ok(ApiResponse.ok("Password changed successfully"));
    } catch (NoSuchAlgorithmException ex) {
      log.error("Salt generation error", ex);
      return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
          .body(ApiResponse.error("INTERNAL_ERROR", "Failed to change password"));
    }
  }

  public record PasswordChangeRequest(
      @NotBlank String username,
      @NotBlank String oldPassword,
      @NotBlank String newPassword
  ) {
  }
}
