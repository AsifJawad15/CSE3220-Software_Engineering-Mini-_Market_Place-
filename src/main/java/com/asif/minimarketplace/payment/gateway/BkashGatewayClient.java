package com.asif.minimarketplace.payment.gateway;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Simulated bKash payment gateway client.
 * In a real system this would be provided by the bKash SDK.
 * Its API shape (sendMoney → BkashResponse) is intentionally different
 * from Nagad's, which is why we need the Adapter pattern to normalise them.
 */
@Slf4j
@Component
public class BkashGatewayClient {

    @Data
    @AllArgsConstructor
    public static class BkashResponse {
        private String paymentId;
        private String status;       // "SUCCESS" or "FAILED"
        private String errorMessage;
    }

    /**
     * bKash-proprietary operation: charge a customer wallet.
     */
    public BkashResponse sendMoney(String customerPhone, BigDecimal amount) {
        log.info("[bKash SDK] sendMoney called – phone={}, amount={}", customerPhone, amount);
        // Simulate gateway processing
        String paymentId = "BK-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        return new BkashResponse(paymentId, "SUCCESS", null);
    }
}
