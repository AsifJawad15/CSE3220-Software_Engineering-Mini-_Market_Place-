package com.asif.minimarketplace.common.dto;

import com.asif.minimarketplace.cart.entity.Cart;
import com.asif.minimarketplace.cart.entity.CartItem;
import com.asif.minimarketplace.order.entity.Order;
import com.asif.minimarketplace.order.entity.OrderItem;
import com.asif.minimarketplace.product.entity.Category;
import com.asif.minimarketplace.product.entity.Product;
import com.asif.minimarketplace.seller.entity.SellerProfile;
import com.asif.minimarketplace.user.entity.User;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Static mapper utility: entity → DTO (no circular refs, safe for JSON serialization).
 */
public final class DtoMapper {

    private DtoMapper() {}

    // ── Category ───────────────────────────────────────────────────────────
    public static CategoryDTO toCategory(Category c) {
        return CategoryDTO.builder()
                .id(c.getId())
                .name(c.getName())
                .slug(c.getSlug())
                .build();
    }

    // ── Product ────────────────────────────────────────────────────────────
    public static ProductDTO toProduct(Product p) {
        return ProductDTO.builder()
                .id(p.getId())
                .name(p.getName())
                .description(p.getDescription())
                .price(p.getPrice())
                .stockQuantity(p.getStockQuantity())
                .imageUrl(p.getImageUrl())
                .active(p.isActive())
                .categoryId(p.getCategory().getId())
                .categoryName(p.getCategory().getName())
                .sellerId(p.getSeller().getId())
                .sellerShopName(p.getSeller().getShopName())
                .build();
    }

    // ── Cart ───────────────────────────────────────────────────────────────
    public static CartItemDTO toCartItem(CartItem i) {
        BigDecimal subtotal = i.getUnitPriceSnapshot()
                .multiply(BigDecimal.valueOf(i.getQuantity()));
        return CartItemDTO.builder()
                .id(i.getId())
                .productId(i.getProduct().getId())
                .productName(i.getProduct().getName())
                .productImageUrl(i.getProduct().getImageUrl())
                .quantity(i.getQuantity())
                .unitPriceSnapshot(i.getUnitPriceSnapshot())
                .subtotal(subtotal)
                .build();
    }

    public static CartDTO toCart(Cart cart, BigDecimal total) {
        List<CartItemDTO> items = cart.getItems().stream()
                .map(DtoMapper::toCartItem)
                .collect(Collectors.toList());
        return CartDTO.builder()
                .id(cart.getId())
                .items(items)
                .total(total)
                .itemCount(items.size())
                .build();
    }

    // ── Order ──────────────────────────────────────────────────────────────
    public static OrderItemDTO toOrderItem(OrderItem i) {
        BigDecimal subtotal = i.getPriceAtPurchase()
                .multiply(BigDecimal.valueOf(i.getQuantity()));
        return OrderItemDTO.builder()
                .id(i.getId())
                .productId(i.getProduct().getId())
                .productName(i.getProduct().getName())
                .sellerProfileId(i.getSellerProfile().getId())
                .sellerShopName(i.getSellerProfile().getShopName())
                .quantity(i.getQuantity())
                .priceAtPurchase(i.getPriceAtPurchase())
                .subtotal(subtotal)
                .build();
    }

    public static OrderDTO toOrder(Order o) {
        List<OrderItemDTO> items = o.getItems().stream()
                .map(DtoMapper::toOrderItem)
                .collect(Collectors.toList());
        return OrderDTO.builder()
                .id(o.getId())
                .status(o.getStatus().name())
                .totalAmount(o.getTotalAmount())
                .shippingAddress(o.getShippingAddress())
                .items(items)
                .createdAt(o.getCreatedAt())
                .buyerProfileId(o.getBuyerProfile().getId())
                .buyerName(o.getBuyerProfile().getUser().getFullName())
                .build();
    }

    // ── User ───────────────────────────────────────────────────────────────
    public static UserDTO toUser(User u) {
        return UserDTO.builder()
                .id(u.getId())
                .fullName(u.getFullName())
                .email(u.getEmail())
                .role(u.getRole().name())
                .enabled(u.isEnabled())
                .build();
    }

    // ── SellerProfile ──────────────────────────────────────────────────────
    public static SellerProfileDTO toSellerProfile(SellerProfile s) {
        return SellerProfileDTO.builder()
                .id(s.getId())
                .userId(s.getUser().getId())
                .fullName(s.getUser().getFullName())
                .email(s.getUser().getEmail())
                .shopName(s.getShopName())
                .phone(s.getPhone())
                .approvalStatus(s.getApprovalStatus().name())
                .build();
    }
}

