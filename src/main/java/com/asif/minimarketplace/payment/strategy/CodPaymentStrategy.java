package com.asif.minimarketplace.payment.strategy;

import com.asif.minimarketplace.payment.PaymentMethod;
import com.asif.minimarketplace.payment.PaymentResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Concrete strategy for Cash-on-Delivery.
 * No gateway call is needed — payment is collected on delivery.
 */
@Slf4j
@Component
public class CodPaymentStrategy implements PaymentStrategy {

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.COD;
    }

    @Override
    public PaymentResult pay(BigDecimal amount, String orderReference) {
        log.info("[COD Strategy] Order {} – ৳{} to be collected on delivery", orderReference, amount);
        return PaymentResult.builder()
                .success(true)
                .transactionId(null)
                .message("Cash on Delivery – payment will be collected upon receipt")
                .build();
    }
}
