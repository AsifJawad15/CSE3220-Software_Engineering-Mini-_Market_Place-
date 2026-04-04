package com.asif.minimarketplace.payment.strategy;

import com.asif.minimarketplace.payment.PaymentMethod;
import com.asif.minimarketplace.payment.PaymentResult;
import com.asif.minimarketplace.payment.adapter.NagadPaymentAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Concrete strategy for Nagad mobile payment.
 * Delegates to the NagadPaymentAdapter (Adapter pattern) which in turn
 * wraps the proprietary NagadGatewayClient.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NagadPaymentStrategy implements PaymentStrategy {

    private final NagadPaymentAdapter nagadAdapter;

    @Override
    public PaymentMethod getPaymentMethod() {
        return PaymentMethod.NAGAD;
    }

    @Override
    public PaymentResult pay(BigDecimal amount, String orderReference) {
        log.info("[Nagad Strategy] Processing ৳{} for order {}", amount, orderReference);
        return nagadAdapter.processPayment(amount, orderReference);
    }
}
