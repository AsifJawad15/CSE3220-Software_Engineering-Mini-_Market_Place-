package com.asif.minimarketplace.payment.strategy;

import com.asif.minimarketplace.payment.PaymentMethod;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Factory that auto-discovers all PaymentStrategy beans via Spring DI
 * and maps them by their PaymentMethod enum value.
 *
 * This eliminates any if/else or switch statement in the calling service —
 * adding a new payment method only requires a new PaymentStrategy @Component.
 */
@Slf4j
@Component
public class PaymentStrategyFactory {

    private final Map<PaymentMethod, PaymentStrategy> strategies;

    public PaymentStrategyFactory(List<PaymentStrategy> strategyBeans) {
        strategies = new EnumMap<>(PaymentMethod.class);
        for (PaymentStrategy s : strategyBeans) {
            strategies.put(s.getPaymentMethod(), s);
            log.info("Registered payment strategy: {} → {}", s.getPaymentMethod(), s.getClass().getSimpleName());
        }
    }

    public PaymentStrategy getStrategy(PaymentMethod method) {
        PaymentStrategy strategy = strategies.get(method);
        if (strategy == null) {
            throw new IllegalArgumentException("No payment strategy registered for: " + method);
        }
        return strategy;
    }
}
