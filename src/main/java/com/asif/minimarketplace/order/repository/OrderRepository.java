package com.asif.minimarketplace.order.repository;

import com.asif.minimarketplace.order.entity.Order;
import com.asif.minimarketplace.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {
    List<Order> findByBuyerProfileIdOrderByCreatedAtDesc(Long buyerProfileId);
    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);
    long countByStatus(OrderStatus status);
}

