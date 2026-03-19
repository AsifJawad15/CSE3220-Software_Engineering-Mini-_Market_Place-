package com.asif.minimarketplace.cart.repository;

import com.asif.minimarketplace.cart.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<Cart, Long> {
    Optional<Cart> findByBuyerProfileId(Long buyerProfileId);
}

