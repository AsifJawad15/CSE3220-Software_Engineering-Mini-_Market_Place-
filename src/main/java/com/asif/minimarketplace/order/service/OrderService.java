package com.asif.minimarketplace.order.service;

import com.asif.minimarketplace.buyer.entity.BuyerProfile;
import com.asif.minimarketplace.buyer.service.BuyerProfileService;
import com.asif.minimarketplace.common.exception.AccessDeniedException;
import com.asif.minimarketplace.common.exception.NotFoundException;
import com.asif.minimarketplace.order.entity.Order;
import com.asif.minimarketplace.order.entity.OrderItem;
import com.asif.minimarketplace.order.entity.OrderStatus;
import com.asif.minimarketplace.order.repository.OrderItemRepository;
import com.asif.minimarketplace.order.repository.OrderRepository;
import com.asif.minimarketplace.product.service.InventoryService;
import com.asif.minimarketplace.seller.entity.SellerProfile;
import com.asif.minimarketplace.seller.service.SellerProfileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final BuyerProfileService buyerProfileService;
    private final SellerProfileService sellerProfileService;
    private final InventoryService inventoryService;

    // ── Buyer methods ──────────────────────────────────────────────────────

    public List<Order> getBuyerOrders(Long userId) {
        BuyerProfile profile = buyerProfileService.getProfileByUserId(userId);
        return orderRepository.findByBuyerProfileIdOrderByCreatedAtDesc(profile.getId());
    }

    public Order getBuyerOrderDetail(Long userId, Long orderId) {
        BuyerProfile profile = buyerProfileService.getProfileByUserId(userId);
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));
        if (!order.getBuyerProfile().getId().equals(profile.getId())) {
            throw new AccessDeniedException("You do not own this order");
        }
        return order;
    }

    @Transactional
    public Order cancelOrder(Long userId, Long orderId) {
        Order order = getBuyerOrderDetail(userId, orderId);
        if (order.getStatus() != OrderStatus.PENDING) {
            throw new IllegalStateException("Only PENDING orders can be cancelled");
        }
        order.setStatus(OrderStatus.CANCELLED);
        // Restore stock
        for (OrderItem item : order.getItems()) {
            inventoryService.increaseStock(item.getProduct().getId(), item.getQuantity());
        }
        log.info("Order #{} cancelled by buyer", orderId);
        return orderRepository.save(order);
    }

    // ── Seller methods ─────────────────────────────────────────────────────

    /**
     * Get all order items belonging to this seller (across all orders).
     */
    public List<OrderItem> getSellerOrderItems(Long userId) {
        SellerProfile profile = sellerProfileService.getProfileByUserId(userId);
        return orderItemRepository.findBySellerProfileId(profile.getId());
    }

    @Transactional
    public Order advanceOrderStatus(Long orderId, Long sellerId) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new NotFoundException("Order", orderId));

        // Verify seller owns at least one item in this order
        boolean sellerOwnsItem = order.getItems().stream()
                .anyMatch(i -> i.getSellerProfile().getId().equals(sellerId));
        if (!sellerOwnsItem) {
            throw new AccessDeniedException("You do not have items in this order");
        }

        OrderStatus next = nextStatus(order.getStatus());
        order.setStatus(next);
        log.info("Order #{} status advanced to {}", orderId, next);
        return orderRepository.save(order);
    }

    private OrderStatus nextStatus(OrderStatus current) {
        return switch (current) {
            case PENDING    -> OrderStatus.CONFIRMED;
            case CONFIRMED  -> OrderStatus.PACKED;
            case PACKED     -> OrderStatus.SHIPPED;
            case SHIPPED    -> OrderStatus.DELIVERED;
            default         -> current;
        };
    }

    // ── Admin methods ──────────────────────────────────────────────────────

    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public long countByStatus(OrderStatus status) {
        return orderRepository.countByStatus(status);
    }
}

