package com.asif.minimarketplace.config;

import com.asif.minimarketplace.product.service.CategoryService;
import com.asif.minimarketplace.user.entity.User;
import com.asif.minimarketplace.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
@Slf4j

@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
    private final CategoryService categoryService;

    }
    private void seedAdmin() {
        String adminEmail = "admin@market.com";
        seedCategories();
                    .fullName("System Admin")

                    .email(adminEmail)
                    .password(passwordEncoder.encode("12345678"))
                    .enabled(true)
            userRepository.save(admin);
            log.info("Admin user seeded: {}", adminEmail);
        } else {
            log.info("Admin user already exists, skipping seed.");
        }
    }
}