/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.exception;

public class InvalidJwtTokenException extends BusinessException {
  public InvalidJwtTokenException(Object... params) {
    super("invalidJwtToken", params);
  }
}
