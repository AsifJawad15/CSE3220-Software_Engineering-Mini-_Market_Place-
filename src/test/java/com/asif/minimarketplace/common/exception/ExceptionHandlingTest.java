package com.asif.minimarketplace.common.exception;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Exception handling tests -- verifies GlobalExceptionHandler behaviour
 * for both web and API requests.
 *
 * Uses a dummy controller that throws each exception type.
 * Web requests return proper HTTP status codes + a view name.
 * API requests (URI starts with /api/) return proper HTTP status codes + JSON.
 *
 * Tests cover:
 * - NotFoundException returns 404 view (web) / 404 JSON (API)
 * - AccessDeniedException returns 403 view (web) / 403 JSON (API)
 * - InsufficientStockException returns 409 stock view (web) / 409 JSON (API)
 * - ValidationException returns 400 view (web) / 400 JSON (API)
 * - Generic Exception returns 500 view (web) / 500 JSON (API)
 */
public class ExceptionHandlingTest {

    private MockMvc mockMvc;

    /**
     * Dummy controller that throws specific exceptions for testing the
     * GlobalExceptionHandler. Each endpoint triggers a different exception.
     */
    @RestController
    static class ThrowingController {
        @GetMapping("/throw/not-found")
        public void throwNotFound() { throw new NotFoundException("Test entity", 99L); }

        @GetMapping("/api/throw/not-found")
        public void throwNotFoundApi() { throw new NotFoundException("Test entity", 99L); }

        @GetMapping("/throw/access-denied")
        public void throwAccessDenied() { throw new AccessDeniedException("forbidden"); }

        @GetMapping("/api/throw/access-denied")
        public void throwAccessDeniedApi() { throw new AccessDeniedException("forbidden"); }

        @GetMapping("/throw/stock")
        public void throwStock() { throw new InsufficientStockException("Not enough stock"); }

        @GetMapping("/api/throw/stock")
        public void throwStockApi() { throw new InsufficientStockException("Not enough stock"); }

        @GetMapping("/throw/validation")
        public void throwValidation() { throw new ValidationException("Invalid input"); }

        @GetMapping("/api/throw/validation")
        public void throwValidationApi() { throw new ValidationException("Invalid input"); }

        @GetMapping("/throw/generic")
        public void throwGeneric() { throw new RuntimeException("unexpected"); }

        @GetMapping("/api/throw/generic")
        public void throwGenericApi() { throw new RuntimeException("unexpected"); }
    }

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(new ThrowingController())
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ── NotFoundException returns 404 error page for web ───────────────────
    @Test
    void notFound_Web_Returns404View() throws Exception {
        mockMvc.perform(get("/throw/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("errorCode", 404));
    }

    // ── NotFoundException returns 404 JSON for API ─────────────────────────
    @Test
    void notFound_Api_Returns404Json() throws Exception {
        mockMvc.perform(get("/api/throw/not-found"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── AccessDeniedException returns 403 error page for web ───────────────
    @Test
    void accessDenied_Web_Returns403View() throws Exception {
        mockMvc.perform(get("/throw/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(view().name("error/403"))
                .andExpect(model().attribute("errorCode", 403));
    }

    // ── AccessDeniedException returns 403 JSON for API ─────────────────────
    @Test
    void accessDenied_Api_Returns403Json() throws Exception {
        mockMvc.perform(get("/api/throw/access-denied"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── InsufficientStockException returns stock error page for web ────────
    @Test
    void insufficientStock_Web_ReturnsStockView() throws Exception {
        mockMvc.perform(get("/throw/stock"))
                .andExpect(status().isConflict())
                .andExpect(view().name("error/stock"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    // ── InsufficientStockException returns 409 JSON for API ────────────────
    @Test
    void insufficientStock_Api_Returns409Json() throws Exception {
        mockMvc.perform(get("/api/throw/stock"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── ValidationException returns 400 error page for web ─────────────────
    @Test
    void validation_Web_Returns400View() throws Exception {
        mockMvc.perform(get("/throw/validation"))
                .andExpect(status().isBadRequest())
                .andExpect(view().name("error/400"))
                .andExpect(model().attribute("errorCode", 400));
    }

    // ── ValidationException returns 400 JSON for API ───────────────────────
    @Test
    void validation_Api_Returns400Json() throws Exception {
        mockMvc.perform(get("/api/throw/validation"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── generic Exception returns 500 error page for web ───────────────────
    @Test
    void generic_Web_Returns500View() throws Exception {
        mockMvc.perform(get("/throw/generic"))
                .andExpect(status().isInternalServerError())
                .andExpect(view().name("error/500"))
                .andExpect(model().attribute("errorCode", 500));
    }

    // ── generic Exception returns 500 JSON for API ─────────────────────────
    @Test
    void generic_Api_Returns500Json() throws Exception {
        mockMvc.perform(get("/api/throw/generic"))
                .andExpect(status().isInternalServerError())
                .andExpect(jsonPath("$.success").value(false));
    }
}
