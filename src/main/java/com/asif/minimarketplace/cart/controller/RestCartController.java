package com.asif.minimarketplace.cart.controller;

import com.asif.minimarketplace.cart.entity.Cart;
import com.asif.minimarketplace.cart.service.CartService;
import com.asif.minimarketplace.common.dto.ApiResponse;
import com.asif.minimarketplace.common.dto.CartDTO;
import com.asif.minimarketplace.common.dto.DtoMapper;
import com.asif.minimarketplace.user.entity.User;
import com.asif.minimarketplace.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

/**
 * REST API for cart operations (BUYER only — /api/buyer/cart).
 */
@RestController
@RequestMapping("/api/buyer/cart")
@RequiredArgsConstructor
public class RestCartController {

    private final CartService cartService;
    private final UserRepository userRepository;

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    /**
     * GET /api/buyer/cart
     * Returns the current buyer's cart with all items and total.
     */
    @GetMapping
    public ResponseEntity<ApiResponse<CartDTO>> getCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        Cart cart = cartService.getOrCreateCart(user.getId());
        BigDecimal total = cartService.calculateTotal(cart);
        return ResponseEntity.ok(ApiResponse.success(DtoMapper.toCart(cart, total)));
    }

    /**
     * POST /api/buyer/cart/items
     * Add a product to the cart.
     * Body: { "productId": 1, "quantity": 2 }
     * Returns 201 Created with updated cart.
     */
    @PostMapping("/items")
    public ResponseEntity<ApiResponse<CartDTO>> addItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam Long productId,
            @RequestParam(defaultValue = "1") int quantity) {
        User user = getCurrentUser(userDetails);
        Cart cart = cartService.addItem(user.getId(), productId, quantity);
        BigDecimal total = cartService.calculateTotal(cart);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Item added to cart", DtoMapper.toCart(cart, total)));
    }

    /**
     * PATCH /api/buyer/cart/items/{itemId}
     * Update quantity of a cart item.
     * Returns 200 OK with updated cart, or 204 No Content if item removed (qty ≤ 0).
     */
    @PatchMapping("/items/{itemId}")
    public ResponseEntity<ApiResponse<CartDTO>> updateItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId,
            @RequestParam int quantity) {
        User user = getCurrentUser(userDetails);
        Cart cart = cartService.updateItemQuantity(user.getId(), itemId, quantity);
        if (quantity <= 0) {
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        }
        BigDecimal total = cartService.calculateTotal(cart);
        return ResponseEntity.ok(ApiResponse.success("Cart updated", DtoMapper.toCart(cart, total)));
    }

    /**
     * DELETE /api/buyer/cart/items/{itemId}
     * Remove a specific item from cart.
     * Returns 204 No Content.
     */
    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<Void> removeItem(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long itemId) {
        User user = getCurrentUser(userDetails);
        cartService.removeItem(user.getId(), itemId);
        return ResponseEntity.noContent().build();
    }

    /**
     * DELETE /api/buyer/cart
     * Clear the entire cart.
     * Returns 204 No Content.
     */
    @DeleteMapping
    public ResponseEntity<Void> clearCart(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        Cart cart = cartService.getOrCreateCart(user.getId());
        cartService.clearCart(cart);
        return ResponseEntity.noContent().build();
    }
}

