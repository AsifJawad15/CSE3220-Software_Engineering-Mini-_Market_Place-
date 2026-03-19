package com.asif.minimarketplace.seller.controller;

import com.asif.minimarketplace.product.dto.ProductRequest;
import com.asif.minimarketplace.product.entity.Product;
import com.asif.minimarketplace.product.service.CategoryService;
import com.asif.minimarketplace.product.service.ProductService;
import com.asif.minimarketplace.seller.dto.UpdateSellerProfileRequest;
import com.asif.minimarketplace.seller.entity.ApprovalStatus;
import com.asif.minimarketplace.seller.entity.SellerProfile;
import com.asif.minimarketplace.seller.service.SellerProfileService;
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
@RequestMapping("/seller")
@RequiredArgsConstructor
public class SellerController {

    private final SellerProfileService sellerProfileService;
    private final ProductService productService;
    private final CategoryService categoryService;
    private final UserRepository userRepository;

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ── Dashboard ──────────────────────────────────────────────────────────
    @GetMapping("/dashboard")
    public String dashboard(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getCurrentUser(userDetails);
        SellerProfile profile = sellerProfileService.getProfileByUserId(user.getId());
        long productCount = productService.countBySeller(profile.getId());
        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        model.addAttribute("productCount", productCount);
        return "seller/dashboard";
    }

    // ── Profile ────────────────────────────────────────────────────────────
    @GetMapping("/profile")
    public String profile(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getCurrentUser(userDetails);
        SellerProfile profile = sellerProfileService.getProfileByUserId(user.getId());
        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        model.addAttribute("updateRequest", new UpdateSellerProfileRequest());
        return "seller/profile";
    }

    @PostMapping("/profile")
    public String updateProfile(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute("updateRequest") UpdateSellerProfileRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(userDetails);
        if (result.hasErrors()) {
            SellerProfile profile = sellerProfileService.getProfileByUserId(user.getId());
            model.addAttribute("user", user);
            model.addAttribute("profile", profile);
            return "seller/profile";
        }
        sellerProfileService.updateProfile(user.getId(), request);
        redirectAttributes.addFlashAttribute("successMessage", "Profile updated successfully!");
        return "redirect:/seller/profile";
    }

    // ── Products ───────────────────────────────────────────────────────────
    @GetMapping("/products")
    public String listProducts(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getCurrentUser(userDetails);
        SellerProfile profile = sellerProfileService.getProfileByUserId(user.getId());
        List<Product> products = productService.findBySeller(profile.getId());
        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        model.addAttribute("products", products);
        return "seller/products";
    }

    @GetMapping("/products/new")
    public String showCreateForm(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getCurrentUser(userDetails);
        SellerProfile profile = sellerProfileService.getProfileByUserId(user.getId());
        if (profile.getApprovalStatus() != ApprovalStatus.APPROVED) {
            return "redirect:/seller/dashboard";
        }
        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        model.addAttribute("productRequest", new ProductRequest());
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("editMode", false);
        return "seller/product-form";
    }

    @PostMapping("/products/new")
    public String createProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @ModelAttribute("productRequest") ProductRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(userDetails);
        SellerProfile profile = sellerProfileService.getProfileByUserId(user.getId());
        if (result.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("profile", profile);
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("editMode", false);
            return "seller/product-form";
        }
        productService.create(request, profile);
        redirectAttributes.addFlashAttribute("successMessage", "Product created successfully!");
        return "redirect:/seller/products";
    }

    @GetMapping("/products/{id}/edit")
    public String showEditForm(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            Model model) {
        User user = getCurrentUser(userDetails);
        SellerProfile profile = sellerProfileService.getProfileByUserId(user.getId());
        Product product = productService.findById(id);
        if (!product.getSeller().getId().equals(profile.getId())) {
            return "redirect:/seller/products";
        }
        ProductRequest req = new ProductRequest();
        req.setName(product.getName());
        req.setDescription(product.getDescription());
        req.setPrice(product.getPrice());
        req.setStockQuantity(product.getStockQuantity());
        req.setImageUrl(product.getImageUrl());
        req.setCategoryId(product.getCategory().getId());
        req.setActive(product.isActive());

        model.addAttribute("user", user);
        model.addAttribute("profile", profile);
        model.addAttribute("productRequest", req);
        model.addAttribute("productId", id);
        model.addAttribute("categories", categoryService.findAll());
        model.addAttribute("editMode", true);
        return "seller/product-form";
    }

    @PostMapping("/products/{id}/edit")
    public String updateProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            @Valid @ModelAttribute("productRequest") ProductRequest request,
            BindingResult result,
            Model model,
            RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(userDetails);
        SellerProfile profile = sellerProfileService.getProfileByUserId(user.getId());
        if (result.hasErrors()) {
            model.addAttribute("user", user);
            model.addAttribute("profile", profile);
            model.addAttribute("productId", id);
            model.addAttribute("categories", categoryService.findAll());
            model.addAttribute("editMode", true);
            return "seller/product-form";
        }
        productService.update(id, request, profile);
        redirectAttributes.addFlashAttribute("successMessage", "Product updated!");
        return "redirect:/seller/products";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            RedirectAttributes redirectAttributes) {
        User user = getCurrentUser(userDetails);
        SellerProfile profile = sellerProfileService.getProfileByUserId(user.getId());
        productService.delete(id, profile);
        redirectAttributes.addFlashAttribute("successMessage", "Product deleted.");
        return "redirect:/seller/products";
    }
}


