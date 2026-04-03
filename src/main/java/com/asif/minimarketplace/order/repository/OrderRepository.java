package com.asif.minimarketplace.order.repository;

import com.asif.minimarketplace.order.entity.Order;
import com.asif.minimarketplace.order.entity.OrderStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    /** Buyer order list — eagerly loads items so template can access order.items size. */
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.items i " +
           "LEFT JOIN FETCH i.product " +
           "LEFT JOIN FETCH i.sellerProfile " +
           "WHERE o.buyerProfile.id = :buyerProfileId " +
           "ORDER BY o.createdAt DESC")
    List<Order> findByBuyerProfileIdOrderByCreatedAtDesc(@Param("buyerProfileId") Long buyerProfileId);

    /** Single order detail — also fetches buyerProfile (for ownership check) + all items. */
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.buyerProfile bp " +
           "LEFT JOIN FETCH bp.user " +
           "LEFT JOIN FETCH o.items i " +
           "LEFT JOIN FETCH i.product " +
           "LEFT JOIN FETCH i.sellerProfile " +
           "WHERE o.id = :orderId")
    Optional<Order> findByIdWithItems(@Param("orderId") Long orderId);

    /** Admin view — all orders with buyer info and item count. */
    @Query("SELECT DISTINCT o FROM Order o " +
           "LEFT JOIN FETCH o.buyerProfile bp " +
           "LEFT JOIN FETCH bp.user " +
           "LEFT JOIN FETCH o.items " +
           "ORDER BY o.createdAt DESC")
    List<Order> findAllWithDetails();

    List<Order> findByStatusOrderByCreatedAtDesc(OrderStatus status);

    long countByStatus(OrderStatus status);
}
