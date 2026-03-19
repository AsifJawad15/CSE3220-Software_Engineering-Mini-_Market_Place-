package com.asif.minimarketplace.admin.controller;

import com.asif.minimarketplace.product.entity.Product;
import com.asif.minimarketplace.product.service.ProductService;
import com.asif.minimarketplace.seller.entity.ApprovalStatus;
import com.asif.minimarketplace.seller.entity.SellerProfile;
import com.asif.minimarketplace.seller.service.SellerProfileService;
import com.asif.minimarketplace.user.entity.User;
import com.asif.minimarketplace.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin")
@RequiredArgsConstructor
public class AdminController {

    private final UserRepository userRepository;
    private final SellerProfileService sellerProfileService;
    private final ProductService productService;

    // ── Dashboard ──────────────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = userRepository.findByEmail(userDetails.getUsername()).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("totalProducts", productService.countTotal());
        model.addAttribute("activeProducts", productService.countActive());
        model.addAttribute("pendingSellers", sellerProfileService.countByStatus(ApprovalStatus.PENDING));
        model.addAttribute("approvedSellers", sellerProfileService.countByStatus(ApprovalStatus.APPROVED));
        return "admin/dashboard";
    }

    // ── Seller Approval ────────────────────────────────────────────────────
    @GetMapping("/sellers")
    public String listSellers(Model model) {
        List<SellerProfile> sellers = sellerProfileService.findAll();
        model.addAttribute("sellers", sellers);
        return "admin/sellers";
    }

    @PostMapping("/sellers/{id}/approve")
    public String approveSeller(@PathVariable Long id, RedirectAttributes ra) {
        sellerProfileService.approve(id);
        ra.addFlashAttribute("successMessage", "Seller approved!");
        return "redirect:/admin/sellers";
    }

    @PostMapping("/sellers/{id}/reject")
    public String rejectSeller(@PathVariable Long id, RedirectAttributes ra) {
        sellerProfileService.reject(id);
        ra.addFlashAttribute("successMessage", "Seller rejected.");
        return "redirect:/admin/sellers";
    }

    // ── User Management ────────────────────────────────────────────────────
    @GetMapping("/users")
    public String listUsers(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        return "admin/users";
    }

    // ── Product Moderation ─────────────────────────────────────────────────
    @GetMapping("/products")
    public String listProducts(Model model) {
        List<Product> products = productService.findAll();
        model.addAttribute("products", products);
        return "admin/products";
    }

    @PostMapping("/products/{id}/toggle")
    public String toggleProduct(@PathVariable Long id, RedirectAttributes ra) {
        Product p = productService.toggleActive(id);
        ra.addFlashAttribute("successMessage",
                "Product \"" + p.getName() + "\" is now " + (p.isActive() ? "active" : "inactive"));
        return "redirect:/admin/products";
    }
}
