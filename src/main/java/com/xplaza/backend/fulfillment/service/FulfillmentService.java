/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.fulfillment.service;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.fulfillment.domain.entity.Carrier;
import com.xplaza.backend.fulfillment.domain.entity.Return;
import com.xplaza.backend.fulfillment.domain.entity.Shipment;
import com.xplaza.backend.fulfillment.domain.entity.ShipmentItem;
import com.xplaza.backend.fulfillment.domain.entity.ShipmentTrackingEvent;
import com.xplaza.backend.fulfillment.domain.repository.CarrierRepository;
import com.xplaza.backend.fulfillment.domain.repository.ReturnRepository;
import com.xplaza.backend.fulfillment.domain.repository.ShipmentRepository;
import com.xplaza.backend.notification.domain.entity.Notification;
import com.xplaza.backend.notification.service.NotificationService;
import com.xplaza.backend.order.domain.entity.CustomerOrder;
import com.xplaza.backend.order.service.CustomerOrderService;

/**
 * Service for shipment and return operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class FulfillmentService {

  private final ShipmentRepository shipmentRepository;
  private final ReturnRepository returnRepository;
  private final CarrierRepository carrierRepository;
  private final NotificationService notificationService;
  private final CustomerOrderService customerOrderService;

  // Shipment operations

  /**
   * Create a shipment for an order.
   */
  public Shipment createShipment(UUID orderId, Long warehouseId, Long carrierId,
      Shipment.ShippingMethod method, String recipientName, String recipientPhone,
      String addressLine1, String addressLine2, String city, String state,
      String postalCode, String countryCode) {
    Carrier carrier = carrierRepository.findById(carrierId)
        .orElseThrow(() -> new IllegalArgumentException("Carrier not found: " + carrierId));

    Shipment shipment = Shipment.builder()
        .orderId(orderId)
        .warehouseId(warehouseId)
        .carrier(carrier)
        .shippingMethod(method)
        .recipientName(recipientName)
        .recipientPhone(recipientPhone)
        .addressLine1(addressLine1)
        .addressLine2(addressLine2)
        .city(city)
        .state(state)
        .postalCode(postalCode)
        .countryCode(countryCode)
        .build();

    shipment = shipmentRepository.save(shipment);
    log.info("Created shipment for order {}: shipmentId={}", orderId, shipment.getShipmentId());
    return shipment;
  }

  /**
   * Add item to shipment.
   */
  public void addShipmentItem(UUID shipmentId, UUID orderItemId, Long productId,
      UUID variantId, String sku, String productName, int quantity) {
    Shipment shipment = shipmentRepository.findById(shipmentId)
        .orElseThrow(() -> new IllegalArgumentException("Shipment not found: " + shipmentId));

    ShipmentItem item = ShipmentItem.builder()
        .shipment(shipment)
        .orderItemId(orderItemId)
        .productId(productId)
        .variantId(variantId)
        .sku(sku)
        .productName(productName)
        .quantity(quantity)
        .build();

    shipment.addItem(item);
    shipmentRepository.save(shipment);
  }

  /**
   * Mark shipment as shipped.
   */
  public Shipment markShipped(UUID shipmentId, String trackingNumber, String trackingUrl) {
    Shipment shipment = shipmentRepository.findById(shipmentId)
        .orElseThrow(() -> new IllegalArgumentException("Shipment not found: " + shipmentId));

    shipment.ship(trackingNumber, trackingUrl);

    // Add tracking event
    ShipmentTrackingEvent event = ShipmentTrackingEvent.builder()
        .shipment(shipment)
        .status(Shipment.ShipmentStatus.SHIPPED)
        .description("Package shipped")
        .eventTime(Instant.now())
        .build();
    shipment.addTrackingEvent(event);

    Shipment savedShipment = shipmentRepository.save(shipment);
    log.info("Marked shipment as shipped: {} tracking={}", shipmentId, trackingNumber);

    // Send notification
    try {
      CustomerOrder order = customerOrderService.getOrder(savedShipment.getOrderId())
          .orElseThrow(() -> new IllegalArgumentException("Order not found: " + savedShipment.getOrderId()));

      notificationService.createOrderNotification(
          order.getCustomerId(),
          Notification.NotificationType.ORDER_SHIPPED,
          "Order Shipped",
          "Your order " + order.getOrderNumber() + " has been shipped. Tracking: " + trackingNumber,
          order.getOrderId().toString());
    } catch (Exception e) {
      log.error("Failed to send shipment notification: {}", e.getMessage());
    }

    return savedShipment;
  }

  /**
   * Update shipment tracking.
   */
  public void addTrackingEvent(UUID shipmentId, Shipment.ShipmentStatus status,
      String description, String location) {
    Shipment shipment = shipmentRepository.findById(shipmentId)
        .orElseThrow(() -> new IllegalArgumentException("Shipment not found: " + shipmentId));

    shipment.setStatus(status);

    ShipmentTrackingEvent event = ShipmentTrackingEvent.builder()
        .shipment(shipment)
        .status(status)
        .description(description)
        .location(location)
        .eventTime(Instant.now())
        .build();
    shipment.addTrackingEvent(event);

    shipmentRepository.save(shipment);
    log.info("Added tracking event to shipment {}: {}", shipmentId, status);
  }

  /**
   * Mark shipment as delivered.
   */
  public Shipment markDelivered(UUID shipmentId) {
    Shipment shipment = shipmentRepository.findById(shipmentId)
        .orElseThrow(() -> new IllegalArgumentException("Shipment not found: " + shipmentId));

    shipment.deliver();

    ShipmentTrackingEvent event = ShipmentTrackingEvent.builder()
        .shipment(shipment)
        .status(Shipment.ShipmentStatus.DELIVERED)
        .description("Package delivered")
        .eventTime(Instant.now())
        .build();
    shipment.addTrackingEvent(event);

    Shipment savedShipment = shipmentRepository.save(shipment);
    log.info("Marked shipment as delivered: {}", shipmentId);

    // Send notification
    try {
      CustomerOrder order = customerOrderService.getOrder(savedShipment.getOrderId())
          .orElseThrow(() -> new IllegalArgumentException("Order not found: " + savedShipment.getOrderId()));

      notificationService.createOrderNotification(
          order.getCustomerId(),
          Notification.NotificationType.ORDER_DELIVERED,
          "Order Delivered",
          "Your order " + order.getOrderNumber() + " has been delivered.",
          order.getOrderId().toString());
    } catch (Exception e) {
      log.error("Failed to send delivery notification: {}", e.getMessage());
    }

    return savedShipment;
  }

  /**
   * Get shipments for an order.
   */
  @Transactional(readOnly = true)
  public List<Shipment> getOrderShipments(UUID orderId) {
    return shipmentRepository.findByOrderId(orderId);
  }

  /**
   * Get shipment by tracking number.
   */
  @Transactional(readOnly = true)
  public Optional<Shipment> getShipmentByTracking(String trackingNumber) {
    return shipmentRepository.findByTrackingNumber(trackingNumber);
  }

  /**
   * Get shipment with full details.
   */
  @Transactional(readOnly = true)
  public Optional<Shipment> getShipmentWithDetails(UUID shipmentId) {
    return shipmentRepository.findByIdWithDetails(shipmentId);
  }

  /**
   * Get pending shipments for a warehouse.
   */
  @Transactional(readOnly = true)
  public List<Shipment> getPendingShipments(Long warehouseId) {
    return shipmentRepository.findPendingShipmentsByWarehouse(warehouseId);
  }

  // Return operations

  /**
   * Create a return request.
   */
  public Return createReturnRequest(UUID orderId, Long customerId, Return.ReturnReason reason,
      String reasonDetail, Return.ReturnType type) {
    Return returnRequest = Return.builder()
        .orderId(orderId)
        .customerId(customerId)
        .reason(reason)
        .reasonDetail(reasonDetail)
        .type(type)
        .build();

    returnRequest = returnRepository.save(returnRequest);
    log.info("Created return request for order {}: returnId={}", orderId, returnRequest.getReturnId());
    return returnRequest;
  }

  /**
   * Approve a return request.
   */
  public Return approveReturn(UUID returnId, Long adminId, String returnAddressLine1,
      String returnCity, String returnPostalCode, String returnCountryCode) {
    Return returnRequest = returnRepository.findById(returnId)
        .orElseThrow(() -> new IllegalArgumentException("Return not found: " + returnId));

    returnRequest.approve(adminId);
    returnRequest.setReturnAddressLine1(returnAddressLine1);
    returnRequest.setReturnCity(returnCity);
    returnRequest.setReturnPostalCode(returnPostalCode);
    returnRequest.setReturnCountryCode(returnCountryCode);

    returnRequest = returnRepository.save(returnRequest);
    log.info("Approved return: {}", returnId);
    return returnRequest;
  }

  /**
   * Reject a return request.
   */
  public Return rejectReturn(UUID returnId, Long adminId, String reason) {
    Return returnRequest = returnRepository.findById(returnId)
        .orElseThrow(() -> new IllegalArgumentException("Return not found: " + returnId));

    returnRequest.reject(adminId, reason);
    returnRequest = returnRepository.save(returnRequest);

    log.info("Rejected return {}: {}", returnId, reason);
    return returnRequest;
  }

  /**
   * Mark return as shipped by customer.
   */
  public Return markReturnShipped(UUID returnId, String trackingNumber) {
    Return returnRequest = returnRepository.findById(returnId)
        .orElseThrow(() -> new IllegalArgumentException("Return not found: " + returnId));

    returnRequest.markShipped(trackingNumber);
    returnRequest = returnRepository.save(returnRequest);

    log.info("Marked return as shipped: {} tracking={}", returnId, trackingNumber);
    return returnRequest;
  }

  /**
   * Mark return as received at warehouse.
   */
  public Return markReturnReceived(UUID returnId) {
    Return returnRequest = returnRepository.findById(returnId)
        .orElseThrow(() -> new IllegalArgumentException("Return not found: " + returnId));

    returnRequest.markReceived();
    returnRequest = returnRepository.save(returnRequest);

    log.info("Marked return as received: {}", returnId);
    return returnRequest;
  }

  /**
   * Complete return with resolution.
   */
  public Return completeReturn(UUID returnId, Return.Resolution resolution) {
    Return returnRequest = returnRepository.findById(returnId)
        .orElseThrow(() -> new IllegalArgumentException("Return not found: " + returnId));

    returnRequest.complete(resolution);
    returnRequest = returnRepository.save(returnRequest);

    log.info("Completed return {}: resolution={}", returnId, resolution);
    return returnRequest;
  }

  /**
   * Get return by RMA number.
   */
  @Transactional(readOnly = true)
  public Optional<Return> getReturnByRma(String rmaNumber) {
    return returnRepository.findByRmaNumber(rmaNumber);
  }

  /**
   * Get returns for an order.
   */
  @Transactional(readOnly = true)
  public List<Return> getOrderReturns(UUID orderId) {
    return returnRepository.findByOrderId(orderId);
  }

  /**
   * Get customer returns.
   */
  @Transactional(readOnly = true)
  public Page<Return> getCustomerReturns(Long customerId, Pageable pageable) {
    return returnRepository.findByCustomerId(customerId, pageable);
  }

  /**
   * Get pending return requests.
   */
  @Transactional(readOnly = true)
  public Page<Return> getPendingReturns(Pageable pageable) {
    return returnRepository.findPendingReturns(pageable);
  }

  // Carrier operations

  /**
   * Get active carriers.
   */
  @Transactional(readOnly = true)
  public List<Carrier> getActiveCarriers() {
    return carrierRepository.findByIsActiveTrue();
  }

  /**
   * Get carriers for a country.
   */
  @Transactional(readOnly = true)
  public List<Carrier> getCarriersForCountry(String countryCode) {
    return carrierRepository.findActiveCarriersByCountry(countryCode);
  }
}
