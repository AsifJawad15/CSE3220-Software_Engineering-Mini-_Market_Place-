package com.asif.minimarketplace.payment.adapter;

import com.asif.minimarketplace.payment.PaymentResult;
import com.asif.minimarketplace.payment.gateway.BkashGatewayClient;
import com.asif.minimarketplace.payment.gateway.BkashGatewayClient.BkashResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Adapter that wraps the bKash gateway client and translates
 * its proprietary BkashResponse into the common PaymentResult format.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class BkashPaymentAdapter implements PaymentGatewayPort {

    private final BkashGatewayClient bkashClient;

    @Override
    public PaymentResult processPayment(BigDecimal amount, String reference) {
        log.info("[BkashAdapter] Adapting call for reference={}", reference);
        // Translate to bKash-specific API shape
        BkashResponse response = bkashClient.sendMoney("01XXXXXXXXX", amount);

        // Translate bKash-specific response → common PaymentResult
        return PaymentResult.builder()
                .success("SUCCESS".equals(response.getStatus()))
                .transactionId(response.getPaymentId())
                .message(response.getErrorMessage() != null
                        ? response.getErrorMessage()
                        : "bKash payment processed")
                .build();
    }
}
