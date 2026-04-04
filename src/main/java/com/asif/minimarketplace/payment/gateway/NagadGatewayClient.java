package com.asif.minimarketplace.payment.gateway;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Simulated Nagad payment gateway client.
 * In a real system this would be provided by the Nagad SDK.
 * Its API shape (executePayment → NagadResult) is intentionally different
 * from bKash's, which is why we need the Adapter pattern to normalise them.
 */
@Slf4j
@Component
public class NagadGatewayClient {

    @Data
    @AllArgsConstructor
    public static class NagadResult {
        private String issuerPaymentRef;
        private boolean accepted;
        private String reason;
    }

    /**
     * Nagad-proprietary operation: execute a merchant payment.
     */
    public NagadResult executePayment(String merchantId, BigDecimal amount, String orderRef) {
        log.info("[Nagad SDK] executePayment called – merchant={}, amount={}, ref={}", merchantId, amount, orderRef);
        // Simulate gateway processing
        String ref = "NG-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new NagadResult(ref, true, null);
    }
}
