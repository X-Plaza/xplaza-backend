/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.delivery.controller;

import java.util.List;

import jakarta.validation.Valid;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.delivery.domain.entity.DeliveryCost;
import com.xplaza.backend.delivery.dto.request.DeliveryCostRequest;
import com.xplaza.backend.delivery.dto.response.DeliveryCostResponse;
import com.xplaza.backend.delivery.mapper.DeliveryCostMapper;
import com.xplaza.backend.delivery.service.DeliveryCostService;

@RestController
@RequestMapping("/api/v1/delivery-costs")
@RequiredArgsConstructor
@Tag(name = "Delivery Cost Management", description = "APIs for managing delivery costs")
public class DeliveryCostController {
  private final DeliveryCostService deliveryCostService;
  private final DeliveryCostMapper deliveryCostMapper;

  @GetMapping
  @Operation(summary = "List all delivery costs")
  public ResponseEntity<ApiResponse<List<DeliveryCostResponse>>> listDeliveryCosts(
      @RequestParam(required = false) Long shopId,
      @RequestParam(required = false) Long cityId) {
    List<DeliveryCost> costs;
    if (shopId != null) {
      costs = deliveryCostService.listDeliveryCostsByShop(shopId);
    } else if (cityId != null) {
      costs = deliveryCostService.listDeliveryCostsByCity(cityId);
    } else {
      costs = deliveryCostService.listDeliveryCosts();
    }
    List<DeliveryCostResponse> response = costs.stream().map(deliveryCostMapper::toResponse).toList();
    return ResponseEntity.ok(ApiResponse.ok(response));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get delivery cost by ID")
  public ResponseEntity<ApiResponse<DeliveryCostResponse>> getDeliveryCost(@PathVariable Long id) {
    DeliveryCost cost = deliveryCostService.getDeliveryCost(id);
    return ResponseEntity.ok(ApiResponse.ok(deliveryCostMapper.toResponse(cost)));
  }

  @PostMapping
  @Operation(summary = "Create a new delivery cost")
  public ResponseEntity<ApiResponse<DeliveryCostResponse>> createDeliveryCost(
      @RequestBody @Valid DeliveryCostRequest request) {
    DeliveryCost entity = deliveryCostMapper.toEntity(request);
    DeliveryCost saved = deliveryCostService.createDeliveryCost(entity, request.getShopId(), request.getCityId());
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.created(deliveryCostMapper.toResponse(saved)));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update an existing delivery cost")
  public ResponseEntity<ApiResponse<DeliveryCostResponse>> updateDeliveryCost(@PathVariable Long id,
      @RequestBody @Valid DeliveryCostRequest request) {
    DeliveryCost details = deliveryCostMapper.toEntity(request);
    DeliveryCost updated = deliveryCostService.updateDeliveryCost(id, details);
    return ResponseEntity.ok(ApiResponse.ok(deliveryCostMapper.toResponse(updated)));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete a delivery cost")
  public ResponseEntity<ApiResponse<Void>> deleteDeliveryCost(@PathVariable Long id) {
    deliveryCostService.deleteDeliveryCost(id);
    return ResponseEntity.ok(ApiResponse.ok("Delivery cost deleted"));
  }
}
