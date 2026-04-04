package com.asif.minimarketplace.buyer.controller;

import com.asif.minimarketplace.buyer.dto.AddressRequest;
import com.asif.minimarketplace.buyer.dto.UpdateBuyerProfileRequest;
import com.asif.minimarketplace.buyer.entity.Address;
import com.asif.minimarketplace.buyer.entity.BuyerProfile;
import com.asif.minimarketplace.buyer.service.BuyerProfileService;
import com.asif.minimarketplace.cart.entity.Cart;
import com.asif.minimarketplace.cart.service.CartService;
import com.asif.minimarketplace.order.service.OrderService;
import com.asif.minimarketplace.user.entity.User;
import com.asif.minimarketplace.user.repository.UserRepository;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Slf4j
@Controller
@RequestMapping("/buyer")
@RequiredArgsConstructor
public class BuyerController {

    private final BuyerProfileService buyerProfileService;
    private final UserRepository userRepository;
    private final CartService cartService;
    private final OrderService orderService;

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ── Dashboard ──────────────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getCurrentUser(userDetails);
        model.addAttribute("user", user);

        // Defensively load profile — never let dashboard crash
        BuyerProfile profile = null;
        try {
            profile = buyerProfileService.getProfileByUserId(user.getId());
        } catch (Exception e) {
            log.warn("Could not load buyer profile for user {}: {}", user.getId(), e.getMessage());
        }
        model.addAttribute("profile", profile);

        // Address count
        try {
            List<Address> addresses = buyerProfileService.getAddresses(user.getId());
            model.addAttribute("addressCount", addresses.size());
        } catch (Exception e) {
            log.warn("Could not load addresses for user {}: {}", user.getId(), e.getMessage());
            model.addAttribute("addressCount", 0);
        }

        // Cart item count
        try {
            Cart cart = cartService.getOrCreateCart(user.getId());
            model.addAttribute("cartItemCount", cart.getItems().size());
        } catch (Exception e) {
            log.warn("Could not load cart for user {}: {}", user.getId(), e.getMessage());
            model.addAttribute("cartItemCount", 0);
        }

        // Order count
        try {
            model.addAttribute("orderCount", orderService.getBuyerOrders(user.getId()).size());
        } catch (Exception e) {
            log.warn("Could not load orders for user {}: {}", user.getId(), e.getMessage());
            model.addAttribute("orderCount", 0);
        }

        return "buyer/dashboard";
    }

    // ── Profile ────────────────────────────────────────────────────────────
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getCurrentUser(userDetails);
        BuyerProfile profile = buyerProfileService.getProfileByUserId(user.getId());
        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        model.addAttribute("updateRequest", new UpdateBuyerProfileRequest());
        return "buyer/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute("updateRequest") UpdateBuyerProfileRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(userDetails);
        if (result.hasErrors()) {
            BuyerProfile profile = buyerProfileService.getProfileByUserId(user.getId());
            model.addAttribute("user", user);
            model.addAttribute("profile", profile);
            return "buyer/profile";
        }
        buyerProfileService.updateProfile(user.getId(), request);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        return "redirect:/buyer/profile";
    }

    // ── Addresses ──────────────────────────────────────────────────────────
    @GetMapping("/addresses")
    public String addresses(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getCurrentUser(userDetails);
        List<Address> addresses = buyerProfileService.getAddresses(user.getId());
        model.addAttribute("user", user);
        model.addAttribute("addresses", addresses);
        model.addAttribute("newAddress", new AddressRequest());
        return "buyer/addresses";
    }

    @PostMapping("/addresses")
    public String addAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute("newAddress") AddressRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(userDetails);
        if (result.hasErrors()) {
            List<Address> addresses = buyerProfileService.getAddresses(user.getId());
            model.addAttribute("user", user);
            model.addAttribute("addresses", addresses);
            return "buyer/addresses";
        }
        buyerProfileService.addAddress(user.getId(), request);
        redirectAttributes.addFlashAttribute("successMessage", "Address added successfully!");
        return "redirect:/buyer/addresses";
    }

    @PostMapping("/addresses/{id}/default")
    public String setDefault(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(userDetails);
        buyerProfileService.setDefaultAddress(user.getId(), id);
        redirectAttributes.addFlashAttribute("successMessage", "Default address updated!");
        return "redirect:/buyer/addresses";
    }

    @PostMapping("/addresses/{id}/delete")
    public String deleteAddress(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(userDetails);
        buyerProfileService.deleteAddress(user.getId(), id);
        redirectAttributes.addFlashAttribute("successMessage", "Address deleted.");
        return "redirect:/buyer/addresses";
    }
}

