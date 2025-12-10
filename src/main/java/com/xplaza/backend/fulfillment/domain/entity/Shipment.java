/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.fulfillment.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import jakarta.persistence.*;

import lombok.*;

/**
 * Represents a shipment for order fulfillment.
 *
 * A single order may have multiple shipments (split shipment).
 */
@Entity
@Table(name = "shipments", indexes = {
    @Index(name = "idx_shipments_order", columnList = "order_id"),
    @Index(name = "idx_shipments_tracking", columnList = "tracking_number"),
    @Index(name = "idx_shipments_status", columnList = "status")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Shipment {

  @Id
  @Column(name = "shipment_id")
  @Builder.Default
  private UUID shipmentId = UUID.randomUUID();

  @Column(name = "order_id", nullable = false)
  private UUID orderId;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "carrier_id")
  private Carrier carrier;

  @Column(name = "warehouse_id")
  private Long warehouseId;

  // Tracking
  @Column(name = "tracking_number", length = 100)
  private String trackingNumber;

  @Column(name = "tracking_url", length = 500)
  private String trackingUrl;

  // Status
  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 30)
  @Builder.Default
  private ShipmentStatus status = ShipmentStatus.PENDING;

  // Shipping method
  @Enumerated(EnumType.STRING)
  @Column(name = "shipping_method", nullable = false, length = 30)
  @Builder.Default
  private ShippingMethod shippingMethod = ShippingMethod.STANDARD;

  // Costs
  @Column(name = "shipping_cost", precision = 15, scale = 2)
  private BigDecimal shippingCost;

  @Column(name = "insurance_cost", precision = 15, scale = 2)
  @Builder.Default
  private BigDecimal insuranceCost = BigDecimal.ZERO;

  @Column(name = "currency", length = 3)
  @Builder.Default
  private String currency = "EUR";

  // Package dimensions
  @Column(name = "weight", precision = 10, scale = 3)
  private BigDecimal weight;

  @Enumerated(EnumType.STRING)
  @Column(name = "weight_unit", length = 5)
  @Builder.Default
  private WeightUnit weightUnit = WeightUnit.KG;

  @Column(name = "length", precision = 10, scale = 2)
  private BigDecimal length;

  @Column(name = "width", precision = 10, scale = 2)
  private BigDecimal width;

  @Column(name = "height", precision = 10, scale = 2)
  private BigDecimal height;

  @Enumerated(EnumType.STRING)
  @Column(name = "dimension_unit", length = 5)
  @Builder.Default
  private DimensionUnit dimensionUnit = DimensionUnit.CM;

  // Shipping address (denormalized for historical accuracy)
  @Column(name = "recipient_name", nullable = false, length = 255)
  private String recipientName;

  @Column(name = "recipient_phone", length = 50)
  private String recipientPhone;

  @Column(name = "address_line1", nullable = false, length = 255)
  private String addressLine1;

  @Column(name = "address_line2", length = 255)
  private String addressLine2;

  @Column(name = "city", nullable = false, length = 100)
  private String city;

  @Column(name = "state", length = 100)
  private String state;

  @Column(name = "postal_code", nullable = false, length = 20)
  private String postalCode;

  @Column(name = "country_code", nullable = false, length = 2)
  private String countryCode;

  // Delivery instructions
  @Column(name = "delivery_instructions", columnDefinition = "TEXT")
  private String deliveryInstructions;

  @Column(name = "signature_required")
  @Builder.Default
  private Boolean signatureRequired = false;

  @Column(name = "insured")
  @Builder.Default
  private Boolean insured = false;

  // Dates
  @Column(name = "estimated_delivery_date")
  private Instant estimatedDeliveryDate;

  @Column(name = "shipped_at")
  private Instant shippedAt;

  @Column(name = "delivered_at")
  private Instant deliveredAt;

  @Column(name = "created_at")
  @Builder.Default
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at")
  @Builder.Default
  private Instant updatedAt = Instant.now();

  // Label
  @Column(name = "label_url", length = 500)
  private String labelUrl;

  @Column(name = "label_format", length = 20)
  private String labelFormat;

  @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @Builder.Default
  private List<ShipmentItem> items = new ArrayList<>();

  @OneToMany(mappedBy = "shipment", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
  @OrderBy("eventTime DESC")
  @Builder.Default
  private List<ShipmentTrackingEvent> trackingEvents = new ArrayList<>();

  public enum ShipmentStatus {
    /** Shipment created */
    PENDING,
    /** Label created */
    LABEL_CREATED,
    /** Picked by warehouse */
    PICKED,
    /** Packed and ready */
    PACKED,
    /** Handed to carrier */
    SHIPPED,
    /** In transit */
    IN_TRANSIT,
    /** Out for delivery */
    OUT_FOR_DELIVERY,
    /** Delivered */
    DELIVERED,
    /** Delivery attempted */
    DELIVERY_ATTEMPTED,
    /** Returned to sender */
    RETURNED,
    /** Lost in transit */
    LOST,
    /** Cancelled */
    CANCELLED
  }

  public enum ShippingMethod {
    STANDARD,
    EXPRESS,
    OVERNIGHT,
    SAME_DAY,
    PICKUP,
    ECONOMY
  }

  public enum WeightUnit {
    KG,
    LB,
    G,
    OZ
  }

  public enum DimensionUnit {
    CM,
    IN,
    M
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = Instant.now();
  }

  public void addItem(ShipmentItem item) {
    items.add(item);
    item.setShipment(this);
  }

  public void addTrackingEvent(ShipmentTrackingEvent event) {
    trackingEvents.add(event);
    event.setShipment(this);
  }

  /**
   * Mark shipment as shipped.
   */
  public void ship(String trackingNumber, String trackingUrl) {
    this.trackingNumber = trackingNumber;
    this.trackingUrl = trackingUrl;
    this.status = ShipmentStatus.SHIPPED;
    this.shippedAt = Instant.now();
  }

  /**
   * Mark shipment as delivered.
   */
  public void deliver() {
    this.status = ShipmentStatus.DELIVERED;
    this.deliveredAt = Instant.now();
  }

  /**
   * Cancel shipment.
   */
  public void cancel() {
    this.status = ShipmentStatus.CANCELLED;
  }

  /**
   * Calculate total weight of all items.
   */
  public BigDecimal calculateTotalWeight() {
    if (items == null || items.isEmpty()) {
      return BigDecimal.ZERO;
    }
    return items.stream()
        .filter(i -> i.getWeight() != null)
        .map(i -> i.getWeight().multiply(BigDecimal.valueOf(i.getQuantity())))
        .reduce(BigDecimal.ZERO, BigDecimal::add);
  }
}
