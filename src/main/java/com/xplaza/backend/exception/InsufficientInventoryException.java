/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.exception;

import lombok.Getter;

/**
 * Exception thrown when there is insufficient inventory to fulfill an order.
 * This is a business exception that should result in a 422 Unprocessable Entity
 * response.
 */
@Getter
public class InsufficientInventoryException extends BusinessException {

  private final Long productId;
  private final String productName;
  private final Long requestedQuantity;
  private final Integer availableQuantity;

  public InsufficientInventoryException(Long productId, String productName,
      Long requestedQuantity, Integer availableQuantity) {
    super("insufficient.inventory",
        String.format("Insufficient inventory for product '%s' (ID: %d). Requested: %d, Available: %d",
            productName, productId, requestedQuantity, availableQuantity));
    this.productId = productId;
    this.productName = productName;
    this.requestedQuantity = requestedQuantity;
    this.availableQuantity = availableQuantity;
  }

  public InsufficientInventoryException(Long productId, Long requestedQuantity, Integer availableQuantity) {
    this(productId, "Unknown", requestedQuantity, availableQuantity);
  }
}
