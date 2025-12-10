/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */

package com.xplaza.backend.payment.controller;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.xplaza.backend.payment.domain.entity.PaymentTransaction;
import com.xplaza.backend.payment.domain.entity.Refund;
import com.xplaza.backend.payment.service.PaymentService;

/**
 * REST controller for payment operations.
 */
@RestController
@RequestMapping("/api/v1/payments")
@RequiredArgsConstructor
@Tag(name = "Payments", description = "Payment processing and refund management APIs")
public class PaymentController {

  private final PaymentService paymentService;

  // ==================== Transaction Operations ====================

  @Operation(summary = "Create payment authorization")
  @PostMapping("/authorize")
  public ResponseEntity<PaymentTransaction> authorize(@RequestBody @Valid CreateAuthorizationRequest request) {
    PaymentTransaction txn = paymentService.createAuthorization(
        request.orderId(),
        request.customerId(),
        request.amount(),
        request.currency(),
        request.methodType(),
        request.lastFourDigits(),
        request.cardBrand());
    return ResponseEntity.ok(txn);
  }

  @Operation(summary = "Create sale transaction (auth + capture)")
  @PostMapping("/sale")
  public ResponseEntity<PaymentTransaction> createSale(@RequestBody CreateSaleRequest request) {
    PaymentTransaction txn = paymentService.createSale(
        request.orderId(),
        request.customerId(),
        request.amount(),
        request.currency(),
        request.methodType());
    return ResponseEntity.ok(txn);
  }

  @Operation(summary = "Capture authorized payment")
  @PostMapping("/{authorizationId}/capture")
  public ResponseEntity<PaymentTransaction> capture(
      @PathVariable UUID authorizationId,
      @RequestParam(required = false) BigDecimal amount) {
    PaymentTransaction txn = paymentService.capture(authorizationId, amount);
    return ResponseEntity.ok(txn);
  }

  @Operation(summary = "Complete a pending transaction (for testing/webhooks)")
  @PostMapping("/{transactionId}/complete")
  public ResponseEntity<PaymentTransaction> complete(
      @PathVariable UUID transactionId,
      @RequestParam String gatewayTransactionId,
      @RequestParam String authorizationCode) {
    PaymentTransaction txn = paymentService.completeTransaction(transactionId, gatewayTransactionId, authorizationCode);
    return ResponseEntity.ok(txn);
  }

  @Operation(summary = "Fail a pending transaction")
  @PostMapping("/{transactionId}/fail")
  public ResponseEntity<PaymentTransaction> fail(
      @PathVariable UUID transactionId,
      @RequestParam String errorCode,
      @RequestParam String errorMessage) {
    PaymentTransaction txn = paymentService.failTransaction(transactionId, errorCode, errorMessage);
    return ResponseEntity.ok(txn);
  }

  @Operation(summary = "Get transactions for an order")
  @GetMapping("/orders/{orderId}")
  public ResponseEntity<List<PaymentTransaction>> getOrderTransactions(@PathVariable UUID orderId) {
    return ResponseEntity.ok(paymentService.getOrderTransactions(orderId));
  }

  @Operation(summary = "Get customer payment history")
  @GetMapping("/customers/{customerId}")
  public ResponseEntity<Page<PaymentTransaction>> getCustomerTransactions(
      @PathVariable Long customerId,
      Pageable pageable) {
    return ResponseEntity.ok(paymentService.getCustomerTransactions(customerId, pageable));
  }

  @Operation(summary = "Get total paid amount for order")
  @GetMapping("/orders/{orderId}/paid")
  public ResponseEntity<BigDecimal> getTotalPaid(@PathVariable UUID orderId) {
    return ResponseEntity.ok(paymentService.getTotalPaidAmount(orderId));
  }

  @Operation(summary = "Get total refunded amount for order")
  @GetMapping("/orders/{orderId}/refunded")
  public ResponseEntity<BigDecimal> getTotalRefunded(@PathVariable UUID orderId) {
    return ResponseEntity.ok(paymentService.getTotalRefundedAmount(orderId));
  }

  // ==================== Refund Operations ====================

  @Operation(summary = "Request a refund")
  @PostMapping("/refunds")
  public ResponseEntity<Refund> requestRefund(@RequestBody CreateRefundRequest request) {
    Refund refund = paymentService.createRefundRequest(
        request.orderId(),
        request.amount(),
        request.currency(),
        request.reason(),
        request.reasonDetail(),
        request.requestedBy(),
        request.requesterType());
    return ResponseEntity.ok(refund);
  }

  @Operation(summary = "Approve a refund")
  @PostMapping("/refunds/{refundId}/approve")
  public ResponseEntity<Refund> approveRefund(
      @PathVariable UUID refundId,
      @RequestParam Long adminId) {
    Refund refund = paymentService.approveRefund(refundId, adminId);
    return ResponseEntity.ok(refund);
  }

  @Operation(summary = "Reject a refund")
  @PostMapping("/refunds/{refundId}/reject")
  public ResponseEntity<Refund> rejectRefund(
      @PathVariable UUID refundId,
      @RequestParam Long adminId,
      @RequestParam String reason) {
    Refund refund = paymentService.rejectRefund(refundId, adminId, reason);
    return ResponseEntity.ok(refund);
  }

  @Operation(summary = "Process approved refund")
  @PostMapping("/refunds/{refundId}/process")
  public ResponseEntity<Refund> processRefund(
      @PathVariable UUID refundId,
      @RequestParam String gatewayRefundId) {
    Refund refund = paymentService.processRefund(refundId, gatewayRefundId);
    return ResponseEntity.ok(refund);
  }

  @Operation(summary = "Get pending refunds")
  @GetMapping("/refunds/pending")
  public ResponseEntity<Page<Refund>> getPendingRefunds(Pageable pageable) {
    return ResponseEntity.ok(paymentService.getPendingRefunds(pageable));
  }

  @Operation(summary = "Get refunds for an order")
  @GetMapping("/refunds/orders/{orderId}")
  public ResponseEntity<List<Refund>> getOrderRefunds(@PathVariable UUID orderId) {
    return ResponseEntity.ok(paymentService.getOrderRefunds(orderId));
  }

  @Operation(summary = "Create Stripe Payment Intent")
  @PostMapping("/create-payment-intent")
  public ResponseEntity<String> createPaymentIntent(@RequestBody @Valid CreatePaymentIntentRequest request) {
    String clientSecret = paymentService.createPaymentIntent(
        request.amount(),
        request.currency(),
        request.description(),
        request.metadata());
    return ResponseEntity.ok(clientSecret);
  }

  // ==================== Request DTOs ====================

  public record CreatePaymentIntentRequest(
      @NotNull @Positive BigDecimal amount,
      @NotBlank @Size(min = 3, max = 3) String currency,
      String description,
      Map<String, String> metadata
  ) {
  }

  public record CreateAuthorizationRequest(
      @NotNull UUID orderId,
      @NotNull Long customerId,
      @NotNull @Positive BigDecimal amount,
      @NotBlank @Size(min = 3, max = 3) String currency,
      @NotNull PaymentTransaction.PaymentMethodType methodType,
      String lastFourDigits,
      String cardBrand
  ) {
  }

  public record CreateSaleRequest(
      UUID orderId,
      Long customerId,
      BigDecimal amount,
      String currency,
      PaymentTransaction.PaymentMethodType methodType
  ) {
  }

  public record CreateRefundRequest(
      UUID orderId,
      BigDecimal amount,
      String currency,
      Refund.RefundReason reason,
      String reasonDetail,
      Long requestedBy,
      Refund.RequesterType requesterType
  ) {
  }
}
