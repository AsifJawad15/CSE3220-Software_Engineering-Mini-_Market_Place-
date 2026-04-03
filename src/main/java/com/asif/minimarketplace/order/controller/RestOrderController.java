package com.asif.minimarketplace.order.controller;

import com.asif.minimarketplace.common.dto.ApiResponse;
import com.asif.minimarketplace.common.dto.DtoMapper;
import com.asif.minimarketplace.common.dto.OrderDTO;
import com.asif.minimarketplace.common.dto.OrderItemDTO;
import com.asif.minimarketplace.order.entity.Order;
import com.asif.minimarketplace.order.entity.OrderItem;
import com.asif.minimarketplace.order.service.CheckoutService;
import com.asif.minimarketplace.order.service.OrderService;
import com.asif.minimarketplace.payment.PaymentMethod;
import com.asif.minimarketplace.seller.entity.SellerProfile;
import com.asif.minimarketplace.seller.service.SellerProfileService;
import com.asif.minimarketplace.user.entity.User;
import com.asif.minimarketplace.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * REST API for order operations.
 *  - Buyer endpoints: /api/buyer/orders/**
 *  - Seller endpoints: /api/seller/orders/**
 */
@RestController
@RequiredArgsConstructor
public class RestOrderController {

    private final CheckoutService checkoutService;
    private final OrderService orderService;
    private final SellerProfileService sellerProfileService;
    private final UserRepository userRepository;

    private User getCurrentUser(UserDetails userDetails) {
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new RuntimeException("User not found"));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // BUYER ENDPOINTS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * POST /api/buyer/orders/checkout?addressId=1
     * Place an order from the current cart.
     * Returns 201 Created with the new order.
     */
    @PostMapping("/api/buyer/orders/checkout")
    public ResponseEntity<ApiResponse<OrderDTO>> checkout(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Long addressId,
            @RequestParam(defaultValue = "COD") PaymentMethod paymentMethod) {
        User user = getCurrentUser(userDetails);
        Order order = checkoutService.checkout(user.getId(), addressId, paymentMethod);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success("Order placed successfully", DtoMapper.toOrder(order)));
    }

    /**
     * GET /api/buyer/orders
     * Returns all orders for the authenticated buyer.
     */
    @GetMapping("/api/buyer/orders")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> getBuyerOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        List<OrderDTO> orders = orderService.getBuyerOrders(user.getId())
                .stream().map(DtoMapper::toOrder).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(orders));
    }

    /**
     * GET /api/buyer/orders/{id}
     * Returns a specific order detail for the authenticated buyer.
     * Returns 404 if not found, 403 if order belongs to another buyer.
     */
    @GetMapping("/api/buyer/orders/{id}")
    public ResponseEntity<ApiResponse<OrderDTO>> getBuyerOrderDetail(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        User user = getCurrentUser(userDetails);
        Order order = orderService.getBuyerOrderDetail(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success(DtoMapper.toOrder(order)));
    }

    /**
     * DELETE /api/buyer/orders/{id}
     * Cancel a PENDING order. Returns 204 No Content on success.
     * Returns 400 if order is not in PENDING state.
     */
    @DeleteMapping("/api/buyer/orders/{id}")
    public ResponseEntity<ApiResponse<OrderDTO>> cancelOrder(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long id) {
        User user = getCurrentUser(userDetails);
        Order order = orderService.cancelOrder(user.getId(), id);
        return ResponseEntity.ok(ApiResponse.success("Order cancelled", DtoMapper.toOrder(order)));
    }

    // ═══════════════════════════════════════════════════════════════════════
    // SELLER ENDPOINTS
    // ═══════════════════════════════════════════════════════════════════════

    /**
     * GET /api/seller/orders
     * Returns all order items belonging to the authenticated seller's products.
     */
    @GetMapping("/api/seller/orders")
    public ResponseEntity<ApiResponse<List<OrderItemDTO>>> getSellerOrders(
            @AuthenticationPrincipal UserDetails userDetails) {
        User user = getCurrentUser(userDetails);
        List<OrderItemDTO> items = orderService.getSellerOrderItems(user.getId())
                .stream().map(DtoMapper::toOrderItem).collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(items));
    }

    /**
     * PATCH /api/seller/orders/{orderId}/advance
     * Advance the order status to the next state (PENDING→CONFIRMED→PACKED→SHIPPED→DELIVERED).
     * Returns 200 OK with updated order.
     */
    @PatchMapping("/api/seller/orders/{orderId}/advance")
    public ResponseEntity<ApiResponse<OrderDTO>> advanceOrderStatus(
            @AuthenticationPrincipal UserDetails userDetails,
            @PathVariable Long orderId) {
        User user = getCurrentUser(userDetails);
        SellerProfile profile = sellerProfileService.getProfileByUserId(user.getId());
        Order order = orderService.advanceOrderStatus(orderId, profile.getId());
        return ResponseEntity.ok(ApiResponse.success("Order status updated", DtoMapper.toOrder(order)));
    }
}

