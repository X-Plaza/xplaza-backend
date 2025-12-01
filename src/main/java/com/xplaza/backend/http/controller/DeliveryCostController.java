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

import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.http.dto.request.DeliveryCostRequest;
import com.xplaza.backend.http.dto.response.DeliveryCostResponse;
import com.xplaza.backend.mapper.DeliveryCostMapper;
import com.xplaza.backend.service.DeliveryCostService;
import com.xplaza.backend.service.entity.DeliveryCost;

@RestController
@RequestMapping("/api/v1/delivery-costs")
public class DeliveryCostController extends BaseController {
  @Autowired
  private DeliveryCostService deliveryCostService;
  @Autowired
  private DeliveryCostMapper deliveryCostMapper;

  @GetMapping
  public ResponseEntity<List<DeliveryCostResponse>> getDeliveryCosts() {
    List<DeliveryCost> entities = deliveryCostService.listDeliveryCosts();
    List<DeliveryCostResponse> dtos = entities.stream().map(deliveryCostMapper::toResponse).toList();
    return ResponseEntity.ok(dtos);
  }

  @GetMapping("/{id}")
  public ResponseEntity<DeliveryCostResponse> getDeliveryCost(@PathVariable @Valid Long id) {
    DeliveryCost entity = deliveryCostService.listDeliveryCost(id);
    DeliveryCostResponse dto = deliveryCostMapper.toResponse(entity);
    return ResponseEntity.ok(dto);
  }

  @PostMapping
  public ResponseEntity<ApiResponse> addDeliveryCost(
      @RequestBody @Valid DeliveryCostRequest deliveryCostRequest) {
    long start = System.currentTimeMillis();
    DeliveryCost entity = deliveryCostMapper.toEntity(deliveryCostRequest);
    deliveryCostService.addDeliveryCost(entity);
    long responseTime = System.currentTimeMillis() - start;
    return new ResponseEntity<>(new ApiResponse(responseTime, "Add Delivery Cost", HttpStatus.CREATED.value(),
        "Success", "Delivery Cost has been created.", null), HttpStatus.CREATED);
  }

  @PutMapping
  public ResponseEntity<ApiResponse> updateDeliveryCost(
      @RequestBody @Valid DeliveryCostRequest deliveryCostRequest) {
    long start = System.currentTimeMillis();
    DeliveryCost entity = deliveryCostMapper.toEntity(deliveryCostRequest);
    deliveryCostService.updateDeliveryCost(entity);
    long responseTime = System.currentTimeMillis() - start;
    return new ResponseEntity<>(new ApiResponse(responseTime, "Update Delivery Cost", HttpStatus.OK.value(),
        "Success", "Delivery Cost has been updated.", null), HttpStatus.OK);
  }

  @DeleteMapping("/{id}")
  public ResponseEntity<ApiResponse> deleteDeliveryCost(@PathVariable @Valid Long id) {
    String delivery_slab = deliveryCostService.getDeliverySlabRangeNameByID(id);
    long start = System.currentTimeMillis();
    deliveryCostService.deleteDeliveryCost(id);
    long responseTime = System.currentTimeMillis() - start;
    return new ResponseEntity<>(new ApiResponse(responseTime, "Delete Delivery Cost", HttpStatus.OK.value(),
        "Success", "Delivery cost of " + delivery_slab + " order range has been deleted.", null), HttpStatus.OK);
  }
}
