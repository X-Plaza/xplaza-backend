/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.backend.xplaza.exception;

/**
 * Base exception class for all exceptions which are expected based on the
 * business case (e.g. validation errors)
 */
public abstract class BusinessException extends RuntimeException {
  private final String messageKey;
  private final transient Object[] messageParams;

  protected BusinessException(String messageKey) {
    this.messageKey = messageKey;
    this.messageParams = new Object[0];
  }

  protected BusinessException(String messageKey, Object... messageParams) {
    this.messageKey = messageKey;
    this.messageParams = messageParams;
  }

  public String getMessageKey() {
    return this.messageKey;
  }

  public Object[] getMessageParams() {
    return this.messageParams;
  }
}
