package com.asif.minimarketplace.order.service;

import com.asif.minimarketplace.buyer.entity.BuyerProfile;
import com.asif.minimarketplace.buyer.service.BuyerProfileService;
import com.asif.minimarketplace.cart.entity.Cart;
import com.asif.minimarketplace.cart.entity.CartItem;
import com.asif.minimarketplace.cart.service.CartService;
import com.asif.minimarketplace.common.exception.InsufficientStockException;
import com.asif.minimarketplace.order.entity.Order;
import com.asif.minimarketplace.order.entity.OrderStatus;
import com.asif.minimarketplace.order.repository.OrderRepository;
import com.asif.minimarketplace.product.entity.Product;
import com.asif.minimarketplace.product.service.InventoryService;
import com.asif.minimarketplace.seller.entity.SellerProfile;
import com.asif.minimarketplace.user.entity.RoleName;
import com.asif.minimarketplace.user.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock private BuyerProfileService buyerProfileService;
    @Mock private CartService cartService;
    @Mock private InventoryService inventoryService;
    @Mock private OrderRepository orderRepository;

    @InjectMocks private CheckoutService checkoutService;

    private User buyerUser;
    private BuyerProfile buyerProfile;
    private SellerProfile sellerProfile;
    private Product product;
    private Cart cart;

    @BeforeEach
    void setUp() {
        buyerUser = User.builder().fullName("Buyer").email("buyer@test.com")
                .password("enc").role(RoleName.BUYER).enabled(true).build();
        buyerUser.setId(1L);

        buyerProfile = BuyerProfile.builder().user(buyerUser).build();
        buyerProfile.setId(1L);

        sellerProfile = new SellerProfile();
        sellerProfile.setId(10L);

        product = Product.builder().name("Widget").price(BigDecimal.valueOf(20.00))
                .stockQuantity(50).active(true).seller(sellerProfile).build();
        product.setId(1L);

        CartItem cartItem = CartItem.builder().product(product).quantity(2)
                .unitPriceSnapshot(BigDecimal.valueOf(20.00)).build();
        cartItem.setId(1L);

        cart = Cart.builder().buyerProfile(buyerProfile).items(new ArrayList<>(List.of(cartItem))).build();
        cart.setId(1L);
    }

    @Test
    void checkout_success_createsOrder() {
        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(buyerProfile);
        when(cartService.getOrCreateCart(1L)).thenReturn(cart);
        when(cartService.calculateTotal(cart)).thenReturn(BigDecimal.valueOf(40.00));
        when(orderRepository.save(any(Order.class))).thenAnswer(inv -> {
            Order o = inv.getArgument(0);
            o.setId(100L);
            return o;
        });

        Order result = checkoutService.checkout(1L, null);

        assertThat(result.getId()).isEqualTo(100L);
        assertThat(result.getStatus()).isEqualTo(OrderStatus.PENDING);
        assertThat(result.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(40.00));
        verify(inventoryService).validateStock(1L, 2);
        verify(inventoryService).decreaseStock(1L, 2);
        verify(cartService).clearCart(cart);
    }

    @Test
    void checkout_emptyCart_throwsIllegalState() {
        Cart emptyCart = Cart.builder().buyerProfile(buyerProfile).items(new ArrayList<>()).build();
        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(buyerProfile);
        when(cartService.getOrCreateCart(1L)).thenReturn(emptyCart);

        assertThatThrownBy(() -> checkoutService.checkout(1L, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Cart is empty");
    }

    @Test
    void checkout_insufficientStock_propagatesException() {
        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(buyerProfile);
        when(cartService.getOrCreateCart(1L)).thenReturn(cart);
        doThrow(new InsufficientStockException("Widget", 2, 0))
                .when(inventoryService).validateStock(1L, 2);

        assertThatThrownBy(() -> checkoutService.checkout(1L, null))
                .isInstanceOf(InsufficientStockException.class);
        verify(orderRepository, never()).save(any());
    }
}

