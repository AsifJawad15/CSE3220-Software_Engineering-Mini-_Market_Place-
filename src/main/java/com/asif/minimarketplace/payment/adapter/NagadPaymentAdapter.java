package com.asif.minimarketplace.payment.adapter;

import com.asif.minimarketplace.payment.PaymentResult;
import com.asif.minimarketplace.payment.gateway.NagadGatewayClient;
import com.asif.minimarketplace.payment.gateway.NagadGatewayClient.NagadResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Adapter that wraps the Nagad gateway client and translates
 * its proprietary NagadResult into the common PaymentResult format.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class NagadPaymentAdapter implements PaymentGatewayPort {

    private final NagadGatewayClient nagadClient;

    @Override
    public PaymentResult processPayment(BigDecimal amount, String reference) {
        log.info("[NagadAdapter] Adapting call for reference={}", reference);
        // Translate to Nagad-specific API shape
        NagadResult result = nagadClient.executePayment("MINI_MARKET", amount, reference);

        // Translate Nagad-specific response → common PaymentResult
        return PaymentResult.builder()
                .success(result.isAccepted())
                .transactionId(result.getIssuerPaymentRef())
                .message(result.getReason() != null
                        ? result.getReason()
                        : "Nagad payment processed")
                .build();
    }
}
