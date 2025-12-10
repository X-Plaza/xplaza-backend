/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.common.util;

import com.xplaza.backend.exception.ValidationException;

public class ValidationUtil {

  public static void validateNotNull(Object value, String fieldName) {
    if (value == null) {
      throw new ValidationException(fieldName + " cannot be null");
    }
  }

  public static void validateNotEmpty(String value, String fieldName) {
    if (value == null || value.trim().isEmpty()) {
      throw new ValidationException(fieldName + " cannot be null or empty");
    }
  }

  public static void validatePositive(Number value, String fieldName) {
    if (value == null || value.doubleValue() <= 0) {
      throw new ValidationException(fieldName + " must be a positive value");
    }
  }

  public static void validateNonNegative(Number value, String fieldName) {
    if (value == null || value.doubleValue() < 0) {
      throw new ValidationException(fieldName + " must be a non-negative value");
    }
  }

  public static void validateId(Long id, String fieldName) {
    if (id == null || id <= 0) {
      throw new ValidationException(fieldName + " must be a valid positive ID");
    }
  }

  public static void validateEmail(String email) {
    if (email == null || !email.matches("^[A-Za-z0-9+_.-]+@(.+)$")) {
      throw new ValidationException("Invalid email format");
    }
  }

  public static void validatePhoneNumber(String phoneNumber) {
    if (phoneNumber == null || !phoneNumber.matches("^[+]?[0-9]{10,15}$")) {
      throw new ValidationException("Invalid phone number format");
    }
  }
}
