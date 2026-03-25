package com.asif.minimarketplace.cart.service;

import com.asif.minimarketplace.buyer.entity.BuyerProfile;
import com.asif.minimarketplace.buyer.service.BuyerProfileService;
import com.asif.minimarketplace.cart.entity.Cart;
import com.asif.minimarketplace.cart.entity.CartItem;
import com.asif.minimarketplace.cart.repository.CartRepository;
import com.asif.minimarketplace.common.exception.InsufficientStockException;
import com.asif.minimarketplace.common.exception.NotFoundException;
import com.asif.minimarketplace.product.entity.Product;
import com.asif.minimarketplace.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CartService {

    private final CartRepository cartRepository;
    private final BuyerProfileService buyerProfileService;
    private final ProductService productService;

    /**
     * Get or create a cart for the buyer.
     */
    @Transactional
    public Cart getOrCreateCart(Long userId) {
        BuyerProfile profile = buyerProfileService.getProfileByUserId(userId);
        return cartRepository.findByBuyerProfileId(profile.getId())
                .orElseGet(() -> {
                    Cart newCart = Cart.builder().buyerProfile(profile).build();
                    return cartRepository.save(newCart);
                });
    }

    /**
     * Add item to cart or increase quantity if already present.
     */
    @Transactional
    public Cart addItem(Long userId, Long productId, int quantity) {
        Cart cart = getOrCreateCart(userId);
        Product product = productService.findById(productId);

        if (!product.isActive()) {
            throw new NotFoundException("Product is not available");
        }
        if (product.getStockQuantity() < quantity) {
            throw new InsufficientStockException("Not enough stock. Available: " + product.getStockQuantity());
        }

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(i -> i.getProduct().getId().equals(productId))
                .findFirst();

        if (existingItem.isPresent()) {
            CartItem item = existingItem.get();
            int newQty = item.getQuantity() + quantity;
            if (product.getStockQuantity() < newQty) {
                throw new InsufficientStockException("Not enough stock. Available: " + product.getStockQuantity());
            }
            item.setQuantity(newQty);
            item.setUnitPriceSnapshot(product.getPrice());
        } else {
            CartItem newItem = CartItem.builder()
                    .cart(cart)
                    .product(product)
                    .quantity(quantity)
                    .unitPriceSnapshot(product.getPrice())
                    .build();
            cart.getItems().add(newItem);
        }

        return cartRepository.save(cart);
    }

    /**
     * Update item quantity (set to 0 or negative removes it).
     */
    @Transactional
    public Cart updateItemQuantity(Long userId, Long cartItemId, int quantity) {
        Cart cart = getOrCreateCart(userId);

        CartItem item = cart.getItems().stream()
                .filter(i -> i.getId().equals(cartItemId))
                .findFirst()
                .orElseThrow(() -> new NotFoundException("Cart item not found"));

        if (quantity <= 0) {
            cart.getItems().remove(item);
        } else {
            if (item.getProduct().getStockQuantity() < quantity) {
                throw new InsufficientStockException("Not enough stock. Available: " + item.getProduct().getStockQuantity());
            }
            item.setQuantity(quantity);
        }

        return cartRepository.save(cart);
    }

    /**
     * Remove a specific item from cart.
     */
    @Transactional
    public Cart removeItem(Long userId, Long cartItemId) {
        Cart cart = getOrCreateCart(userId);
        cart.getItems().removeIf(i -> i.getId().equals(cartItemId));
        return cartRepository.save(cart);
    }

    /**
     * Calculate cart total.
     */
    public BigDecimal calculateTotal(Cart cart) {
        return cart.getItems().stream()
                .map(i -> i.getUnitPriceSnapshot().multiply(BigDecimal.valueOf(i.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    /**
     * Clear all items from cart.
     */
    @Transactional
    public void clearCart(Cart cart) {
        cart.getItems().clear();
        cartRepository.save(cart);
    }
}

