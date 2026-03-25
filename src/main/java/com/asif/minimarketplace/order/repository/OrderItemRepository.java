package com.asif.minimarketplace.order.repository;

import com.asif.minimarketplace.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findBySellerProfileId(Long sellerProfileId);
    List<OrderItem> findByOrderId(Long orderId);
}

