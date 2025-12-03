/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.payment.service;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.xplaza.backend.payment.domain.entity.PaymentTransaction;
import com.xplaza.backend.payment.domain.entity.Refund;
import com.xplaza.backend.payment.domain.repository.PaymentTransactionRepository;
import com.xplaza.backend.payment.domain.repository.RefundRepository;

/**
 * Service for payment processing operations.
 */
@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class PaymentService {

  private final PaymentTransactionRepository transactionRepository;
  private final RefundRepository refundRepository;

  /**
   * Create a payment authorization.
   */
  public PaymentTransaction createAuthorization(Long orderId, Long customerId, BigDecimal amount,
      String currency, PaymentTransaction.PaymentMethodType methodType,
      String lastFourDigits, String cardBrand) {
    PaymentTransaction transaction = PaymentTransaction.builder()
        .orderId(orderId)
        .customerId(customerId)
        .type(PaymentTransaction.TransactionType.AUTHORIZATION)
        .amount(amount)
        .currency(currency)
        .amountInCents(amount.multiply(BigDecimal.valueOf(100)).longValue())
        .gateway("stripe") // Default gateway
        .paymentMethodType(methodType)
        .cardLast4(lastFourDigits)
        .cardBrand(cardBrand)
        .status(PaymentTransaction.TransactionStatus.PENDING)
        .build();

    transaction = transactionRepository.save(transaction);
    log.info("Created authorization for order {}: txn={}", orderId, transaction.getTransactionId());
    return transaction;
  }

  /**
   * Create a sale transaction (authorization + capture combined).
   */
  public PaymentTransaction createSale(Long orderId, Long customerId, BigDecimal amount,
      String currency, PaymentTransaction.PaymentMethodType methodType) {
    PaymentTransaction transaction = PaymentTransaction.builder()
        .orderId(orderId)
        .customerId(customerId)
        .type(PaymentTransaction.TransactionType.SALE)
        .amount(amount)
        .currency(currency)
        .amountInCents(amount.multiply(BigDecimal.valueOf(100)).longValue())
        .gateway("stripe")
        .paymentMethodType(methodType)
        .status(PaymentTransaction.TransactionStatus.PENDING)
        .build();

    transaction = transactionRepository.save(transaction);
    log.info("Created sale transaction for order {}: txn={}", orderId, transaction.getTransactionId());
    return transaction;
  }

  /**
   * Capture an authorized payment.
   */
  public PaymentTransaction capture(UUID authorizationId, BigDecimal amount) {
    PaymentTransaction auth = transactionRepository.findById(authorizationId)
        .orElseThrow(() -> new IllegalArgumentException("Authorization not found: " + authorizationId));

    if (auth.getType() != PaymentTransaction.TransactionType.AUTHORIZATION) {
      throw new IllegalStateException("Transaction is not an authorization");
    }

    PaymentTransaction capture = PaymentTransaction.builder()
        .orderId(auth.getOrderId())
        .customerId(auth.getCustomerId())
        .type(PaymentTransaction.TransactionType.CAPTURE)
        .amount(amount != null ? amount : auth.getAmount())
        .currency(auth.getCurrency())
        .amountInCents((amount != null ? amount : auth.getAmount()).multiply(BigDecimal.valueOf(100)).longValue())
        .gateway(auth.getGateway())
        .paymentMethodType(auth.getPaymentMethodType())
        .parentTransactionId(auth.getTransactionId())
        .status(PaymentTransaction.TransactionStatus.PENDING)
        .build();

    capture = transactionRepository.save(capture);
    log.info("Created capture for authorization {}: capture={}", authorizationId, capture.getTransactionId());
    return capture;
  }

  /**
   * Complete a transaction (simulates gateway response).
   */
  public PaymentTransaction completeTransaction(UUID transactionId, String gatewayTransactionId,
      String authorizationCode) {
    PaymentTransaction txn = transactionRepository.findById(transactionId)
        .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

    txn.markSuccess(gatewayTransactionId, authorizationCode);
    txn = transactionRepository.save(txn);

    log.info("Completed transaction: {}", transactionId);
    return txn;
  }

  /**
   * Fail a transaction.
   */
  public PaymentTransaction failTransaction(UUID transactionId, String errorCode, String errorMessage) {
    PaymentTransaction txn = transactionRepository.findById(transactionId)
        .orElseThrow(() -> new IllegalArgumentException("Transaction not found: " + transactionId));

    txn.markFailed(errorCode, errorMessage);
    txn = transactionRepository.save(txn);

    log.info("Failed transaction {}: {}", transactionId, errorMessage);
    return txn;
  }

  /**
   * Get transactions for an order.
   */
  @Transactional(readOnly = true)
  public List<PaymentTransaction> getOrderTransactions(Long orderId) {
    return transactionRepository.findByOrderId(orderId);
  }

  /**
   * Get completed sale for an order.
   */
  @Transactional(readOnly = true)
  public Optional<PaymentTransaction> getCompletedSale(Long orderId) {
    return transactionRepository.findCompletedSaleByOrderId(orderId);
  }

  /**
   * Get customer payment history.
   */
  @Transactional(readOnly = true)
  public Page<PaymentTransaction> getCustomerTransactions(Long customerId, Pageable pageable) {
    return transactionRepository.findByCustomerId(customerId, pageable);
  }

  /**
   * Get total paid amount for an order.
   */
  @Transactional(readOnly = true)
  public BigDecimal getTotalPaidAmount(Long orderId) {
    BigDecimal paid = transactionRepository.sumCompletedAmountByOrderId(orderId);
    return paid != null ? paid : BigDecimal.ZERO;
  }

  /**
   * Get total refunded amount for an order.
   */
  @Transactional(readOnly = true)
  public BigDecimal getTotalRefundedAmount(Long orderId) {
    BigDecimal refunded = transactionRepository.sumRefundedAmountByOrderId(orderId);
    return refunded != null ? refunded : BigDecimal.ZERO;
  }

  // Refund operations

  /**
   * Create a refund request.
   */
  public Refund createRefundRequest(Long orderId, BigDecimal amount, String currency,
      Refund.RefundReason reason, String reasonDetail, Long requestedBy,
      Refund.RequesterType requesterType) {
    Refund refund = Refund.builder()
        .orderId(orderId)
        .totalAmount(amount)
        .currency(currency)
        .reason(reason)
        .reasonDetail(reasonDetail)
        .requestedBy(requestedBy)
        .requestedByType(requesterType)
        .type(Refund.RefundType.PARTIAL) // Default to partial
        .build();

    refund = refundRepository.save(refund);
    log.info("Created refund request for order {}: refund={}", orderId, refund.getRefundId());
    return refund;
  }

  /**
   * Approve a refund request.
   */
  public Refund approveRefund(UUID refundId, Long adminId) {
    Refund refund = refundRepository.findById(refundId)
        .orElseThrow(() -> new IllegalArgumentException("Refund not found: " + refundId));

    refund.approve(adminId);
    refund = refundRepository.save(refund);

    log.info("Approved refund: {}", refundId);
    return refund;
  }

  /**
   * Reject a refund request.
   */
  public Refund rejectRefund(UUID refundId, Long adminId, String reason) {
    Refund refund = refundRepository.findById(refundId)
        .orElseThrow(() -> new IllegalArgumentException("Refund not found: " + refundId));

    refund.reject(adminId, reason);
    refund = refundRepository.save(refund);

    log.info("Rejected refund {}: {}", refundId, reason);
    return refund;
  }

  /**
   * Process approved refund.
   */
  public Refund processRefund(UUID refundId, String gatewayRefundId) {
    Refund refund = refundRepository.findById(refundId)
        .orElseThrow(() -> new IllegalArgumentException("Refund not found: " + refundId));

    if (refund.getStatus() != Refund.RefundStatus.APPROVED) {
      throw new IllegalStateException("Refund must be approved before processing");
    }

    refund.startProcessing();
    refundRepository.save(refund);

    // Simulate processing completion
    refund.complete(gatewayRefundId);
    refund = refundRepository.save(refund);

    log.info("Processed refund: {}", refundId);
    return refund;
  }

  /**
   * Get pending refunds.
   */
  @Transactional(readOnly = true)
  public Page<Refund> getPendingRefunds(Pageable pageable) {
    return refundRepository.findPendingRefunds(pageable);
  }

  /**
   * Get refunds for an order.
   */
  @Transactional(readOnly = true)
  public List<Refund> getOrderRefunds(Long orderId) {
    return refundRepository.findByOrderId(orderId);
  }

  /**
   * Clean up stale pending transactions.
   */
  public int cleanupStalePendingTransactions() {
    Instant cutoff = Instant.now().minus(24, ChronoUnit.HOURS);
    List<PaymentTransaction> stale = transactionRepository.findStalePendingTransactions(cutoff);

    for (PaymentTransaction txn : stale) {
      txn.markFailed("TIMEOUT", "Transaction timed out");
      transactionRepository.save(txn);
    }

    log.info("Cleaned up {} stale pending transactions", stale.size());
    return stale.size();
  }
}
