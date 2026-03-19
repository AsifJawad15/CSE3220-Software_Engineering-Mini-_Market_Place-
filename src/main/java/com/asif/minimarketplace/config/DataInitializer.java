package com.asif.minimarketplace.config;
import com.asif.minimarketplace.user.entity.RoleName;
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
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    @Override
    public void run(String... args) {
        seedAdmin();
    }
    private void seedAdmin() {
        String adminEmail = "admin@market.com";
        if (!userRepository.existsByEmail(adminEmail)) {
            User admin = User.builder()
                    .fullName("System Admin")
                    .email(adminEmail)
                    .password(passwordEncoder.encode("12345678"))
                    .role(RoleName.ADMIN)
                    .enabled(true)
                    .build();
            userRepository.save(admin);
            log.info("Admin user seeded: {}", adminEmail);
        } else {
            log.info("Admin user already exists, skipping seed.");
        }
    }
}