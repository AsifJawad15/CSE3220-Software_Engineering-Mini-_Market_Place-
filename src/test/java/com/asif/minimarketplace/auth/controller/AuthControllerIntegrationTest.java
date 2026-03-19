package com.asif.minimarketplace.auth.controller;

import com.asif.minimarketplace.user.entity.RoleName;
import com.asif.minimarketplace.user.entity.User;
import com.asif.minimarketplace.user.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class AuthControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    @Test
    void loginPage_returns200() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    void registerBuyerPage_returns200() throws Exception {
        mockMvc.perform(get("/register/buyer"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register-buyer"));
    }

    @Test
    void registerBuyer_validData_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/register/buyer")
                        .with(csrf())
                        .param("fullName", "Integration Buyer")
                        .param("email", "intbuyer@test.com")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        User user = userRepository.findByEmail("intbuyer@test.com").orElseThrow();
        assertThat(user.getRole()).isEqualTo(RoleName.BUYER);
    }

    @Test
    void registerBuyer_duplicateEmail_showsError() throws Exception {
        // Use the admin email that DataInitializer already seeded
        mockMvc.perform(post("/register/buyer")
                        .with(csrf())
                        .param("fullName", "New User")
                        .param("email", "admin@market.com")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register-buyer"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    void registerSeller_validData_redirectsToLogin() throws Exception {
        mockMvc.perform(post("/register/seller")
                        .with(csrf())
                        .param("fullName", "Integration Seller")
                        .param("email", "intseller@test.com")
                        .param("password", "password123")
                        .param("confirmPassword", "password123")
                        .param("shopName", "Test Shop"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        User user = userRepository.findByEmail("intseller@test.com").orElseThrow();
        assertThat(user.getRole()).isEqualTo(RoleName.SELLER);
    }
}
