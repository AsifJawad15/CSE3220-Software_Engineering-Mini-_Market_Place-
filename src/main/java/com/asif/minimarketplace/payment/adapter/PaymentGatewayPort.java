package com.asif.minimarketplace.payment.adapter;

import com.asif.minimarketplace.payment.PaymentResult;

import java.math.BigDecimal;

/**
 * Adapter target interface.
 * Defines the uniform contract that all payment gateway adapters must implement,
 * regardless of the underlying gateway SDK's proprietary API shape.
 */
public interface PaymentGatewayPort {
    PaymentResult processPayment(BigDecimal amount, String reference);
}
