/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.exception;

public class ResourceAlreadyExistsException extends RuntimeException {
  public ResourceAlreadyExistsException(String message) {
    super(message);
  }
}
