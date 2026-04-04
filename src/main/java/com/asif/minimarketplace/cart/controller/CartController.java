package com.asif.minimarketplace.cart.controller;

import com.asif.minimarketplace.cart.entity.Cart;
import com.asif.minimarketplace.cart.service.CartService;
import com.asif.minimarketplace.user.entity.User;
import com.asif.minimarketplace.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Slf4j
@Controller
@RequestMapping("/buyer/cart")
@RequiredArgsConstructor
public class CartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ── View Cart ──────────────────────────────────────────────────────────
    @GetMapping
    public String viewCart(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        try {
            User user = getCurrentUser(userDetails);
            Cart cart = cartService.getOrCreateCart(user.getId());
            BigDecimal total = cartService.calculateTotal(cart);
            model.addAttribute("cart", cart);
            model.addAttribute("cartTotal", total);
            model.addAttribute("user", user);
        } catch (Exception e) {
            log.error("Error loading cart: {}", e.getMessage(), e);
            model.addAttribute("cart", null);
            model.addAttribute("cartTotal", BigDecimal.ZERO);
            model.addAttribute("errorMessage", "Could not load your cart. Please try again.");
        }
        return "buyer/cart";
    }

    // ── Add to Cart ────────────────────────────────────────────────────────
    @PostMapping("/add")
    public String addToCart(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity,
            RedirectAttributes ra) {
        try {
            User user = getCurrentUser(userDetails);
            cartService.addItem(user.getId(), productId, quantity);
            ra.addFlashAttribute("successMessage", "Item added to cart!");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/buyer/cart";
    }

    // ── Update Item Quantity ───────────────────────────────────────────────
    @PostMapping("/update/{itemId}")
    public String updateItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId,
            @RequestParam int quantity,
            RedirectAttributes ra) {
        try {
            User user = getCurrentUser(userDetails);
            cartService.updateItemQuantity(user.getId(), itemId, quantity);
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/buyer/cart";
    }

    // ── Remove Item ────────────────────────────────────────────────────────
    @PostMapping("/remove/{itemId}")
    public String removeItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId,
            RedirectAttributes ra) {
        User user = getCurrentUser(userDetails);
        cartService.removeItem(user.getId(), itemId);
        ra.addFlashAttribute("successMessage", "Item removed from cart.");
        return "redirect:/buyer/cart";
    }
}

