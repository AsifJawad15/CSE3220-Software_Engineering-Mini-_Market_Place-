package com.asif.minimarketplace.order.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import org.mockito.junit.jupiter.MockitoExtension;

import com.asif.minimarketplace.buyer.entity.Address;
import com.asif.minimarketplace.buyer.entity.BuyerProfile;
import com.asif.minimarketplace.buyer.service.BuyerProfileService;
import com.asif.minimarketplace.cart.entity.Cart;
import com.asif.minimarketplace.cart.entity.CartItem;
import com.asif.minimarketplace.cart.service.CartService;
import com.asif.minimarketplace.common.exception.InsufficientStockException;
import com.asif.minimarketplace.order.entity.Order;
import com.asif.minimarketplace.order.entity.OrderItem;
import com.asif.minimarketplace.order.entity.OrderStatus;
import com.asif.minimarketplace.order.repository.OrderRepository;
import com.asif.minimarketplace.payment.PaymentMethod;
import com.asif.minimarketplace.payment.PaymentResult;
import com.asif.minimarketplace.payment.strategy.PaymentStrategy;
import com.asif.minimarketplace.payment.strategy.PaymentStrategyFactory;
import com.asif.minimarketplace.product.entity.Product;
import com.asif.minimarketplace.product.service.InventoryService;
import com.asif.minimarketplace.seller.entity.SellerProfile;
import com.asif.minimarketplace.user.entity.User;

@ExtendWith(MockitoExtension.class)
class CheckoutServiceTest {

    @Mock
    private BuyerProfileService buyerProfileService;
    
    @Mock
    private CartService cartService;
    
    @Mock
    private InventoryService inventoryService;
    
    @Mock
    private OrderRepository orderRepository;

    @Mock
    private PaymentStrategyFactory paymentStrategyFactory;

    @Mock
    private PaymentStrategy paymentStrategy;

    @InjectMocks
    private CheckoutService checkoutService;

    private User user;
    private BuyerProfile buyerProfile;
    private SellerProfile sellerProfile;
    private Product product;
    private Cart cart;
    private CartItem cartItem;
    private Address address;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setId(1L);
        user.setEmail("buyer@example.com");

        buyerProfile = new BuyerProfile();
        buyerProfile.setId(1L);
        buyerProfile.setUser(user);

        sellerProfile = new SellerProfile();
        sellerProfile.setId(1L);

        product = new Product();
        product.setId(1L);
        product.setPrice(new BigDecimal("100.00"));
        product.setSeller(sellerProfile);

        cartItem = new CartItem();
        cartItem.setId(1L);
        cartItem.setProduct(product);
        cartItem.setQuantity(2);
        cartItem.setUnitPriceSnapshot(new BigDecimal("100.00"));

        cart = new Cart();
        cart.setId(1L);
        cart.setBuyerProfile(buyerProfile);
        cart.setItems(new ArrayList<>(List.of(cartItem)));

