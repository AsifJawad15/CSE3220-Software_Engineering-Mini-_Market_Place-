package com.asif.minimarketplace.admin.controller;

import com.asif.minimarketplace.common.dto.*;
import com.asif.minimarketplace.order.entity.OrderStatus;
import com.asif.minimarketplace.order.service.OrderService;
import com.asif.minimarketplace.product.service.ProductService;
import com.asif.minimarketplace.seller.entity.ApprovalStatus;
import com.asif.minimarketplace.seller.service.SellerProfileService;
import com.asif.minimarketplace.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * REST API for admin operations (ROLE_ADMIN only — /api/admin/**).
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class RestAdminController {

    private final UserRepository userRepository;
    private final SellerProfileService sellerProfileService;
    private final ProductService productService;
    private final OrderService orderService;

    // ── Dashboard Stats ────────────────────────────────────────────────────

    /**
     * GET /api/admin/stats
     * Returns dashboard statistics as a JSON map.
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getDashboardStats() {
        Map<String, Long> stats = Map.of(
                "totalUsers",      userRepository.count(),
                "totalProducts",   productService.countTotal(),
                "activeProducts",  productService.countActive(),
                "pendingSellers",  sellerProfileService.countByStatus(ApprovalStatus.PENDING),
                "approvedSellers", sellerProfileService.countByStatus(ApprovalStatus.APPROVED),
                "totalOrders",     orderService.countByStatus(OrderStatus.PENDING)
                                 + orderService.countByStatus(OrderStatus.CONFIRMED)
                                 + orderService.countByStatus(OrderStatus.PACKED)
                                 + orderService.countByStatus(OrderStatus.SHIPPED)
                                 + orderService.countByStatus(OrderStatus.DELIVERED)
        );
        return ResponseEntity.ok(ApiResponse.success(stats));
    }

    // ── User Management ────────────────────────────────────────────────────

    /**
     * GET /api/admin/users
     * Returns all users.
     */
    @GetMapping("/users")
    public ResponseEntity<ApiResponse<List<UserDTO>>> listUsers() {
        List<UserDTO> users = userRepository.findAll().stream()
                .map(DtoMapper::toUser)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(users));
    }

    /**
     * GET /api/admin/users/{id}
     * Returns a specific user by ID.
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<ApiResponse<UserDTO>> getUser(@PathVariable Long id) {
        UserDTO dto = userRepository.findById(id)
                .map(DtoMapper::toUser)
                .orElseThrow(() -> new com.asif.minimarketplace.common.exception.NotFoundException("User", id));
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    // ── Seller Approval ────────────────────────────────────────────────────

    /**
     * GET /api/admin/sellers
     * Returns all seller profiles (with approval status).
     */
    @GetMapping("/sellers")
    public ResponseEntity<ApiResponse<List<SellerProfileDTO>>> listSellers() {
        List<SellerProfileDTO> sellers = sellerProfileService.findAll().stream()
                .map(DtoMapper::toSellerProfile)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(sellers));
    }

    /**
     * GET /api/admin/sellers?status=PENDING
     * Filter sellers by approval status.
     */
    @GetMapping("/sellers/pending")
    public ResponseEntity<ApiResponse<List<SellerProfileDTO>>> listPendingSellers() {
        List<SellerProfileDTO> sellers = sellerProfileService.findByStatus(ApprovalStatus.PENDING).stream()
                .map(DtoMapper::toSellerProfile)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(sellers));
    }

    /**
     * PATCH /api/admin/sellers/{id}/approve
     * Approve a seller. Returns 200 OK with updated profile.
     */
    @PatchMapping("/sellers/{id}/approve")
    public ResponseEntity<ApiResponse<SellerProfileDTO>> approveSeller(@PathVariable Long id) {
        SellerProfileDTO dto = DtoMapper.toSellerProfile(sellerProfileService.approve(id));
        return ResponseEntity.ok(ApiResponse.success("Seller approved", dto));
    }

    /**
     * PATCH /api/admin/sellers/{id}/reject
     * Reject a seller. Returns 200 OK with updated profile.
     */
    @PatchMapping("/sellers/{id}/reject")
    public ResponseEntity<ApiResponse<SellerProfileDTO>> rejectSeller(@PathVariable Long id) {
        SellerProfileDTO dto = DtoMapper.toSellerProfile(sellerProfileService.reject(id));
        return ResponseEntity.ok(ApiResponse.success("Seller rejected", dto));
    }

    // ── Product Moderation ─────────────────────────────────────────────────

    /**
     * GET /api/admin/products
     * Returns all products (active + inactive).
     */
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<List<ProductDTO>>> listProducts() {
        List<ProductDTO> products = productService.findAll().stream()
                .map(DtoMapper::toProduct)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(products));
    }

    /**
     * PATCH /api/admin/products/{id}/toggle
     * Toggle product active/inactive. Returns 200 OK with updated product.
     */
    @PatchMapping("/products/{id}/toggle")
    public ResponseEntity<ApiResponse<ProductDTO>> toggleProduct(@PathVariable Long id) {
        ProductDTO dto = DtoMapper.toProduct(productService.toggleActive(id));
        return ResponseEntity.ok(ApiResponse.success(
                "Product is now " + (dto.isActive() ? "active" : "inactive"), dto));
    }

    // ── Order Management ───────────────────────────────────────────────────

    /**
     * GET /api/admin/orders
     * Returns all orders (all statuses, all buyers).
     */
    @GetMapping("/orders")
    public ResponseEntity<ApiResponse<List<OrderDTO>>> listOrders() {
        List<OrderDTO> orders = orderService.getAllOrders().stream()
                .map(DtoMapper::toOrder)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(orders));
    }
}

