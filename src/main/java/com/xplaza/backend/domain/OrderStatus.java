/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.domain;

import java.util.Set;

import lombok.Getter;

/**
 * Represents the possible states of an order in the system.
 * 
 * This enum enforces valid state transitions to maintain order integrity.
 * Invalid transitions (e.g., Delivered -> Pending) are prevented.
 * 
 * Order Flow: 1. PENDING - Order received, awaiting confirmation 2. CONFIRMED -
 * Shop has confirmed the order 3. PROCESSING - Order is being prepared 4.
 * SHIPPED - Order is out for delivery 5. DELIVERED - Order successfully
 * delivered
 * 
 * Special States: - CANCELLED - Order cancelled before delivery - RETURNED -
 * Order returned after delivery - REFUNDED - Payment refunded
 */
@Getter
public enum OrderStatus {

  PENDING(1L, "Pending", Set.of(2L, 6L)), // Can move to CONFIRMED or CANCELLED
  CONFIRMED(2L, "Confirmed", Set.of(3L, 6L)), // Can move to PROCESSING or CANCELLED
  PROCESSING(3L, "Processing", Set.of(4L, 6L)), // Can move to SHIPPED or CANCELLED
  SHIPPED(4L, "Shipped", Set.of(5L, 7L)), // Can move to DELIVERED or RETURNED
  DELIVERED(5L, "Delivered", Set.of(7L)), // Can move to RETURNED only
  CANCELLED(6L, "Cancelled", Set.of()), // Terminal state
  RETURNED(7L, "Returned", Set.of(8L)), // Can move to REFUNDED
  REFUNDED(8L, "Refunded", Set.of()); // Terminal state

  private final Long id;
  private final String displayName;
  private final Set<Long> allowedTransitions;

  OrderStatus(Long id, String displayName, Set<Long> allowedTransitions) {
    this.id = id;
    this.displayName = displayName;
    this.allowedTransitions = allowedTransitions;
  }

  /**
   * Check if transition to the target status is allowed.
   * 
   * @param targetStatusId the ID of the target status
   * @return true if the transition is allowed
   */
  public boolean canTransitionTo(Long targetStatusId) {
    return allowedTransitions.contains(targetStatusId);
  }

  /**
   * Find OrderStatus by ID.
   * 
   * @param id the status ID
   * @return the corresponding OrderStatus, or null if not found
   */
  public static OrderStatus fromId(Long id) {
    if (id == null) {
      return null;
    }
    for (OrderStatus status : values()) {
      if (status.id.equals(id)) {
        return status;
      }
    }
    return null;
  }

  /**
   * Check if this status requires inventory restoration when transitioning to
   * CANCELLED. Only orders that haven't been shipped should restore inventory.
   */
  public boolean shouldRestoreInventoryOnCancel() {
    return this == PENDING || this == CONFIRMED || this == PROCESSING;
  }

  /**
   * Check if this status is a terminal state (no further transitions allowed).
   */
  public boolean isTerminal() {
    return allowedTransitions.isEmpty();
  }
}
