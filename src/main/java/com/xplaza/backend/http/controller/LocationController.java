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
import com.xplaza.backend.http.dto.response.LocationResponse;
import com.xplaza.backend.mapper.LocationMapper;
import com.xplaza.backend.service.LocationService;
import com.xplaza.backend.service.entity.Location;

@Deprecated(since = "1.0", forRemoval = true)
@RestController
@RequestMapping("/api/v1/locations")
public class LocationController extends BaseController {
  @Autowired
  private LocationService locationService;

  @Autowired
  private LocationMapper locationMapper;

  @GetMapping
  public ResponseEntity<String> getLocations() throws JsonProcessingException {
    long start = System.currentTimeMillis();
    List<Location> entities = locationService.listLocations();
    List<LocationResponse> dtos = entities.stream().map(locationMapper::toResponse).toList();
    long responseTime = System.currentTimeMillis() - start;
    ObjectMapper mapper = new ObjectMapper();
    String response = "{\n" +
        "  \"responseTime\": " + responseTime + ",\n" +
        "  \"responseType\": \"Location List\",\n" +
        "  \"status\": 200,\n" +
        "  \"response\": \"Success\",\n" +
        "  \"msg\": \"\",\n" +
        "  \"data\":" + mapper.writeValueAsString(dtos) + "\n}";
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @GetMapping("/{id}")
  public ResponseEntity<String> getLocation(@PathVariable @Valid Long id) throws JsonProcessingException {
    long start = System.currentTimeMillis();
    Location entity = locationService.listLocation(id);
    LocationResponse dto = locationMapper.toResponse(entity);
    long responseTime = System.currentTimeMillis() - start;
    ObjectMapper mapper = new ObjectMapper();
    String response = "{\n" +
        "  \"responseTime\": " + responseTime + ",\n" +
        "  \"responseType\": \"Location By ID\",\n" +
        "  \"status\": 200,\n" +
        "  \"response\": \"Success\",\n" +
        "  \"msg\": \"\",\n" +
        "  \"data\":" + mapper.writeValueAsString(dto) + "\n}";
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @PostMapping
  public ResponseEntity<ApiResponse> addLocation(@RequestBody @Valid Location location) {
    long start = System.currentTimeMillis();
    locationService.addLocation(location);
    long responseTime = System.currentTimeMillis() - start;
    return new ResponseEntity<>(new ApiResponse(responseTime, "Add Location", HttpStatus.CREATED.value(),
        "Success", "Location has been created.", null), HttpStatus.CREATED);
  }

  @PutMapping("/{id}")
  public ResponseEntity<ApiResponse> updateLocation(@PathVariable Long id, @RequestBody @Valid Location location) {
    long start = System.currentTimeMillis();
    locationService.updateLocation(id, location);
    long responseTime = System.currentTimeMillis() - start;
    return new ResponseEntity<>(new ApiResponse(responseTime, "Update Location", HttpStatus.OK.value(),
        "Success", "Location has been updated.", null), HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse> deleteLocation(@PathVariable @Valid Long id) {
    long start = System.currentTimeMillis();
    locationService.deleteLocation(id);
    long responseTime = System.currentTimeMillis() - start;
    return new ResponseEntity<>(new ApiResponse(responseTime, "Delete Location", HttpStatus.OK.value(),
        "Success", "Location has been deleted.", null), HttpStatus.OK);
  }
}