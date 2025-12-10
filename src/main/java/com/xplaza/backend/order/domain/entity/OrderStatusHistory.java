/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.order.domain.entity;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

import lombok.*;

/**
 * Tracks the history of status changes for an order.
 */
@Entity
@Table(name = "customer_order_status_history", indexes = {
    @Index(name = "idx_cust_order_status_history_order", columnList = "order_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@ToString(exclude = "order")
public class OrderStatusHistory {

  @Id
  @Column(name = "history_id")
  @Builder.Default
  private UUID historyId = UUID.randomUUID();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "order_id", nullable = false)
  @com.fasterxml.jackson.annotation.JsonIgnore
  private CustomerOrder order;

  @Enumerated(EnumType.STRING)
  @Column(name = "previous_status", length = 30)
  private CustomerOrder.OrderStatus previousStatus;

  @Enumerated(EnumType.STRING)
  @Column(name = "new_status", nullable = false, length = 30)
  private CustomerOrder.OrderStatus newStatus;

  @Column(name = "reason", length = 500)
  private String reason;

  @Column(name = "changed_by", length = 100)
  private String changedBy;

  @Column(name = "changed_at", nullable = false)
  @Builder.Default
  private Instant changedAt = Instant.now();

  public UUID getOrderId() {
    return order != null ? order.getOrderId() : null;
  }
}
