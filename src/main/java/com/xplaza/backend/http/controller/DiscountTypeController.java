/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller;

import java.util.Date;
import java.util.List;

import jakarta.servlet.http.HttpServletResponse;

import org.json.JSONException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xplaza.backend.http.dto.response.DiscountTypeResponse;
import com.xplaza.backend.mapper.DiscountTypeMapper;
import com.xplaza.backend.service.DiscountTypeService;
import com.xplaza.backend.service.entity.DiscountType;

@Deprecated(since = "1.0", forRemoval = true)
@RestController
@RequestMapping("/api/v1/discount-types")
public class DiscountTypeController {
  private final DiscountTypeService discountTypeService;
  private final DiscountTypeMapper discountTypeMapper;

  @Autowired
  public DiscountTypeController(DiscountTypeService discountTypeService, DiscountTypeMapper discountTypeMapper) {
    this.discountTypeService = discountTypeService;
    this.discountTypeMapper = discountTypeMapper;
  }

  @ModelAttribute
  public void setResponseHeader(HttpServletResponse response) {
    response.setHeader("Cache-Control", "no-store"); // HTTP 1.1.
    response.setHeader("Pragma", "no-cache"); // HTTP 1.0.
    response.setHeader("Expires", "0"); // Proxies.
    response.setHeader("Content-Type", "application/json");
    response.setHeader("Set-Cookie", "type=ninja");
  }

  @GetMapping
  public ResponseEntity<String> getDiscountTypes() throws JsonProcessingException, JSONException {
    Date start = new Date();
    List<DiscountType> entities = discountTypeService.listDiscountTypes();
    List<DiscountTypeResponse> dtos = entities.stream().map(discountTypeMapper::toResponse).toList();
    Date end = new Date();
    long responseTime = end.getTime() - start.getTime();
    ObjectMapper mapper = new ObjectMapper();
    String response = "{\n" +
        "  \"responseTime\": " + responseTime + ",\n" +
        "  \"responseType\": \"Discount Type List\",\n" +
        "  \"status\": 200,\n" +
        "  \"response\": \"Success\",\n" +
        "  \"msg\": \"\",\n" +
        "  \"data\":" + mapper.writeValueAsString(dtos) + "\n}";
    return new ResponseEntity<>(response, HttpStatus.OK);
  }
}
