/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.payment.service;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import com.stripe.param.PaymentIntentCreateParams;

@Service
public class StripePaymentGateway implements PaymentGateway {

  @Override
  public PaymentIntent createPaymentIntent(BigDecimal amount, String currency, String description,
      Map<String, String> metadata) throws StripeException {
    PaymentIntentCreateParams params = PaymentIntentCreateParams.builder()
        .setAmount(amount.multiply(new BigDecimal(100)).longValue()) // Amount in cents
        .setCurrency(currency)
        .setDescription(description)
        .putAllMetadata(metadata)
        .setAutomaticPaymentMethods(
            PaymentIntentCreateParams.AutomaticPaymentMethods.builder()
                .setEnabled(true)
                .build())
        .build();

    return PaymentIntent.create(params);
  }

  @Override
  public PaymentIntent confirmPaymentIntent(String paymentIntentId) throws StripeException {
    PaymentIntent paymentIntent = PaymentIntent.retrieve(paymentIntentId);
    Map<String, Object> params = new HashMap<>();
    // In a real flow, you might need payment_method_id here if not attached
    // previously
    return paymentIntent.confirm(params);
  }
}
