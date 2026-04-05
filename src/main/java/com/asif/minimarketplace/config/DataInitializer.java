package com.asif.minimarketplace.config;

import com.asif.minimarketplace.product.service.CategoryService;
import com.asif.minimarketplace.product.service.TagService;
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
    private final CategoryService categoryService;
    private final TagService tagService;

    @Override
    public void run(String... args) {
        seedAdmin();
        seedCategories();
        seedTags();
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

    private void seedCategories() {
        String[][] categories = {
            {"Electronics", "electronics"},
            {"Clothing", "clothing"},
            {"Books", "books"},
            {"Home & Garden", "home-garden"},
            {"Sports", "sports"},
            {"Toys & Games", "toys-games"},
            {"Health & Beauty", "health-beauty"},
            {"Automotive", "automotive"},
            {"Food & Beverages", "food-beverages"},
            {"Jewelry", "jewelry"}
        };
        for (String[] cat : categories) {
            if (!categoryService.existsByName(cat[0])) {
                categoryService.create(cat[0], cat[1]);
            }
        }
        log.info("Categories seeded.");
    }

    private void seedTags() {
        String[][] tags = {
            {"New Arrival", "new-arrival"},
            {"Best Seller", "best-seller"},
            {"On Sale", "on-sale"},
            {"Eco Friendly", "eco-friendly"},
            {"Premium", "premium"},
            {"Trending", "trending"},
            {"Limited Edition", "limited-edition"},
            {"Handmade", "handmade"},
            {"Organic", "organic"},
            {"Local", "local"}
        };
        for (String[] tag : tags) {
            if (!tagService.existsByName(tag[0])) {
                tagService.create(tag[0], tag[1]);
            }
        }
        log.info("Tags seeded.");
    }
}