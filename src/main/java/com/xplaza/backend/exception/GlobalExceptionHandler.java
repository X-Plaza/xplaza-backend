/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.exception;

import java.util.HashMap;
import java.util.Map;

import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import com.xplaza.backend.common.util.ApiResponse;

/**
 * Global exception handler for REST API. Provides consistent error response
 * format across all endpoints.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

  /**
   * Handle resource already exists (409)
   */
  @ExceptionHandler(ResourceAlreadyExistsException.class)
  public ResponseEntity<ApiResponse<Void>> handleResourceAlreadyExists(ResourceAlreadyExistsException ex) {
    log.warn("Resource already exists: {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.CONFLICT)
        .body(ApiResponse.error("RESOURCE_ALREADY_EXISTS", ex.getMessage()));
  }

  /**
   * Handle bad credentials (401)
   */
  @ExceptionHandler(BadCredentialsException.class)
  public ResponseEntity<ApiResponse<Void>> handleBadCredentials(
      BadCredentialsException ex) {
    log.warn("Bad credentials: {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.error("BAD_CREDENTIALS", "Invalid username or password"));
  }

  /**
   * Handle illegal state (400) - e.g. Insufficient stock if not using specific
   * exception
   */
  @ExceptionHandler(IllegalStateException.class)
  public ResponseEntity<ApiResponse<Void>> handleIllegalState(IllegalStateException ex) {
    log.warn("Illegal state: {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error("ILLEGAL_STATE", ex.getMessage()));
  }

  /**
   * Handle resource not found (404)
   */
  @ExceptionHandler(ResourceNotFoundException.class)
  public ResponseEntity<ApiResponse<Void>> handleResourceNotFound(ResourceNotFoundException ex) {
    log.warn("Resource not found: {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.NOT_FOUND)
        .body(ApiResponse.error("RESOURCE_NOT_FOUND", ex.getMessage()));
  }

  /**
   * Handle insufficient inventory (422)
   */
  @ExceptionHandler(InsufficientInventoryException.class)
  public ResponseEntity<ApiResponse<Void>> handleInsufficientInventory(InsufficientInventoryException ex) {
    log.warn("Insufficient inventory: productId={}, requested={}, available={}",
        ex.getProductId(), ex.getRequestedQuantity(), ex.getAvailableQuantity());

    var details = new java.util.LinkedHashMap<String, Object>();
    details.put("productId", ex.getProductId());
    details.put("productName", ex.getProductName());
    details.put("requestedQuantity", ex.getRequestedQuantity());
    details.put("availableQuantity", ex.getAvailableQuantity());

    return ResponseEntity
        .status(HttpStatus.UNPROCESSABLE_ENTITY)
        .body(ApiResponse.error("INSUFFICIENT_INVENTORY", ex.getMessage(), details));
  }

  /**
   * Handle validation errors (400)
   */
  @ExceptionHandler(MethodArgumentNotValidException.class)
  public ResponseEntity<ApiResponse<Void>> handleValidationErrors(MethodArgumentNotValidException ex) {
    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getAllErrors().forEach(error -> {
      String fieldName = ((FieldError) error).getField();
      String errorMessage = error.getDefaultMessage();
      errors.put(fieldName, errorMessage);
    });

    log.warn("Validation failed: {}", errors);
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error("VALIDATION_ERROR", "Validation failed", errors));
  }

  /**
   * Handle missing request parameters (400)
   */
  @ExceptionHandler(MissingServletRequestParameterException.class)
  public ResponseEntity<ApiResponse<Void>> handleMissingParams(MissingServletRequestParameterException ex) {
    log.warn("Missing parameter: {}", ex.getParameterName());
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error("MISSING_PARAMETER",
            String.format("Required parameter '%s' is missing", ex.getParameterName())));
  }

  /**
   * Handle type mismatch (400)
   */
  @ExceptionHandler(MethodArgumentTypeMismatchException.class)
  public ResponseEntity<ApiResponse<Void>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
    log.warn("Type mismatch for parameter: {}", ex.getName());
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error("TYPE_MISMATCH",
            String.format("Parameter '%s' should be of type '%s'",
                ex.getName(), ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "unknown")));
  }

  /**
   * Handle illegal arguments (400)
   */
  @ExceptionHandler(IllegalArgumentException.class)
  public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException ex) {
    log.warn("Illegal argument: {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.BAD_REQUEST)
        .body(ApiResponse.error("INVALID_ARGUMENT", ex.getMessage()));
  }

  /**
   * Handle authentication failures (401)
   */
  @ExceptionHandler(AuthenticationException.class)
  public ResponseEntity<ApiResponse<Void>> handleAuthenticationFailure(AuthenticationException ex) {
    log.warn("Authentication failed: {}", ex.getMessage());
    return ResponseEntity
        .status(HttpStatus.UNAUTHORIZED)
        .body(ApiResponse.error("AUTHENTICATION_FAILED", ex.getMessage()));
  }

  /**
   * Catch-all for unexpected errors (500)
   */
  @ExceptionHandler(Exception.class)
  public ResponseEntity<ApiResponse<Void>> handleGenericException(Exception ex) {
    log.error("Unexpected error occurred", ex);
    return ResponseEntity
        .status(HttpStatus.INTERNAL_SERVER_ERROR)
        .body(ApiResponse.error("INTERNAL_ERROR", "An unexpected error occurred. Please try again later."));
  }
}
