/*
 * Copyright (c) 2025 Xplaza or Xplaza affiliate company. All rights reserved.
 * Author: Mahiuddin Al Kamal <mahiuddinalkamal>
 */
package com.xplaza.backend.payment.service;

import java.math.BigDecimal;
import java.util.Map;

import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;

public interface PaymentGateway {

  PaymentIntent createPaymentIntent(BigDecimal amount, String currency, String description,
      Map<String, String> metadata) throws StripeException;

  PaymentIntent confirmPaymentIntent(String paymentIntentId) throws StripeException;
}
