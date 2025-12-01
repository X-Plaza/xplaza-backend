/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.List;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.http.dto.request.AdminUserRequest;
import com.xplaza.backend.http.dto.response.AdminUserResponse;
import com.xplaza.backend.mapper.AdminUserMapper;
import com.xplaza.backend.service.AdminUserLoginService;
import com.xplaza.backend.service.AdminUserService;
import com.xplaza.backend.service.ConfirmationTokenService;
import com.xplaza.backend.service.RoleService;
import com.xplaza.backend.service.SecurityService;
import com.xplaza.backend.service.entity.AdminUser;

@Deprecated(since = "1.0", forRemoval = true)
@RestController
@RequestMapping("/api/v1/admin-users")
@Tag(name = "Admin User Management [DEPRECATED]", description = "APIs for managing admin users. Use V2 API instead.")
public class AdminUserController extends BaseController {
  private static final Logger logger = LoggerFactory.getLogger(AdminUserController.class);
  private final AdminUserService adminUserService;
  private final AdminUserLoginService adminUserLoginService;
  private final ConfirmationTokenService confirmationTokenService;
  private final SecurityService securityService;
  private final RoleService roleService;
  private final AdminUserMapper adminUserMapper;
  private final ObjectMapper objectMapper;

  @Autowired
  public AdminUserController(AdminUserService adminUserService, AdminUserLoginService adminUserLoginService,
      ConfirmationTokenService confirmationTokenService, SecurityService securityService, RoleService roleService,
      AdminUserMapper adminUserMapper, ObjectMapper objectMapper) {
    this.adminUserService = adminUserService;
    this.adminUserLoginService = adminUserLoginService;
    this.confirmationTokenService = confirmationTokenService;
    this.securityService = securityService;
    this.roleService = roleService;
    this.adminUserMapper = adminUserMapper;
    this.objectMapper = objectMapper;
  }

