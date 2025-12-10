/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.common.util;

import java.time.Instant;

import lombok.Getter;

import org.springframework.data.domain.Page;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

/**
 * Clean API response wrapper following REST best practices. Minimal structure:
 * success/error status, data, and optional message.
 *
 * @param <T> The type of data being returned
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({ "success", "data", "error", "meta" })
public class ApiResponse<T> {

  private final boolean success;
  private final T data;
  private final ErrorInfo error;
  private final Meta meta;

  private ApiResponse(boolean success, T data, ErrorInfo error, Meta meta) {
    this.success = success;
    this.data = data;
    this.error = error;
    this.meta = meta;
  }

  // ===== Success Responses =====

  /**
   * Success response with data only (for single resource)
   */
  public static <T> ApiResponse<T> ok(T data) {
    return new ApiResponse<>(true, data, null, null);
  }

  /**
   * Success response with data and pagination meta
   */
  public static <T> ApiResponse<T> ok(T data, PageMeta pagination) {
    return new ApiResponse<>(true, data, null, new Meta(pagination, null));
  }

  /**
   * Success response for create operations (201)
   */
  public static <T> ApiResponse<T> created(T data) {
    return new ApiResponse<>(true, data, null, null);
  }

  /**
   * Success response for delete/update with message
   */
  public static ApiResponse<Void> ok(String message) {
    return new ApiResponse<>(true, null, null, new Meta(null, message));
  }

  /**
   * Empty success response (for 204 No Content scenarios)
   */
  public static ApiResponse<Void> noContent() {
    return new ApiResponse<>(true, null, null, null);
  }

  // ===== Error Responses =====

  /**
   * Error response with code, message, and optional details
   */
  public static <T> ApiResponse<T> error(String code, String message) {
    return new ApiResponse<>(false, null, new ErrorInfo(code, message, null), null);
  }

  /**
   * Error response with validation details
   */
  public static <T> ApiResponse<T> error(String code, String message, Object details) {
    return new ApiResponse<>(false, null, new ErrorInfo(code, message, details), null);
  }

  // ===== Nested Classes =====

  @Getter
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class ErrorInfo {
    private final String code;
    private final String message;
    private final Object details;
    private final String timestamp;

    public ErrorInfo(String code, String message, Object details) {
      this.code = code;
      this.message = message;
      this.details = details;
      this.timestamp = Instant.now().toString();
    }
  }

  @Getter
  @JsonInclude(JsonInclude.Include.NON_NULL)
  public static class Meta {
    private final PageMeta pagination;
    private final String message;

    public Meta(PageMeta pagination, String message) {
      this.pagination = pagination;
      this.message = message;
    }
  }

  @Getter
  public static class PageMeta {
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean hasNext;
    private final boolean hasPrevious;

    public PageMeta(int page, int size, long totalElements, int totalPages) {
      this.page = page;
      this.size = size;
      this.totalElements = totalElements;
      this.totalPages = totalPages;
      this.hasNext = page < totalPages - 1;
      this.hasPrevious = page > 0;
    }

    /**
     * Create from Spring Data Page
     */
    public static PageMeta from(Page<?> page) {
      return new PageMeta(
          page.getNumber(),
          page.getSize(),
          page.getTotalElements(),
          page.getTotalPages());
    }
  }
}
