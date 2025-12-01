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
import com.xplaza.backend.http.dto.request.CurrencyRequest;
import com.xplaza.backend.http.dto.response.CurrencyResponse;
import com.xplaza.backend.mapper.CurrencyMapper;
import com.xplaza.backend.service.CurrencyService;
import com.xplaza.backend.service.entity.Currency;

/**
 * V2 Currency Controller - Clean REST API design.
 */
@RestController
@RequestMapping("/api/v2/currencies")
@RequiredArgsConstructor
@Validated
@Tag(name = "Currency Management V2", description = "V2 APIs for managing currencies")
public class CurrencyControllerV2 {

  private final CurrencyService currencyService;
  private final CurrencyMapper currencyMapper;

  @GetMapping
  @Operation(summary = "List currencies", description = "Get paginated list of currencies")
  public ResponseEntity<ApiResponseV2<List<CurrencyResponse>>> getCurrencies(
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

    return ResponseEntity.ok(ApiResponseV2.ok(dtos, pageMeta));
  }

  @GetMapping("/{id}")
  @Operation(summary = "Get currency by ID", description = "Retrieve a specific currency")
  public ResponseEntity<ApiResponseV2<CurrencyResponse>> getCurrency(@PathVariable @Positive Long id) {
    Currency currency = currencyService.listCurrency(id);
    CurrencyResponse dto = currencyMapper.toResponse(currency);
    return ResponseEntity.ok(ApiResponseV2.ok(dto));
  }

  @PostMapping
  @Operation(summary = "Create currency", description = "Create a new currency")
  public ResponseEntity<ApiResponseV2<Void>> createCurrency(@RequestBody @Valid CurrencyRequest request) {
    Currency entity = currencyMapper.toEntity(request);
    currencyService.addCurrency(entity);
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponseV2.ok("Currency created successfully"));
  }

  @PutMapping("/{id}")
  @Operation(summary = "Update currency", description = "Update an existing currency")
  public ResponseEntity<ApiResponseV2<Void>> updateCurrency(
      @PathVariable @Positive Long id,
      @RequestBody @Valid CurrencyRequest request) {
    Currency entity = currencyMapper.toEntity(request);
    currencyService.updateCurrency(id, entity);
    return ResponseEntity.ok(ApiResponseV2.ok("Currency updated successfully"));
  }

  @DeleteMapping("/{id}")
  @Operation(summary = "Delete currency", description = "Delete a currency")
  public ResponseEntity<ApiResponseV2<Void>> deleteCurrency(@PathVariable @Positive Long id) {
    currencyService.deleteCurrency(id);
    return ResponseEntity.ok(ApiResponseV2.ok("Currency deleted successfully"));
  }
}
