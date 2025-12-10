/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.catalog.controller;

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

import com.xplaza.backend.catalog.domain.entity.Currency;
import com.xplaza.backend.catalog.dto.request.CurrencyRequest;
import com.xplaza.backend.catalog.dto.response.CurrencyResponse;
import com.xplaza.backend.catalog.mapper.CurrencyMapper;
import com.xplaza.backend.catalog.service.CurrencyService;
import com.xplaza.backend.common.util.ApiResponse;
import com.xplaza.backend.common.util.ApiResponse.PageMeta;

/**
 * Currency Controller - Clean REST API design.
 */
@RestController
@RequestMapping("/api/v1/currencies")
@RequiredArgsConstructor
@Validated
@Tag(name = "Currency Management", description = "APIs for managing currencies")
public class CurrencyController {

  private final CurrencyService currencyService;
  private final CurrencyMapper currencyMapper;

  @GetMapping
  @Operation(summary = "List currencies", description = "Get paginated list of currencies")
  public ResponseEntity<ApiResponse<List<CurrencyResponse>>> getCurrencies(
      @RequestParam(required = false) String search,
      @RequestParam(defaultValue = "0") @Min(0) int page,
      @RequestParam(defaultValue = "50") @Min(1) int size,
      @RequestParam(defaultValue = "currencyName") String sort,
      @RequestParam(defaultValue = "ASC") Sort.Direction direction) {

    size = Math.min(size, 200);
    Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sort));

    List<Currency> allCurrencies = currencyService.listCurrencies();

    if (search != null && !search.isBlank()) {
      String searchLower = search.toLowerCase().trim();
      allCurrencies = allCurrencies.stream()
          .filter(c -> c.getCurrencyName().toLowerCase().contains(searchLower) ||
              (c.getCurrencySign() != null && c.getCurrencySign().contains(search)))
          .toList();
    }

    int start = (int) pageable.getOffset();
    int end = Math.min(start + pageable.getPageSize(), allCurrencies.size());
    List<Currency> pageContent = start < allCurrencies.size() ? allCurrencies.subList(start, end) : List.of();

    List<CurrencyResponse> dtos = pageContent.stream()
        .map(currencyMapper::toResponse)
        .toList();

    Page<CurrencyResponse> responsePage = new PageImpl<>(dtos, pageable, allCurrencies.size());
    PageMeta pageMeta = PageMeta.from(responsePage);

    return ResponseEntity.ok(ApiResponse.ok(dtos, pageMeta));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get currency by ID", description = "Retrieve a specific currency")
  public ResponseEntity<ApiResponse<CurrencyResponse>> getCurrency(@PathVariable @Positive Long id) {
    Currency currency = currencyService.listCurrency(id);
    CurrencyResponse dto = currencyMapper.toResponse(currency);
    return ResponseEntity.ok(ApiResponse.ok(dto));
  }

  @PostMapping
  @Operation(summary = "Create currency", description = "Create a new currency")
  public ResponseEntity<ApiResponse<Void>> createCurrency(@RequestBody @Valid CurrencyRequest request) {
    Currency entity = currencyMapper.toEntity(request);
    currencyService.addCurrency(entity);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Currency created successfully"));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update currency", description = "Update an existing currency")
  public ResponseEntity<ApiResponse<Void>> updateCurrency(
      @PathVariable @Positive Long id,
      @RequestBody @Valid CurrencyRequest request) {
    Currency entity = currencyMapper.toEntity(request);
    currencyService.updateCurrency(id, entity);
    return ResponseEntity.ok(ApiResponse.ok("Currency updated successfully"));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete currency", description = "Delete a currency")
  public ResponseEntity<ApiResponse<Void>> deleteCurrency(@PathVariable @Positive Long id) {
    currencyService.deleteCurrency(id);
    return ResponseEntity.ok(ApiResponse.ok("Currency deleted successfully"));
  }
}
