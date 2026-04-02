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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private BuyerProfileService buyerProfileService;

    @Mock
    private ProductService productService;

    @InjectMocks
    private CartService cartService;

    private BuyerProfile profile;
    private Cart cart;
    private Product product;
    private CartItem cartItem;

    @BeforeEach
    void setUp() {
        profile = new BuyerProfile();
        profile.setId(10L);

        cart = new Cart();
        cart.setId(100L);
        cart.setBuyerProfile(profile);
        cart.setItems(new ArrayList<>());

        product = new Product();
        product.setId(50L);
        product.setActive(true);
        product.setStockQuantity(20);
        product.setPrice(new BigDecimal("10.00"));

        cartItem = new CartItem();
        cartItem.setId(200L);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setUnitPriceSnapshot(new BigDecimal("10.00"));
    }

    @Test
    void getOrCreateCart_ReturnsExistingCart() {
        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(profile);
        when(cartRepository.findByBuyerProfileIdWithItems(10L)).thenReturn(Optional.of(cart));

        Cart result = cartService.getOrCreateCart(1L);

        assertEquals(100L, result.getId());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void getOrCreateCart_CreatesMissingCart() {
        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(profile);
        when(cartRepository.findByBuyerProfileIdWithItems(10L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> {
            Cart c = i.getArgument(0);
            c.setId(101L);
            return c;
        });

        Cart result = cartService.getOrCreateCart(1L);

        assertEquals(101L, result.getId());
        assertEquals(profile, result.getBuyerProfile());
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void addItem_AddNewItemWorks() {
        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(profile);
        when(cartRepository.findByBuyerProfileIdWithItems(10L)).thenReturn(Optional.of(cart));
        when(productService.findById(50L)).thenReturn(product);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart result = cartService.addItem(1L, 50L, 2);

        assertEquals(1, result.getItems().size());
        assertEquals(50L, result.getItems().get(0).getProduct().getId());
        assertEquals(2, result.getItems().get(0).getQuantity());
    }

    @Test
    void addItem_AddingSameProductIncreasesQuantity() {
        cart.getItems().add(cartItem);
        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(profile);
        when(cartRepository.findByBuyerProfileIdWithItems(10L)).thenReturn(Optional.of(cart));
        when(productService.findById(50L)).thenReturn(product);
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart result = cartService.addItem(1L, 50L, 5);

        assertEquals(1, result.getItems().size());
        assertEquals(7, result.getItems().get(0).getQuantity());
    }

    @Test
    void addItem_ThrowsIfProductInactive() {
        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(profile);
        when(cartRepository.findByBuyerProfileIdWithItems(10L)).thenReturn(Optional.of(cart));
        
        product.setActive(false);
        when(productService.findById(50L)).thenReturn(product);

        assertThrows(NotFoundException.class, () -> cartService.addItem(1L, 50L, 1));
    }

    @Test
    void addItem_ThrowsIfOutOfStock() {
        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(profile);
        when(cartRepository.findByBuyerProfileIdWithItems(10L)).thenReturn(Optional.of(cart));
        
        product.setStockQuantity(0);
        when(productService.findById(50L)).thenReturn(product);

        assertThrows(InsufficientStockException.class, () -> cartService.addItem(1L, 50L, 1));
    }

    @Test
    void updateItemQuantity_ValidNumberWorks() {
        cart.getItems().add(cartItem);
        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(profile);
        when(cartRepository.findByBuyerProfileIdWithItems(10L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart result = cartService.updateItemQuantity(1L, 200L, 5);

        assertEquals(5, result.getItems().get(0).getQuantity());
    }

    @Test
    void updateItemQuantity_AboveStockFails() {
        cart.getItems().add(cartItem);
        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(profile);
        when(cartRepository.findByBuyerProfileIdWithItems(10L)).thenReturn(Optional.of(cart));

        assertThrows(InsufficientStockException.class, () -> cartService.updateItemQuantity(1L, 200L, 25));
    }

    @Test
    void updateItemQuantity_ZeroRemovesItem() {
        cart.getItems().add(cartItem);
        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(profile);
        when(cartRepository.findByBuyerProfileIdWithItems(10L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart result = cartService.updateItemQuantity(1L, 200L, 0);

        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void updateItemQuantity_MissingItemThrowsNotFound() {
        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(profile);
        when(cartRepository.findByBuyerProfileIdWithItems(10L)).thenReturn(Optional.of(cart));

        assertThrows(NotFoundException.class, () -> cartService.updateItemQuantity(1L, 999L, 2));
    }

    @Test
    void removeItem_SpecificItemWorks() {
        cart.getItems().add(cartItem);
        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(profile);
        when(cartRepository.findByBuyerProfileIdWithItems(10L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        Cart result = cartService.removeItem(1L, 200L);

        assertTrue(result.getItems().isEmpty());
    }

    @Test
    void clearCart_RemovesAllItems() {
        cart.getItems().add(cartItem);
        when(cartRepository.save(any(Cart.class))).thenAnswer(i -> i.getArgument(0));

        cartService.clearCart(cart);

        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository).save(cart);
    }

    @Test
    void calculateTotal_CorrectWithSnapshot() {
        CartItem item1 = new CartItem();
        item1.setQuantity(2);
        item1.setUnitPriceSnapshot(new BigDecimal("15.00")); // 30.00
        
        CartItem item2 = new CartItem();
        item2.setQuantity(3);
        item2.setUnitPriceSnapshot(new BigDecimal("10.00")); // 30.00

        cart.getItems().add(item1);
        cart.getItems().add(item2);

        BigDecimal total = cartService.calculateTotal(cart);

        assertEquals(new BigDecimal("60.00"), total);
    }
}
