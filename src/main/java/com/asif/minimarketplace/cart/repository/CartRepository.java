package com.asif.minimarketplace.cart.repository;

import com.asif.minimarketplace.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    /** Eagerly loads items → product → category to avoid LazyInitializationException. */
    @Query("SELECT DISTINCT c FROM Cart c " +
           "LEFT JOIN FETCH c.items i " +
           "LEFT JOIN FETCH i.product p " +
           "LEFT JOIN FETCH p.category " +
           "WHERE c.buyerProfile.id = :buyerProfileId")
    Optional<Cart> findByBuyerProfileIdWithItems(@Param("buyerProfileId") Long buyerProfileId);

    /** Kept for internal usage where items aren't needed (e.g. existence check). */
    Optional<Cart> findByBuyerProfileId(Long buyerProfileId);
}