        address = new Address();
        address.setId(1L);
        address.setBuyerProfile(buyerProfile);
        address.setLine1("123 Street");
        address.setCity("City");
        address.setPostal("12345");
        address.setCountry("Country");
    }

    @Test
    void shouldSucceedWhenCartAndStockAreValid() {
        // Arrange
        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(buyerProfile);
        when(cartService.getOrCreateCart(1L)).thenReturn(cart);
        doNothing().when(inventoryService).validateStock(1L, 2);
        when(buyerProfileService.getAddresses(1L)).thenReturn(List.of(address));
        when(cartService.calculateTotal(cart)).thenReturn(new BigDecimal("200.00"));

        when(paymentStrategyFactory.getStrategy(PaymentMethod.COD)).thenReturn(paymentStrategy);
        when(paymentStrategy.pay(any(), any())).thenReturn(PaymentResult.builder().success(true).build());
        
        Order savedOrder = new Order();
        savedOrder.setId(1L);
        savedOrder.setTotalAmount(new BigDecimal("200.00"));
        savedOrder.setStatus(OrderStatus.PENDING);
        when(orderRepository.save(any(Order.class))).thenReturn(savedOrder);
        
        doNothing().when(inventoryService).decreaseStock(1L, 2);
        doNothing().when(cartService).clearCart(cart);

        // Act
        Order order = checkoutService.checkout(1L, 1L, PaymentMethod.COD);

        // Assert
        assertNotNull(order);
        assertEquals(1L, order.getId());
        assertEquals(new BigDecimal("200.00"), order.getTotalAmount());
    }

    @Test
    void shouldFailWhenCartIsEmpty() {
        // Arrange
        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(buyerProfile);
        
        Cart emptyCart = new Cart();
        emptyCart.setItems(new ArrayList<>());
        when(cartService.getOrCreateCart(1L)).thenReturn(emptyCart);

        // Act & Assert
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> checkoutService.checkout(1L, 1L, PaymentMethod.COD));
    }

    @Test
    void shouldFailWhenStockIsInsufficient() {
        // Arrange
        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(buyerProfile);
        when(cartService.getOrCreateCart(1L)).thenReturn(cart);

        doThrow(new InsufficientStockException("Not enough stock for product"))
            .when(inventoryService).validateStock(1L, 2);

        // Act & Assert
        InsufficientStockException exception = assertThrows(InsufficientStockException.class, 
            () -> checkoutService.checkout(1L, 1L, PaymentMethod.COD));
    }

    @Test
    void shouldCreateOrderWithCorrectPropertiesAndItems() {
        // Arrange
        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(buyerProfile);
        when(cartService.getOrCreateCart(1L)).thenReturn(cart);
        doNothing().when(inventoryService).validateStock(1L, 2);
        
        when(buyerProfileService.getAddresses(1L)).thenReturn(List.of(address));
        when(cartService.calculateTotal(cart)).thenReturn(new BigDecimal("200.00"));
        when(paymentStrategyFactory.getStrategy(PaymentMethod.COD)).thenReturn(paymentStrategy);
        when(paymentStrategy.pay(any(), any())).thenReturn(PaymentResult.builder().success(true).build());        
        Order savedOrder = new Order();
        savedOrder.setId(1L);
        when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
            Order passedOrder = invocation.getArgument(0);
            passedOrder.setId(1L); // Simulate save
            return passedOrder;
        });

        // Act
        Order order = checkoutService.checkout(1L, 1L, PaymentMethod.COD);

        // Assert
        assertEquals(OrderStatus.PENDING, order.getStatus(), "Order should be created with PENDING status");
        assertEquals(new BigDecimal("200.00"), order.getTotalAmount(), "Total amount should match cart sum");
        
        assertNotNull(order.getItems());
        assertEquals(1, order.getItems().size(), "Order items should be created for each cart item");
        
        OrderItem orderItem = order.getItems().get(0);
        assertEquals(product, orderItem.getProduct());
    }

    @Test
    void shouldFallbackWhenAddressIsInvalid() {
        // Arrange
        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(buyerProfile);
        when(cartService.getOrCreateCart(1L)).thenReturn(cart);
        doNothing().when(inventoryService).validateStock(1L, 2);
        
        when(buyerProfileService.getAddresses(1L)).thenReturn(List.of(address));
        when(cartService.calculateTotal(cart)).thenReturn(new BigDecimal("200.00"));
        when(paymentStrategyFactory.getStrategy(PaymentMethod.COD)).thenReturn(paymentStrategy);
        when(paymentStrategy.pay(any(), any())).thenReturn(PaymentResult.builder().success(true).build());
        when(orderRepository.save(any(Order.class))).thenAnswer(i -> i.getArgument(0));
        doNothing().when(inventoryService).decreaseStock(1L, 2);
        doNothing().when(cartService).clearCart(cart);

        // Act — pass non-existent address ID 999
        Order order = checkoutService.checkout(1L, 999L, PaymentMethod.COD);

        // Assert — service falls back to "Address not found" instead of throwing
        assertNotNull(order);
        assertEquals("Address not found", order.getShippingAddress());
    }

    @Test
    void shouldFailWhenStockDeductionFailsMidway() {
        // Arrange
        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(buyerProfile);
        when(cartService.getOrCreateCart(1L)).thenReturn(cart);
        doNothing().when(inventoryService).validateStock(1L, 2);
        when(buyerProfileService.getAddresses(1L)).thenReturn(List.of(address));
        when(cartService.calculateTotal(cart)).thenReturn(new BigDecimal("200.00"));

        when(paymentStrategyFactory.getStrategy(PaymentMethod.COD)).thenReturn(paymentStrategy);
        when(paymentStrategy.pay(any(), any())).thenReturn(PaymentResult.builder().success(true).build());
        
        when(orderRepository.save(any(Order.class))).thenReturn(new Order());
        
        doThrow(new RuntimeException("Database error")).when(inventoryService).decreaseStock(1L, 2);

        // Act & Assert
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> checkoutService.checkout(1L, 1L, PaymentMethod.COD));
            
        assertEquals("Database error", exception.getMessage());
        
        // Ensure cart was not cleared (rollback implication)
        verify(cartService, never()).clearCart(any(Cart.class));
    }
}
