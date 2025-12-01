/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.http.controller;

import static org.json.XMLTokener.entity;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.http.dto.request.DeliveryScheduleRequest;
import com.xplaza.backend.http.dto.response.DeliveryScheduleResponse;
import com.xplaza.backend.mapper.DeliveryScheduleMapper;
import com.xplaza.backend.service.DeliveryScheduleService;
import com.xplaza.backend.service.entity.DeliverySchedule;

@Deprecated(since = "1.0", forRemoval = true)
@RestController
@RequestMapping("/api/v1/delivery-schedules")
public class DeliveryScheduleController extends BaseController {
  private final DeliveryScheduleService deliveryScheduleService;
  private final DeliveryScheduleMapper deliveryScheduleMapper;

  @Autowired
  public DeliveryScheduleController(DeliveryScheduleService deliveryScheduleService,
      DeliveryScheduleMapper deliveryScheduleMapper) {
    this.deliveryScheduleService = deliveryScheduleService;
    this.deliveryScheduleMapper = deliveryScheduleMapper;
  }

  @GetMapping
  public ResponseEntity<ApiResponse> getDeliverySchedules() throws JsonProcessingException {
    long start = System.currentTimeMillis();
    var entities = deliveryScheduleService.listDeliverySchedules();
    var dtos = entities.stream().map(deliveryScheduleMapper::toResponse).toList();
    long responseTime = System.currentTimeMillis() - start;
    String data = new ObjectMapper().writeValueAsString(dtos);
    ApiResponse response = new ApiResponse(responseTime, "Delivery Schedule List", HttpStatus.OK.value(), "Success", "",
        data);
    return ResponseEntity.ok(response);
  }

  @GetMapping("/{id}")
  public ResponseEntity<ApiResponse> getDeliverySchedule(@PathVariable @Valid Long id) throws JsonProcessingException {
    long start = System.currentTimeMillis();
    DeliverySchedule entity = deliveryScheduleService.listDeliverySchedule(id);
    DeliveryScheduleResponse dto = deliveryScheduleMapper.toResponse(entity);
    long responseTime = System.currentTimeMillis() - start;
    String data = new ObjectMapper().writeValueAsString(dto);
    ApiResponse response = new ApiResponse(responseTime, "Delivery Schedule By ID", HttpStatus.OK.value(), "Success",
        "", data);
    return ResponseEntity.ok(response);
  }

  @PostMapping
  public ResponseEntity<ApiResponse> addSchedule(
      @RequestBody @Valid DeliveryScheduleRequest deliveryScheduleRequest) {
    long start = System.currentTimeMillis();
    DeliverySchedule deliverySchedule = deliveryScheduleMapper.toEntity(deliveryScheduleRequest);
    deliveryScheduleService.addSchedule(deliverySchedule);
    long responseTime = System.currentTimeMillis() - start;
    return new ResponseEntity<>(new ApiResponse(responseTime, "Add Delivery Schedule", HttpStatus.CREATED.value(),
        "Success", "Delivery Schedule has been created.", null), HttpStatus.CREATED);
  }

  @PutMapping
  public ResponseEntity<ApiResponse> updateSchedule(
      @RequestBody @Valid DeliveryScheduleRequest deliveryScheduleRequest) {
    long start = System.currentTimeMillis();
    DeliverySchedule entity = deliveryScheduleMapper.toEntity(deliveryScheduleRequest);
    deliveryScheduleService.updateSchedule(entity);
    long responseTime = System.currentTimeMillis() - start;
    return new ResponseEntity<>(new ApiResponse(responseTime, "Update Delivery Schedule", HttpStatus.OK.value(),
        "Success", "Delivery Schedule has been updated.", null), HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse> deleteSchedule(@PathVariable @Valid Long id) {
    long start = System.currentTimeMillis();
    deliveryScheduleService.deleteSchedule(id);
    long responseTime = System.currentTimeMillis() - start;
    return new ResponseEntity<>(new ApiResponse(responseTime, "Delete Delivery Schedule", HttpStatus.OK.value(),
        "Success", "Delivery Schedule has been deleted.", null), HttpStatus.OK);
  }

}
