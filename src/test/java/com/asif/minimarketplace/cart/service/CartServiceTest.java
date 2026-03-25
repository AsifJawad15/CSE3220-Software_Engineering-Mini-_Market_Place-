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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock private CartRepository cartRepository;
    @Mock private BuyerProfileService buyerProfileService;
    @Mock private ProductService productService;

    @InjectMocks private CartService cartService;

    private BuyerProfile buyerProfile;
    private Cart cart;
    private Product product;

    @BeforeEach
    void setUp() {
        buyerProfile = new BuyerProfile();
        buyerProfile.setId(1L);

        cart = Cart.builder().buyerProfile(buyerProfile).items(new ArrayList<>()).build();
        cart.setId(1L);

        product = Product.builder()
                .name("Test Product")
                .price(BigDecimal.valueOf(25.00))
                .stockQuantity(100)
                .active(true)
                .build();
        product.setId(1L);
    }

    @Test
    void getOrCreateCart_existingCart_returnsIt() {
        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(buyerProfile);
        when(cartRepository.findByBuyerProfileId(1L)).thenReturn(Optional.of(cart));

        Cart result = cartService.getOrCreateCart(1L);
        assertThat(result.getId()).isEqualTo(1L);
        verify(cartRepository, never()).save(any());
    }

    @Test
    void getOrCreateCart_noCart_createsNew() {
        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(buyerProfile);
        when(cartRepository.findByBuyerProfileId(1L)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> {
            Cart c = inv.getArgument(0);
            c.setId(2L);
            return c;
        });

        Cart result = cartService.getOrCreateCart(1L);
        assertThat(result.getId()).isEqualTo(2L);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void addItem_newItem_success() {
        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(buyerProfile);
        when(cartRepository.findByBuyerProfileId(1L)).thenReturn(Optional.of(cart));
        when(productService.findById(1L)).thenReturn(product);
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        Cart result = cartService.addItem(1L, 1L, 2);
        assertThat(result.getItems()).hasSize(1);
        assertThat(result.getItems().get(0).getQuantity()).isEqualTo(2);
    }

    @Test
    void addItem_inactiveProduct_throwsNotFound() {
        product.setActive(false);
        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(buyerProfile);
        when(cartRepository.findByBuyerProfileId(1L)).thenReturn(Optional.of(cart));
        when(productService.findById(1L)).thenReturn(product);

        assertThatThrownBy(() -> cartService.addItem(1L, 1L, 1))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("not available");
    }

    @Test
    void addItem_insufficientStock_throwsException() {
        product.setStockQuantity(1);
        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(buyerProfile);
        when(cartRepository.findByBuyerProfileId(1L)).thenReturn(Optional.of(cart));
        when(productService.findById(1L)).thenReturn(product);

        assertThatThrownBy(() -> cartService.addItem(1L, 1L, 5))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void calculateTotal_returnsCorrectSum() {
        CartItem item1 = CartItem.builder().quantity(2).unitPriceSnapshot(BigDecimal.valueOf(10.00)).build();
        CartItem item2 = CartItem.builder().quantity(3).unitPriceSnapshot(BigDecimal.valueOf(5.00)).build();
        cart.getItems().add(item1);
        cart.getItems().add(item2);

        BigDecimal total = cartService.calculateTotal(cart);
        assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(35.00));
    }

    @Test
    void removeItem_removesCorrectItem() {
        CartItem item = CartItem.builder().cart(cart).product(product).quantity(1)
                .unitPriceSnapshot(BigDecimal.TEN).build();
        item.setId(10L);
        cart.getItems().add(item);

        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(buyerProfile);
        when(cartRepository.findByBuyerProfileId(1L)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenAnswer(inv -> inv.getArgument(0));

        Cart result = cartService.removeItem(1L, 10L);
        assertThat(result.getItems()).isEmpty();
    }
}

