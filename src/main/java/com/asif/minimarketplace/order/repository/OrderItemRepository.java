package com.asif.minimarketplace.order.repository;

import com.asif.minimarketplace.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /** Eagerly loads order + product so seller/orders.html can access item.order.status and item.product.name. */
    @Query("SELECT DISTINCT oi FROM OrderItem oi " +
           "LEFT JOIN FETCH oi.order o " +
           "LEFT JOIN FETCH oi.product " +
           "WHERE oi.sellerProfile.id = :sellerProfileId " +
           "ORDER BY o.createdAt DESC")
    List<OrderItem> findBySellerProfileId(@Param("sellerProfileId") Long sellerProfileId);

    List<OrderItem> findByOrderId(Long orderId);
}
