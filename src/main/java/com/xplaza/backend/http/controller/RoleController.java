/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.http.dto.request.RoleRequest;
import com.xplaza.backend.http.dto.response.RoleResponse;
import com.xplaza.backend.mapper.RoleMapper;
import com.xplaza.backend.service.RoleService;
import com.xplaza.backend.service.entity.Role;

@Deprecated(since = "1.0", forRemoval = true)
@RestController
@RequestMapping("/api/v1/roles")
public class RoleController extends BaseController {
  private final RoleService roleService;
  private final RoleMapper roleMapper;

  @Autowired
  public RoleController(RoleService roleService, RoleMapper roleMapper) {
    this.roleService = roleService;
    this.roleMapper = roleMapper;
  }

  @GetMapping
  public ResponseEntity<ApiResponse> getRoles() throws JsonProcessingException {
    long start = System.currentTimeMillis();
    List<Role> roles = roleService.listRoles();
    roles.removeIf(r -> r.getRoleName().equals("Master Admin"));
    List<RoleResponse> dtos = roles.stream().map(roleMapper::toResponse).toList();
    long responseTime = System.currentTimeMillis() - start;
    String data = new ObjectMapper().writeValueAsString(dtos);
    ApiResponse response = new ApiResponse(responseTime, "Role List", HttpStatus.OK.value(), "Success", "", data);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse> getRole(@PathVariable @Valid Long id) throws JsonProcessingException {
    long start = System.currentTimeMillis();
    Role entity = roleService.listRole(id);
    RoleResponse dto = roleMapper.toResponse(entity);
    long responseTime = System.currentTimeMillis() - start;
    String data = new ObjectMapper().writeValueAsString(dto);
    ApiResponse response = new ApiResponse(responseTime, "Role by ID", HttpStatus.OK.value(), "Success", "", data);
    return ResponseEntity.ok(response);
  }

  @PostMapping
  public ResponseEntity<ApiResponse> addRole(@RequestBody @Valid RoleRequest roleRequest) {
    long start = System.currentTimeMillis();
    Role entity = roleMapper.toEntity(roleRequest);
    roleService.addRole(entity);
    long responseTime = System.currentTimeMillis() - start;
    return new ResponseEntity<>(new ApiResponse(responseTime, "Add Role", HttpStatus.CREATED.value(),
        "Success", "Role has been created.", null), HttpStatus.CREATED);
  }

  @PutMapping
  public ResponseEntity<ApiResponse> updateRole(@RequestBody @Valid RoleRequest roleRequestDTO) {
    long start = System.currentTimeMillis();
    Role entity = roleMapper.toEntity(roleRequestDTO);
    roleService.updateRole(entity);
    long responseTime = System.currentTimeMillis() - start;
    return new ResponseEntity<>(new ApiResponse(responseTime, "Update Role", HttpStatus.OK.value(),
        "Success", "Role has been updated.", null), HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse> deleteRole(@PathVariable @Valid Long id) {
    String role_name = roleService.getRoleNameByID(id);
    long start = System.currentTimeMillis();
    roleService.deleteRole(id);
    long responseTime = System.currentTimeMillis() - start;
    return new ResponseEntity<>(new ApiResponse(responseTime, "Delete Role", HttpStatus.OK.value(),
        "Success", role_name + " has been deleted.", null), HttpStatus.OK);
  }
}
