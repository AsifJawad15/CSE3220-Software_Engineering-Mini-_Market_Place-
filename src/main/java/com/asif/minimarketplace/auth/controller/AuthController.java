package com.asif.minimarketplace.auth.controller;
import com.asif.minimarketplace.auth.dto.RegisterBuyerRequest;
import com.asif.minimarketplace.auth.dto.RegisterSellerRequest;
import com.asif.minimarketplace.auth.service.AuthService;
import com.asif.minimarketplace.common.exception.ValidationException;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
@Slf4j
@Controller
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;
    // ── Login ──────────────────────────────────────────────────────────────
    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }
    // ── Register Buyer ─────────────────────────────────────────────────────
    @GetMapping("/register/buyer")
    public String showBuyerRegisterForm(Model model) {
        model.addAttribute("registerRequest", new RegisterBuyerRequest());
        return "auth/register-buyer";
    }
    @PostMapping("/register/buyer")
    public String registerBuyer(
            @Valid @ModelAttribute("registerRequest") RegisterBuyerRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/register-buyer";
        }
        try {
            authService.registerBuyer(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Account created successfully! Please log in.");
            return "redirect:/login";
        } catch (ValidationException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register-buyer";
        }
    }
    // ── Register Seller ────────────────────────────────────────────────────
    @GetMapping("/register/seller")
    public String showSellerRegisterForm(Model model) {
        model.addAttribute("registerRequest", new RegisterSellerRequest());
        return "auth/register-seller";
    }
    @PostMapping("/register/seller")
    public String registerSeller(
            @Valid @ModelAttribute("registerRequest") RegisterSellerRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            return "auth/register-seller";
        }
        try {
            authService.registerSeller(request);
            redirectAttributes.addFlashAttribute("successMessage",
                    "Seller account created! Please log in.");
            return "redirect:/login";
        } catch (ValidationException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "auth/register-seller";
        }
    }
}