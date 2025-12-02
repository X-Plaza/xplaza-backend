/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.fulfillment.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

import lombok.*;

/**
 * Individual item in a shipment.
 */
@Entity
@Table(name = "shipment_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShipmentItem {

  @Id
  @Column(name = "shipment_item_id")
  @Builder.Default
  private UUID shipmentItemId = UUID.randomUUID();

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "shipment_id", nullable = false)
  private Shipment shipment;

  @Column(name = "order_item_id", nullable = false)
  private Long orderItemId;

  @Column(name = "product_id", nullable = false)
  private Long productId;

  @Column(name = "variant_id")
  private UUID variantId;

  @Column(name = "sku", length = 100)
  private String sku;

  @Column(name = "product_name", nullable = false, length = 255)
  private String productName;

  @Column(name = "quantity", nullable = false)
  private Integer quantity;

  @Column(name = "weight", precision = 10, scale = 3)
  private BigDecimal weight;

  @Enumerated(EnumType.STRING)
  @Column(name = "weight_unit", length = 5)
  @Builder.Default
  private Shipment.WeightUnit weightUnit = Shipment.WeightUnit.KG;

  // Warehouse location for picking
  @Column(name = "bin_location", length = 50)
  private String binLocation;

  @Column(name = "serial_numbers", columnDefinition = "TEXT")
  private String serialNumbers;

  @Column(name = "created_at")
  @Builder.Default
  private Instant createdAt = Instant.now();
}
