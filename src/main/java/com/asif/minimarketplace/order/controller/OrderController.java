package com.asif.minimarketplace.order.controller;

import com.asif.minimarketplace.buyer.entity.Address;
import com.asif.minimarketplace.buyer.service.BuyerProfileService;
import com.asif.minimarketplace.cart.entity.Cart;
import com.asif.minimarketplace.cart.service.CartService;
import com.asif.minimarketplace.order.entity.Order;
import com.asif.minimarketplace.order.entity.OrderItem;
import com.asif.minimarketplace.order.service.CheckoutService;
import com.asif.minimarketplace.payment.PaymentMethod;
import com.asif.minimarketplace.order.service.OrderService;
import com.asif.minimarketplace.seller.entity.SellerProfile;
import com.asif.minimarketplace.seller.service.SellerProfileService;
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
import java.util.List;

@Slf4j
@Controller
@RequiredArgsConstructor
public class OrderController {

    private final CheckoutService checkoutService;
    private final OrderService orderService;
    private final CartService cartService;
    private final BuyerProfileService buyerProfileService;
    private final SellerProfileService sellerProfileService;
    private final UserRepository userRepository;

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ── Checkout Page ──────────────────────────────────────────────────────
    @GetMapping("/buyer/checkout")
    public String checkoutPage(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getCurrentUser(userDetails);
        Cart cart = cartService.getOrCreateCart(user.getId());
        if (cart.getItems().isEmpty()) {
            return "redirect:/buyer/cart";
        }
        List<Address> addresses = buyerProfileService.getAddresses(user.getId());
        BigDecimal total = cartService.calculateTotal(cart);
        model.addAttribute("cart", cart);
        model.addAttribute("cartTotal", total);
        model.addAttribute("addresses", addresses);
        model.addAttribute("paymentMethods", PaymentMethod.values());
        model.addAttribute("user", user);
        return "buyer/checkout";
    }

    // ── Place Order ────────────────────────────────────────────────────────
    @PostMapping("/buyer/checkout")
    public String placeOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long addressId,
            @RequestParam(defaultValue = "COD") PaymentMethod paymentMethod,
            RedirectAttributes ra) {
        try {
            User user = getCurrentUser(userDetails);
            Order order = checkoutService.checkout(user.getId(), addressId, paymentMethod);
            ra.addFlashAttribute("successMessage",
                    "Order #" + order.getId() + " placed successfully!");
            return "redirect:/buyer/orders";
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/buyer/checkout";
        }
    }

    // ── Buyer: Order History ───────────────────────────────────────────────
    @GetMapping("/buyer/orders")
    public String buyerOrders(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getCurrentUser(userDetails);
        List<Order> orders = orderService.getBuyerOrders(user.getId());
        model.addAttribute("orders", orders);
        model.addAttribute("user", user);
        return "buyer/orders";
    }

    // ── Buyer: Order Detail ────────────────────────────────────────────────
    @GetMapping("/buyer/orders/{id}")
    public String buyerOrderDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            Model model) {
        User user = getCurrentUser(userDetails);
        Order order = orderService.getBuyerOrderDetail(user.getId(), id);
        model.addAttribute("order", order);
        model.addAttribute("user", user);
        return "buyer/order-detail";
    }

    // ── Buyer: Cancel Order ────────────────────────────────────────────────
    @PostMapping("/buyer/orders/{id}/cancel")
    public String cancelOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id,
            RedirectAttributes ra) {
        try {
            User user = getCurrentUser(userDetails);
            orderService.cancelOrder(user.getId(), id);
            ra.addFlashAttribute("successMessage", "Order #" + id + " cancelled.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/buyer/orders";
    }

    // ── Seller: Orders ─────────────────────────────────────────────────────
    @GetMapping("/seller/orders")
    public String sellerOrders(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User user = getCurrentUser(userDetails);
        List<OrderItem> orderItems = orderService.getSellerOrderItems(user.getId());
        model.addAttribute("orderItems", orderItems);
        model.addAttribute("user", user);
        return "seller/orders";
    }

    // ── Seller: Advance Order Status ───────────────────────────────────────
    @PostMapping("/seller/orders/{orderId}/advance")
    public String advanceStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId,
            RedirectAttributes ra) {
        try {
            User user = getCurrentUser(userDetails);
            SellerProfile profile = sellerProfileService.getProfileByUserId(user.getId());
            orderService.advanceOrderStatus(orderId, profile.getId());
            ra.addFlashAttribute("successMessage", "Order #" + orderId + " status updated.");
        } catch (Exception e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        return "redirect:/seller/orders";
    }
}

