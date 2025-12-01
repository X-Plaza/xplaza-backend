/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller;

import java.security.NoSuchAlgorithmException;

import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.http.dto.request.CustomerUserRequest;
import com.xplaza.backend.http.dto.response.CustomerUserResponse;
import com.xplaza.backend.mapper.CustomerMapper;
import com.xplaza.backend.service.CustomerLoginService;
import com.xplaza.backend.service.CustomerUserService;
import com.xplaza.backend.service.SecurityService;
import com.xplaza.backend.service.entity.Customer;

@Deprecated(since = "1.0", forRemoval = true)
@RestController
@RequestMapping("/api/v1/customers")
public class CustomerUserController extends BaseController {
  private static final Logger logger = LoggerFactory.getLogger(CustomerUserController.class);
  private final CustomerUserService customerUserService;
  private final CustomerLoginService customerLoginService;
  private final SecurityService securityService;
  private final CustomerMapper customerMapper;

  @Autowired
  public CustomerUserController(CustomerUserService customerUserService, CustomerLoginService customerLoginService,
      SecurityService securityService, CustomerMapper customerMapper) {
    this.customerUserService = customerUserService;
    this.customerLoginService = customerLoginService;
    this.securityService = securityService;
    this.customerMapper = customerMapper;
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse> getCustomer(@PathVariable @Valid Long id) throws JsonProcessingException {
    Customer entity = customerUserService.getCustomer(id);
    CustomerUserResponse dto = customerMapper.toResponse(entity);
    String data = new ObjectMapper().writeValueAsString(dto);
    ApiResponse response = new ApiResponse(0L, "Customer By ID", HttpStatus.OK.value(), "Success", "", data);
    return ResponseEntity.ok(response);
  }

  @PutMapping
  public ResponseEntity<ApiResponse> updateCustomer(@RequestBody @Valid CustomerUserRequest request) {
    Customer entity = customerMapper.toEntity(request);
    customerUserService.updateCustomer(entity);
    ApiResponse response = new ApiResponse(0L, "Update Customer", HttpStatus.OK.value(), "Success",
        "Customer has been updated.", null);
    return ResponseEntity.ok(response);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse> deleteCustomer(@PathVariable @Valid Long id) {
    customerUserService.deleteCustomer(id);
    ApiResponse response = new ApiResponse(0L, "Delete Customer", HttpStatus.OK.value(), "Success",
        "Customer has been deleted.", null);
    return ResponseEntity.ok(response);
  }

  @PostMapping("/change-password")
  public ResponseEntity<ApiResponse> changeCustomerUserPassword(@RequestParam("username") @Valid String username,
      @RequestParam("oldPassword") @Valid String oldPassword,
      @RequestParam("newPassword") @Valid String newPassword) {
    boolean isValidUser = customerLoginService.isValidCustomerUser(username.toLowerCase(), oldPassword);
    if (!isValidUser) {
      ApiResponse response = new ApiResponse(0L, "Change Password", HttpStatus.FORBIDDEN.value(), "Error",
          "Invalid user credentials.", null);
      return new ResponseEntity<>(response, HttpStatus.FORBIDDEN);
    }
    byte[] byteSalt = null;
    try {
      byteSalt = securityService.getSalt();
    } catch (NoSuchAlgorithmException ex) {
      logger.error("Salt generation error", ex);
      ApiResponse response = new ApiResponse(0L, "Change Password", HttpStatus.INTERNAL_SERVER_ERROR.value(), "Error",
          "Salt generation error.", null);
      return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
    byte[] byteDigestPsw = securityService.getSaltedHashSHA512(newPassword, byteSalt);
    String strDigestPsw = securityService.toHex(byteDigestPsw);
    String strSalt = securityService.toHex(byteSalt);
    customerUserService.changeCustomerPassword(strDigestPsw, strSalt, username.toLowerCase());
    ApiResponse response = new ApiResponse(0L, "Change Password", HttpStatus.OK.value(), "Success",
        "Password has been changed.", null);
    return ResponseEntity.ok(response);
  }
}
