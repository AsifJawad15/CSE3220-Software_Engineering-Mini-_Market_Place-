package com.asif.minimarketplace.payment.strategy;

import com.asif.minimarketplace.payment.PaymentMethod;
import com.asif.minimarketplace.payment.PaymentResult;

import java.math.BigDecimal;

/**
 * Strategy interface for payment processing.
 * Each concrete strategy encapsulates its own payment logic,
 * eliminating if/else or switch branching in the calling service.
 */
public interface PaymentStrategy {

    PaymentMethod getPaymentMethod();

    PaymentResult pay(BigDecimal amount, String orderReference);
}