  @GetMapping
  @Operation(summary = "Get all admin users", description = "Retrieves a list of all admin users in the system", responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved admin users", content = @Content(schema = @Schema(implementation = ApiResponse.class)))
  })
  public ResponseEntity<?> getAdminUsers(@RequestParam(value = "user_id") @Valid Long userId) {
    String roleName = roleService.getRoleNameByUserID(userId);
    if (roleName == null) {
      return ResponseEntity.ok().body(List.of());
    } else if (roleName.equals("Master Admin")) {
      List<AdminUser> adminUsers = adminUserService.listAdminUsers();
      List<AdminUserResponse> adminUserResponses = adminUsers.stream().map(adminUserMapper::toResponse).toList();
      return ResponseEntity.ok(adminUserResponses);
    } else {
      AdminUser adminUser = adminUserService.listAdminUser(userId);
      AdminUserResponse adminUserResponse = adminUserMapper.toResponse(adminUser);
      return ResponseEntity.ok(adminUserResponse);
    }
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get admin user by ID", description = "Retrieves a specific admin user by their ID", parameters = {
      @Parameter(name = "id", description = "Admin user ID", required = true, example = "1")
  }, responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Successfully retrieved admin user", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Admin user not found")
  })
  public ResponseEntity<AdminUserResponse> getAdminUser(@PathVariable @Valid Long id) {
    AdminUser adminUser = adminUserService.listAdminUser(id);
    AdminUserResponse adminUserResponse = adminUserMapper.toResponse(adminUser);
    return ResponseEntity.ok(adminUserResponse);
  }

  @PostMapping
  @Operation(summary = "Create an admin user", description = "Creates a new admin user in the system", requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Admin user data", required = true, content = @Content(schema = @Schema(implementation = AdminUserRequest.class))), responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "201", description = "Admin user created successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data")
  })
  public ResponseEntity<ApiResponse> addAdminUser(@RequestBody @Valid AdminUserRequest adminUserRequest) {
    long startTime = System.currentTimeMillis();
    AdminUser adminUser = adminUserMapper.toEntity(adminUserRequest);
    adminUserService.addAdminUser(adminUser);
    long responseTime = System.currentTimeMillis() - startTime;
    ApiResponse response = new ApiResponse(responseTime, "success", 200, "Admin user added successfully",
        "Create an admin user", null);
    return ResponseEntity.ok(response);
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update admin user", description = "Updates an existing admin user by ID", parameters = {
      @Parameter(name = "id", description = "Admin user ID", required = true, example = "1")
  }, requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "Updated admin user data", required = true, content = @Content(schema = @Schema(implementation = AdminUserRequest.class))), responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Admin user updated successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Admin user not found"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "400", description = "Invalid input data"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ApiResponse> updateAdminUser(@PathVariable Long id,
      @Valid @RequestBody AdminUserRequest request) throws JsonProcessingException {
    AdminUser adminUser = adminUserMapper.toEntity(request);
    adminUser.setAdminUserId(id);
    adminUserService.updateAdminUser(adminUser);
    AdminUserResponse response = adminUserMapper.toResponse(adminUser);

    ApiResponse apiResponse = new ApiResponse(
        System.currentTimeMillis(),
        "success",
        200,
        "Admin user updated successfully",
        "Update admin user",
        objectMapper.writeValueAsString(response));
    return ResponseEntity.ok(apiResponse);
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete an admin user", description = "Deletes an admin user by ID", parameters = {
      @Parameter(name = "id", description = "Admin user ID", required = true, example = "1")
  }, responses = {
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "200", description = "Admin user deleted successfully", content = @Content(schema = @Schema(implementation = ApiResponse.class))),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "404", description = "Admin user not found"),
      @io.swagger.v3.oas.annotations.responses.ApiResponse(responseCode = "500", description = "Internal server error")
  })
  public ResponseEntity<ApiResponse> deleteAdminUser(@PathVariable Long id) {
    adminUserService.deleteAdminUser(id);
    ApiResponse response = new ApiResponse(
        System.currentTimeMillis(),
        "success",
        200,
        "Admin user deleted successfully",
        "Delete an admin user",
        null);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/change-password")
  public ResponseEntity<ApiResponse> changeAdminUserPassword(@RequestParam("username") @Valid String username,
      @RequestParam("oldPassword") @Valid String oldPassword,
      @RequestParam("newPassword") @Valid String newPassword) throws IOException {
    long startTime = System.currentTimeMillis();
    boolean isValidUser = adminUserLoginService.isValidAdminUser(username.toLowerCase(), oldPassword);
    if (!isValidUser) {
      long responseTime = System.currentTimeMillis() - startTime;
      return new ResponseEntity<>(new ApiResponse(responseTime, "Change Admin User Password",
          HttpStatus.FORBIDDEN.value(), "Failure", "Old Password does not match.", null), HttpStatus.FORBIDDEN);
    }

    try {
      byte[] byteSalt = securityService.getSalt();
      byte[] biteDigestPsw = securityService.getSaltedHashSHA512(newPassword, byteSalt);
      String strDigestPsw = securityService.toHex(biteDigestPsw);
      String strSalt = securityService.toHex(byteSalt);
      adminUserService.changeAdminUserPassword(strDigestPsw, strSalt, username.toLowerCase());

      long responseTime = System.currentTimeMillis() - startTime;
      return new ResponseEntity<>(new ApiResponse(responseTime, "Change Admin User Password",
          HttpStatus.OK.value(), "Success", "Password has been updated successfully.", null), HttpStatus.OK);
    } catch (NoSuchAlgorithmException ex) {
      logger.error("Salt generation error", ex);
      long responseTime = System.currentTimeMillis() - startTime;
      return new ResponseEntity<>(new ApiResponse(responseTime, "Change Admin User Password",
          HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error", "Failed to update password: " + ex.getMessage(), null),
          HttpStatus.INTERNAL_SERVER_ERROR);
    }
  }
}
