/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.fulfillment.controller;

import java.util.List;
import java.util.UUID;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.fulfillment.domain.entity.Carrier;
import com.xplaza.backend.fulfillment.domain.entity.Return;
import com.xplaza.backend.fulfillment.domain.entity.Shipment;
import com.xplaza.backend.fulfillment.service.FulfillmentService;

/**
 * REST controller for fulfillment operations.
 */
@RestController
@RequestMapping("/api/v1/fulfillment")
@RequiredArgsConstructor
@Tag(name = "Fulfillment", description = "Shipment and return management APIs")
public class FulfillmentController {

  private final FulfillmentService fulfillmentService;

  // ==================== Shipment Operations ====================

  @Operation(summary = "Create shipment for an order")
  @PostMapping("/shipments")
  public ResponseEntity<Shipment> createShipment(@RequestBody CreateShipmentRequest request) {
    Shipment shipment = fulfillmentService.createShipment(
        request.orderId(),
        request.warehouseId(),
        request.carrierId(),
        request.shippingMethod(),
        request.recipientName(),
        request.recipientPhone(),
        request.addressLine1(),
        request.addressLine2(),
        request.city(),
        request.state(),
        request.postalCode(),
        request.countryCode());
    return ResponseEntity.ok(shipment);
  }

  @Operation(summary = "Add item to shipment")
  @PostMapping("/shipments/{shipmentId}/items")
  public ResponseEntity<Void> addShipmentItem(
      @PathVariable UUID shipmentId,
      @RequestBody AddShipmentItemRequest request) {
    fulfillmentService.addShipmentItem(shipmentId, request.orderItemId(),
        request.productId(), request.variantId(), request.sku(),
        request.productName(), request.quantity());
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Mark shipment as shipped")
  @PostMapping("/shipments/{shipmentId}/ship")
  public ResponseEntity<Shipment> markShipped(
      @PathVariable UUID shipmentId,
      @RequestParam String trackingNumber,
      @RequestParam(required = false) String trackingUrl) {
    Shipment shipment = fulfillmentService.markShipped(shipmentId, trackingNumber, trackingUrl);
    return ResponseEntity.ok(shipment);
  }

  @Operation(summary = "Add tracking event")
  @PostMapping("/shipments/{shipmentId}/tracking")
  public ResponseEntity<Void> addTrackingEvent(
      @PathVariable UUID shipmentId,
      @RequestBody TrackingEventRequest request) {
    fulfillmentService.addTrackingEvent(shipmentId, request.status(),
        request.description(), request.location());
    return ResponseEntity.ok().build();
  }

  @Operation(summary = "Mark shipment as delivered")
  @PostMapping("/shipments/{shipmentId}/deliver")
  public ResponseEntity<Shipment> markDelivered(@PathVariable UUID shipmentId) {
    Shipment shipment = fulfillmentService.markDelivered(shipmentId);
    return ResponseEntity.ok(shipment);
  }

  @Operation(summary = "Get shipments for an order")
  @GetMapping("/shipments/orders/{orderId}")
  public ResponseEntity<List<Shipment>> getOrderShipments(@PathVariable UUID orderId) {
    return ResponseEntity.ok(fulfillmentService.getOrderShipments(orderId));
  }

  @Operation(summary = "Get shipment by tracking number")
  @GetMapping("/shipments/track/{trackingNumber}")
  public ResponseEntity<Shipment> getByTracking(@PathVariable String trackingNumber) {
    return fulfillmentService.getShipmentByTracking(trackingNumber)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Get shipment with full details")
  @GetMapping("/shipments/{shipmentId}")
  public ResponseEntity<Shipment> getShipmentDetails(@PathVariable UUID shipmentId) {
    return fulfillmentService.getShipmentWithDetails(shipmentId)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Get pending shipments for warehouse")
  @GetMapping("/shipments/pending/warehouses/{warehouseId}")
  public ResponseEntity<List<Shipment>> getPendingShipments(@PathVariable Long warehouseId) {
    return ResponseEntity.ok(fulfillmentService.getPendingShipments(warehouseId));
  }

  // ==================== Return Operations ====================

  @Operation(summary = "Create return request")
  @PostMapping("/returns")
  public ResponseEntity<Return> createReturnRequest(@RequestBody CreateReturnRequest request) {
    Return returnRequest = fulfillmentService.createReturnRequest(
        request.orderId(),
        request.customerId(),
        request.reason(),
        request.reasonDetail(),
        request.type());
    return ResponseEntity.ok(returnRequest);
  }

  @Operation(summary = "Approve return request")
  @PostMapping("/returns/{returnId}/approve")
  public ResponseEntity<Return> approveReturn(
      @PathVariable UUID returnId,
      @RequestBody ApproveReturnRequest request) {
    Return returnRequest = fulfillmentService.approveReturn(returnId, request.adminId(),
        request.returnAddressLine1(), request.returnCity(),
        request.returnPostalCode(), request.returnCountryCode());
    return ResponseEntity.ok(returnRequest);
  }

  @Operation(summary = "Reject return request")
  @PostMapping("/returns/{returnId}/reject")
  public ResponseEntity<Return> rejectReturn(
      @PathVariable UUID returnId,
      @RequestParam Long adminId,
      @RequestParam String reason) {
    Return returnRequest = fulfillmentService.rejectReturn(returnId, adminId, reason);
    return ResponseEntity.ok(returnRequest);
  }

  @Operation(summary = "Mark return as shipped by customer")
  @PostMapping("/returns/{returnId}/ship")
  public ResponseEntity<Return> markReturnShipped(
      @PathVariable UUID returnId,
      @RequestParam String trackingNumber) {
    Return returnRequest = fulfillmentService.markReturnShipped(returnId, trackingNumber);
    return ResponseEntity.ok(returnRequest);
  }

  @Operation(summary = "Mark return as received")
  @PostMapping("/returns/{returnId}/receive")
  public ResponseEntity<Return> markReturnReceived(@PathVariable UUID returnId) {
    Return returnRequest = fulfillmentService.markReturnReceived(returnId);
    return ResponseEntity.ok(returnRequest);
  }

  @Operation(summary = "Complete return with resolution")
  @PostMapping("/returns/{returnId}/complete")
  public ResponseEntity<Return> completeReturn(
      @PathVariable UUID returnId,
      @RequestParam Return.Resolution resolution) {
    Return returnRequest = fulfillmentService.completeReturn(returnId, resolution);
    return ResponseEntity.ok(returnRequest);
  }

  @Operation(summary = "Get return by RMA number")
  @GetMapping("/returns/rma/{rmaNumber}")
  public ResponseEntity<Return> getByRma(@PathVariable String rmaNumber) {
    return fulfillmentService.getReturnByRma(rmaNumber)
        .map(ResponseEntity::ok)
        .orElse(ResponseEntity.notFound().build());
  }

  @Operation(summary = "Get returns for an order")
  @GetMapping("/returns/orders/{orderId}")
  public ResponseEntity<List<Return>> getOrderReturns(@PathVariable UUID orderId) {
    return ResponseEntity.ok(fulfillmentService.getOrderReturns(orderId));
  }

  @Operation(summary = "Get customer returns")
  @GetMapping("/returns/customers/{customerId}")
  public ResponseEntity<Page<Return>> getCustomerReturns(
      @PathVariable Long customerId,
      Pageable pageable) {
    return ResponseEntity.ok(fulfillmentService.getCustomerReturns(customerId, pageable));
  }

  @Operation(summary = "Get pending return requests")
  @GetMapping("/returns/pending")
  public ResponseEntity<Page<Return>> getPendingReturns(Pageable pageable) {
    return ResponseEntity.ok(fulfillmentService.getPendingReturns(pageable));
  }

  // ==================== Carrier Operations ====================

  @Operation(summary = "Get active carriers")
  @GetMapping("/carriers")
  public ResponseEntity<List<Carrier>> getActiveCarriers() {
    return ResponseEntity.ok(fulfillmentService.getActiveCarriers());
  }

  @Operation(summary = "Get carriers for a country")
  @GetMapping("/carriers/countries/{countryCode}")
  public ResponseEntity<List<Carrier>> getCarriersForCountry(@PathVariable String countryCode) {
    return ResponseEntity.ok(fulfillmentService.getCarriersForCountry(countryCode));
  }

  // ==================== Request DTOs ====================

  public record CreateShipmentRequest(
      UUID orderId,
      Long warehouseId,
      Long carrierId,
      Shipment.ShippingMethod shippingMethod,
      String recipientName,
      String recipientPhone,
      String addressLine1,
      String addressLine2,
      String city,
      String state,
      String postalCode,
      String countryCode
  ) {
  }

  public record AddShipmentItemRequest(
      UUID orderItemId,
      Long productId,
      UUID variantId,
      String sku,
      String productName,
      int quantity
  ) {
  }

  public record TrackingEventRequest(
      Shipment.ShipmentStatus status,
      String description,
      String location
  ) {
  }

  public record CreateReturnRequest(
      UUID orderId,
      Long customerId,
      Return.ReturnReason reason,
      String reasonDetail,
      Return.ReturnType type
  ) {
  }

  public record ApproveReturnRequest(
      Long adminId,
      String returnAddressLine1,
      String returnCity,
      String returnPostalCode,
      String returnCountryCode
  ) {
  }
}
