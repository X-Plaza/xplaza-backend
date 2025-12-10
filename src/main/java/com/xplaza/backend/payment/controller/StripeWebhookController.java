/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.payment.controller;

import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.stripe.exception.SignatureVerificationException;
import com.stripe.model.Event;
import com.stripe.model.PaymentIntent;
import com.stripe.net.Webhook;
import com.xplaza.backend.order.domain.entity.CustomerOrder;
import com.xplaza.backend.order.domain.repository.CustomerOrderRepository;

@RestController
@RequestMapping("/api/v1/webhooks/stripe")
@RequiredArgsConstructor
@Slf4j
public class StripeWebhookController {

  @Value("${stripe.webhook-secret}")
  private String webhookSecret;

  private final CustomerOrderRepository orderRepository;

  @PostMapping
  public ResponseEntity<String> handleStripeWebhook(
      @RequestBody String payload,
      @RequestHeader("Stripe-Signature") String sigHeader) {

    Event event;

    try {
      event = Webhook.constructEvent(payload, sigHeader, webhookSecret);
    } catch (SignatureVerificationException e) {
      log.error("Invalid Stripe signature", e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Invalid signature");
    } catch (Exception e) {
      log.error("Webhook error", e);
      return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Webhook error");
    }

    if ("payment_intent.succeeded".equals(event.getType())) {
      PaymentIntent paymentIntent = (PaymentIntent) event.getDataObjectDeserializer().getObject().orElse(null);
      if (paymentIntent != null) {
        handlePaymentSuccess(paymentIntent);
      }
    }

    return ResponseEntity.ok("Received");
  }

  private void handlePaymentSuccess(PaymentIntent paymentIntent) {
    String orderIdStr = paymentIntent.getMetadata().get("orderId");
    if (orderIdStr != null) {
      try {
        UUID orderId = UUID.fromString(orderIdStr);
        orderRepository.findById(orderId).ifPresent(order -> {
          if (order.getStatus() == CustomerOrder.OrderStatus.PENDING) {
            order.setStatus(CustomerOrder.OrderStatus.CONFIRMED);
            orderRepository.save(order);
            log.info("Order {} confirmed via Stripe webhook", orderId);

            // Record transaction if not exists
            // In a real scenario, you might want to link this to the transaction created
            // earlier
          }
        });
      } catch (Exception e) {
        log.error("Error processing order update for payment intent {}", paymentIntent.getId(), e);
      }
    } else {
      log.warn("PaymentIntent {} succeeded but has no orderId metadata", paymentIntent.getId());
    }
  }
}
