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
import com.xplaza.backend.http.dto.request.DeliveryCostRequest;
import com.xplaza.backend.http.dto.response.DeliveryCostResponse;
import com.xplaza.backend.mapper.DeliveryCostMapper;
import com.xplaza.backend.service.DeliveryCostService;
import com.xplaza.backend.service.entity.DeliveryCost;

/**
 * V2 Delivery Cost Controller - Clean REST API design.
 */
@RestController
@RequestMapping("/api/v2/delivery-costs")
@RequiredArgsConstructor
@Validated
@Tag(name = "Delivery Cost Management V2", description = "V2 APIs for managing delivery costs")
public class DeliveryCostControllerV2 {

  private final DeliveryCostService deliveryCostService;
  private final DeliveryCostMapper deliveryCostMapper;

  @GetMapping
  @Operation(summary = "List delivery costs", description = "Get paginated list of delivery costs")
  public ResponseEntity<ApiResponseV2<List<DeliveryCostResponse>>> getDeliveryCosts(
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "20") @Min(1) int size,
      @RequestParam(defaultValue = "deliveryCostId") String sort,
      @RequestParam(defaultValue = "ASC") Sort.Direction direction) {

    size = Math.min(size, 100);
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

    List<DeliveryCost> allCosts = deliveryCostService.listDeliveryCosts();

    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), allCosts.size());
    List<DeliveryCost> pageContent = start < allCosts.size() ? allCosts.subList(start, end) : List.of();

    List<DeliveryCostResponse> dtos = pageContent.stream()
        .map(deliveryCostMapper::toResponse)
        .toList();

    Page<DeliveryCostResponse> responsePage = new PageImpl<>(dtos, pageable, allCosts.size());
    PageMeta pageMeta = PageMeta.from(responsePage);

    return ResponseEntity.ok(ApiResponseV2.ok(dtos, pageMeta));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get delivery cost by ID", description = "Retrieve a specific delivery cost")
  public ResponseEntity<ApiResponseV2<DeliveryCostResponse>> getDeliveryCost(@PathVariable @Positive Long id) {
    DeliveryCost cost = deliveryCostService.listDeliveryCost(id);
    DeliveryCostResponse dto = deliveryCostMapper.toResponse(cost);
    return ResponseEntity.ok(ApiResponseV2.ok(dto));
  }

  @PostMapping
  @Operation(summary = "Create delivery cost", description = "Create a new delivery cost")
  public ResponseEntity<ApiResponseV2<Void>> createDeliveryCost(@RequestBody @Valid DeliveryCostRequest request) {
    DeliveryCost entity = deliveryCostMapper.toEntity(request);
    deliveryCostService.addDeliveryCost(entity);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseV2.ok("Delivery cost created successfully"));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update delivery cost", description = "Update an existing delivery cost")
  public ResponseEntity<ApiResponseV2<Void>> updateDeliveryCost(
      @PathVariable @Positive Long id,
      @RequestBody @Valid DeliveryCostRequest request) {
    DeliveryCost entity = deliveryCostMapper.toEntity(request);
    entity.setDeliveryCostId(id);
    deliveryCostService.updateDeliveryCost(entity);
    return ResponseEntity.ok(ApiResponseV2.ok("Delivery cost updated successfully"));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete delivery cost", description = "Delete a delivery cost")
  public ResponseEntity<ApiResponseV2<Void>> deleteDeliveryCost(@PathVariable @Positive Long id) {
    String deliverySlab = deliveryCostService.getDeliverySlabRangeNameByID(id);
    deliveryCostService.deleteDeliveryCost(id);
    return ResponseEntity.ok(ApiResponseV2.ok("Delivery cost of " + deliverySlab + " order range has been deleted"));
  }
}
