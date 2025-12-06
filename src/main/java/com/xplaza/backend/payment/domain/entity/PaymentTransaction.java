/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.payment.domain.entity;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.*;

import lombok.*;

/**
 * Payment Transaction records all payment attempts and their outcomes.
 * 
 * This entity tracks the full lifecycle of payments: - Authorization (reserve
 * funds) - Capture (actual charge) - Refund (return funds) - Void (cancel
 * authorization)
 */
@Entity
@Table(name = "payment_transactions")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PaymentTransaction {

  @Id
  @Column(name = "transaction_id")
  @Builder.Default
  private UUID transactionId = UUID.randomUUID();

  @Column(name = "order_id")
  private UUID orderId;

  @Column(name = "customer_id")
  private Long customerId;

  @Enumerated(EnumType.STRING)
  @Column(name = "type", nullable = false, length = 20)
  private TransactionType type;

  @Enumerated(EnumType.STRING)
  @Column(name = "status", nullable = false, length = 20)
  @Builder.Default
  private TransactionStatus status = TransactionStatus.PENDING;

  // Amount
  @Column(name = "amount", nullable = false, precision = 15, scale = 2)
  private BigDecimal amount;

  @Column(name = "currency", nullable = false, length = 3)
  private String currency;

  /**
   * Amount in smallest currency unit (cents for USD). Used for gateway API calls.
   */
  @Column(name = "amount_in_cents", nullable = false)
  private Long amountInCents;

  // Gateway Information
  @Column(name = "gateway", nullable = false, length = 50)
  private String gateway;

  @Column(name = "gateway_transaction_id", length = 255)
  private String gatewayTransactionId;

  @Column(name = "authorization_code", length = 100)
  private String authorizationCode;

  @Column(name = "response_code", length = 50)
  private String responseCode;

  @Column(name = "response_message", columnDefinition = "TEXT")
  private String responseMessage;

  // Payment Method Details
  @Enumerated(EnumType.STRING)
  @Column(name = "payment_method_type", length = 20)
  private PaymentMethodType paymentMethodType;

  @Column(name = "card_brand", length = 20)
  private String cardBrand;

  @Column(name = "card_last4", length = 4)
  private String cardLast4;

  @Column(name = "card_expiry_month")
  private Integer cardExpiryMonth;

  @Column(name = "card_expiry_year")
  private Integer cardExpiryYear;

  @Column(name = "cardholder_name", length = 255)
  private String cardholderName;

  // Billing Address
  @Column(name = "billing_address_line1", length = 255)
  private String billingAddressLine1;

  @Column(name = "billing_address_line2", length = 255)
  private String billingAddressLine2;

  @Column(name = "billing_city", length = 100)
  private String billingCity;

  @Column(name = "billing_state", length = 100)
  private String billingState;

  @Column(name = "billing_postal_code", length = 20)
  private String billingPostalCode;

  @Column(name = "billing_country", length = 2)
  private String billingCountry;

  // Risk Assessment
  @Column(name = "risk_score")
  private Integer riskScore;

  @Enumerated(EnumType.STRING)
  @Column(name = "risk_level", length = 20)
  private RiskLevel riskLevel;

  @Column(name = "risk_factors", columnDefinition = "TEXT")
  private String riskFactors;

  // Metadata
  @Column(name = "ip_address", length = 45)
  private String ipAddress;

  @Column(name = "user_agent", columnDefinition = "TEXT")
  private String userAgent;

  @Column(name = "device_fingerprint", length = 255)
  private String deviceFingerprint;

  @Column(name = "metadata", columnDefinition = "TEXT")
  private String metadata;

  /**
   * Parent transaction for refunds (points to original payment).
   */
  @Column(name = "parent_transaction_id")
  private UUID parentTransactionId;

  // Timestamps
  @Column(name = "processed_at")
  private Instant processedAt;

  @Column(name = "created_at")
  @Builder.Default
  private Instant createdAt = Instant.now();

  @Column(name = "updated_at")
  @Builder.Default
  private Instant updatedAt = Instant.now();

  public enum TransactionType {
    /** Reserve funds on customer's payment method */
    AUTHORIZATION,
    /** Capture previously authorized funds */
    CAPTURE,
    /** Immediate charge (auth + capture) */
    SALE,
    /** Return funds to customer */
    REFUND,
    /** Cancel an authorization */
    VOID
  }

  public enum TransactionStatus {
    /** Transaction initiated */
    PENDING,
    /** Transaction completed successfully */
    SUCCESS,
    /** Transaction failed */
    FAILED,
    /** Transaction was cancelled */
    CANCELLED
  }

  public enum PaymentMethodType {
    CARD,
    PAYPAL,
    BANK_TRANSFER,
    WALLET,
    APPLE_PAY,
    GOOGLE_PAY
  }

  public enum RiskLevel {
    LOW,
    MEDIUM,
    HIGH
  }

  @PreUpdate
  protected void onUpdate() {
    this.updatedAt = Instant.now();
  }

  /**
   * Mark the transaction as successful.
   */
  public void markSuccess(String gatewayTransactionId, String authorizationCode) {
    this.status = TransactionStatus.SUCCESS;
    this.gatewayTransactionId = gatewayTransactionId;
    this.authorizationCode = authorizationCode;
    this.processedAt = Instant.now();
  }

  /**
   * Mark the transaction as failed.
   */
  public void markFailed(String responseCode, String responseMessage) {
    this.status = TransactionStatus.FAILED;
    this.responseCode = responseCode;
    this.responseMessage = responseMessage;
    this.processedAt = Instant.now();
  }

  /**
   * Get masked card number for display.
   */
  public String getMaskedCardNumber() {
    if (cardLast4 == null) {
      return null;
    }
    return "•••• •••• •••• " + cardLast4;
  }

  /**
   * Check if this is a refundable transaction.
   */
  public boolean isRefundable() {
    return status == TransactionStatus.SUCCESS &&
        (type == TransactionType.SALE || type == TransactionType.CAPTURE);
  }
}
