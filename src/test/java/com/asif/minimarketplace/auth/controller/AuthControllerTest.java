package com.asif.minimarketplace.auth.controller;

import com.asif.minimarketplace.auth.dto.RegisterBuyerRequest;
import com.asif.minimarketplace.auth.dto.RegisterSellerRequest;
import com.asif.minimarketplace.auth.service.AuthService;
import com.asif.minimarketplace.common.exception.ValidationException;
import com.asif.minimarketplace.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
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

/**
 * Integration tests for AuthController -- covers login page,
 * buyer/seller registration pages, valid registrations, duplicate emails,
 * and invalid form fields.
 */
@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    // ── login page loads ───────────────────────────────────────────────────
    @Test
    void loginPage_Loads() throws Exception {
        mockMvc.perform(get("/login"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/login"));
    }

    // ── buyer register page loads ──────────────────────────────────────────
    @Test
    void buyerRegisterPage_Loads() throws Exception {
        mockMvc.perform(get("/register/buyer"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register-buyer"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    // ── seller register page loads ─────────────────────────────────────────
    @Test
    void sellerRegisterPage_Loads() throws Exception {
        mockMvc.perform(get("/register/seller"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register-seller"))
                .andExpect(model().attributeExists("registerRequest"));
    }

    // ── valid buyer registration redirects to login ────────────────────────
    @Test
    void validBuyerRegistration_RedirectsToLogin() throws Exception {
        when(authService.registerBuyer(any(RegisterBuyerRequest.class)))
                .thenReturn(new User());

        mockMvc.perform(post("/register/buyer")
                        .param("fullName", "John Doe")
                        .param("email", "john@example.com")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(authService).registerBuyer(any(RegisterBuyerRequest.class));
    }

    // ── valid seller registration redirects to login ───────────────────────
    @Test
    void validSellerRegistration_RedirectsToLogin() throws Exception {
        when(authService.registerSeller(any(RegisterSellerRequest.class)))
                .thenReturn(new User());

        mockMvc.perform(post("/register/seller")
                        .param("fullName", "Jane Seller")
                        .param("email", "jane@example.com")
                        .param("password", "password123")
                        .param("confirmPassword", "password123")
                        .param("shopName", "Jane's Shop"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(authService).registerSeller(any(RegisterSellerRequest.class));
    }

    // ── duplicate buyer email shows error on form ──────────────────────────
    @Test
    void duplicateBuyerEmail_ShowsErrorOnForm() throws Exception {
        when(authService.registerBuyer(any(RegisterBuyerRequest.class)))
                .thenThrow(new ValidationException("Email already in use"));

        mockMvc.perform(post("/register/buyer")
                        .param("fullName", "John Doe")
                        .param("email", "existing@example.com")
                        .param("password", "password123")
                        .param("confirmPassword", "password123"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register-buyer"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    // ── duplicate seller email shows error on form ─────────────────────────
    @Test
    void duplicateSellerEmail_ShowsErrorOnForm() throws Exception {
        when(authService.registerSeller(any(RegisterSellerRequest.class)))
                .thenThrow(new ValidationException("Email already in use"));

        mockMvc.perform(post("/register/seller")
                        .param("fullName", "Jane Seller")
                        .param("email", "existing@example.com")
                        .param("password", "password123")
                        .param("confirmPassword", "password123")
                        .param("shopName", "Jane's Shop"))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register-seller"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    // ── invalid form fields return same page with validation errors (buyer) ─
    @Test
    void invalidBuyerForm_ReturnsSamePage() throws Exception {
        mockMvc.perform(post("/register/buyer")
                        .param("fullName", "")   // blank triggers @NotBlank
                        .param("email", "bad")    // invalid email
                        .param("password", "12")  // too short
                        .param("confirmPassword", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register-buyer"));

        verify(authService, never()).registerBuyer(any());
    }

    // ── invalid form fields return same page with validation errors (seller)
    @Test
    void invalidSellerForm_ReturnsSamePage() throws Exception {
        mockMvc.perform(post("/register/seller")
                        .param("fullName", "")
                        .param("email", "bad")
                        .param("password", "12")
                        .param("confirmPassword", "")
                        .param("shopName", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("auth/register-seller"));

        verify(authService, never()).registerSeller(any());
    }
}