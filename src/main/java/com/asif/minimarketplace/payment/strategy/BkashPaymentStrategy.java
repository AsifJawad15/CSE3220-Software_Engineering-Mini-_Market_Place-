package com.asif.minimarketplace.payment.strategy;

import com.asif.minimarketplace.payment.PaymentMethod;
import com.asif.minimarketplace.payment.PaymentResult;
import com.asif.minimarketplace.payment.adapter.BkashPaymentAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Concrete strategy for bKash mobile payment.
 * Delegates to the BkashPaymentAdapter (Adapter pattern) which in turn
 * wraps the proprietary BkashGatewayClient.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BkashPaymentStrategy implements PaymentStrategy {

    private final BkashPaymentAdapter bkashAdapter;

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.BKASH;
    }

    @Override
    public PaymentResult pay(BigDecimal amount, String orderReference) {
        log.info("[bKash Strategy] Processing ৳{} for order {}", amount, orderReference);
        return bkashAdapter.processPayment(amount, orderReference);
    }
}
