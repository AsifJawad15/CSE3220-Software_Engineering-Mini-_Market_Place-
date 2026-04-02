package com.asif.minimarketplace.auth.controller;
import com.asif.minimarketplace.auth.dto.RegisterBuyerRequest;
import com.asif.minimarketplace.auth.dto.RegisterSellerRequest;
import com.asif.minimarketplace.auth.service.AuthService;
import com.asif.minimarketplace.common.exception.ValidationException;
import com.asif.minimarketplace.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    @DisplayName("Login page loads successfully")
    void loginPage_Loads() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    @Test
    @DisplayName("Buyer register page loads successfully")
    void showBuyerRegisterForm_Loads() throws Exception {
        mockMvc.perform(get("/register/buyer"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register-buyer"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    @DisplayName("Seller register page loads successfully")
    void showSellerRegisterForm_Loads() throws Exception {
        mockMvc.perform(get("/register/seller"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register-seller"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    @Test
    @DisplayName("Valid buyer registration redirects to login")
    void registerBuyer_ValidRequest_RedirectsToLogin() throws Exception {
        when(authService.registerBuyer(any(RegisterBuyerRequest.class))).thenReturn(new User());

        mockMvc.perform(post("/register/buyer")
                .param("fullName", "John Doe")
                .param("email", "john@example.com")
                .param("password", "password123")
                .param("confirmPassword", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("Valid seller registration redirects to login")
    void registerSeller_ValidRequest_RedirectsToLogin() throws Exception {
        when(authService.registerSeller(any(RegisterSellerRequest.class))).thenReturn(new User());

        mockMvc.perform(post("/register/seller")
                .param("fullName", "Jane Doe")
                .param("email", "jane@example.com")
                .param("password", "password123")
                .param("confirmPassword", "password123")
                .param("shopName", "Jane Store"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    @Test
    @DisplayName("Duplicate buyer email shows error on form")
    void registerBuyer_DuplicateEmail_ShowsError() throws Exception {
        when(authService.registerBuyer(any(RegisterBuyerRequest.class)))
                .thenThrow(new ValidationException("email", "An account with this email already exists"));

        mockMvc.perform(post("/register/buyer")
                .param("fullName", "John Doe")
                .param("email", "dup@example.com")
                .param("password", "password123")
                .param("confirmPassword", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register-buyer"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("Duplicate seller email shows error on form")
    void registerSeller_DuplicateEmail_ShowsError() throws Exception {
        when(authService.registerSeller(any(RegisterSellerRequest.class)))
                .thenThrow(new ValidationException("email", "An account with this email already exists"));

        mockMvc.perform(post("/register/seller")
                .param("fullName", "Jane Doe")
                .param("email", "dup@example.com")
                .param("password", "password123")
                .param("confirmPassword", "password123")
                .param("shopName", "Jane Store"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register-seller"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    @Test
    @DisplayName("Invalid buyer form fields return same page with validation errors")
    void registerBuyer_InvalidFields_ReturnsForm() throws Exception {
        mockMvc.perform(post("/register/buyer")
                .param("email", "invalid-email")) // Missing required fields
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register-buyer"))
                .andExpect(model().hasErrors());
    }
}
