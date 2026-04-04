package com.asif.minimarketplace.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class DatabaseMigration implements CommandLineRunner {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public void run(String... args) {
        fixPaymentMethodColumn();
    }

    private void fixPaymentMethodColumn() {
        try {
            // Set default value for any NULL payment_method rows
            int updated = jdbcTemplate.update(
                    "UPDATE orders SET payment_method = 'COD' WHERE payment_method IS NULL");
            if (updated > 0) {
                log.info("Fixed {} orders with NULL payment_method → COD", updated);
            }
        } catch (Exception e) {
            log.warn("payment_method migration skipped: {}", e.getMessage());
        }
    }
}
