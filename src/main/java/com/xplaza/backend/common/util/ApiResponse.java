/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.common.util;

import java.time.Instant;

import lombok.Getter;
import lombok.Setter;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Generic API response wrapper for consistent response structure.
 *
 * @param <T> The type of data being returned
 */
@Getter
@Setter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
  private long responseTime;
  private String responseType;
  private int status;
  private String response;
  private String message;
  private T data;
  private String timestamp;

  public ApiResponse(long responseTime, String responseType, int status, String response, String message, T data) {
    this.responseTime = responseTime;
    this.responseType = responseType;
    this.status = status;
    this.response = response;
    this.message = message;
    this.data = data;
    this.timestamp = Instant.now().toString();
  }

  /**
   * Factory method for success responses with data
   */
  public static <T> ApiResponse<T> success(String responseType, T data, long responseTime) {
    return new ApiResponse<>(responseTime, responseType, 200, "Success", "", data);
  }

  /**
   * Factory method for success responses with message
   */
  public static <T> ApiResponse<T> success(String responseType, String message, long responseTime) {
    return new ApiResponse<>(responseTime, responseType, 200, "Success", message, null);
  }

  /**
   * Factory method for created responses
   */
  public static <T> ApiResponse<T> created(String responseType, String message, long responseTime) {
    return new ApiResponse<>(responseTime, responseType, 201, "Success", message, null);
  }

  /**
   * Factory method for error responses
   */
  public static <T> ApiResponse<T> error(String responseType, int status, String message, long responseTime) {
    return new ApiResponse<>(responseTime, responseType, status, "Error", message, null);
  }
}
