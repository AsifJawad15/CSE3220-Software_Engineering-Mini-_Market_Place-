package com.asif.minimarketplace.order.controller;

import com.asif.minimarketplace.buyer.entity.Address;
import com.asif.minimarketplace.buyer.service.BuyerProfileService;
import com.asif.minimarketplace.cart.entity.Cart;
import com.asif.minimarketplace.cart.entity.CartItem;
import com.asif.minimarketplace.cart.service.CartService;
import com.asif.minimarketplace.common.exception.AccessDeniedException;
import com.asif.minimarketplace.order.entity.Order;
import com.asif.minimarketplace.order.entity.OrderItem;
import com.asif.minimarketplace.order.service.CheckoutService;
import com.asif.minimarketplace.order.service.OrderService;
import com.asif.minimarketplace.seller.entity.SellerProfile;
import com.asif.minimarketplace.seller.service.SellerProfileService;
import com.asif.minimarketplace.user.entity.User;
import com.asif.minimarketplace.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.MethodParameter;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class OrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CheckoutService checkoutService;
    @Mock
    private OrderService orderService;
    @Mock
    private CartService cartService;
    @Mock
    private BuyerProfileService buyerProfileService;
    @Mock
    private SellerProfileService sellerProfileService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private OrderController orderController;

    private User testUser;
    private Cart testCart;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(orderController)
                .setCustomArgumentResolvers(new HandlerMethodArgumentResolver() {
                    @Override
                    public boolean supportsParameter(MethodParameter parameter) {
                        return parameter.getParameterType().isAssignableFrom(UserDetails.class);
                    }
                    @Override
                    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                                  NativeWebRequest webRequest, WebDataBinderFactory binderFactory) {
                        return userDetails;
                    }
                })
                .build();

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("buyer@example.com");

        testCart = new Cart();
        testCart.setId(1L);
        testCart.setItems(new ArrayList<>());

        testOrder = new Order();
        testOrder.setId(1L);
        com.asif.minimarketplace.buyer.entity.BuyerProfile bp = new com.asif.minimarketplace.buyer.entity.BuyerProfile(); bp.setUser(testUser); testOrder.setBuyerProfile(bp);
    }

    private void mockAuthentication() {
        when(userDetails.getUsername()).thenReturn("buyer@example.com");
        when(userRepository.findByEmail("buyer@example.com")).thenReturn(Optional.of(testUser));
    }

    // checkout page loads when cart has items
    @Test
    void checkoutPage_WithItems_Loads() throws Exception {
        mockAuthentication();
        testCart.getItems().add(new CartItem()); // Has items
        when(cartService.getOrCreateCart(1L)).thenReturn(testCart);
        when(buyerProfileService.getAddresses(1L)).thenReturn(new ArrayList<>());
        when(cartService.calculateTotal(testCart)).thenReturn(new BigDecimal("100.00"));

        mockMvc.perform(get("/buyer/checkout"))
                .andExpect(status().isOk())
                .andExpect(view().name("buyer/checkout"))
                .andExpect(model().attributeExists("cart", "cartTotal", "addresses", "user"));
    }

    // checkout page redirects to cart when empty
    @Test
    void checkoutPage_EmptyCart_RedirectsToCart() throws Exception {
        mockAuthentication();
        // Cart is empty initially
        when(cartService.getOrCreateCart(1L)).thenReturn(testCart);

        mockMvc.perform(get("/buyer/checkout"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/buyer/cart"));
    }

    // successful checkout redirects to orders
    @Test
    void placeOrder_Success_RedirectsToOrders() throws Exception {
        mockAuthentication();
        when(checkoutService.checkout(eq(1L), anyLong())).thenReturn(testOrder);

        mockMvc.perform(post("/buyer/checkout")
                        .param("addressId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/buyer/orders"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(checkoutService).checkout(1L, 1L);
    }

    // checkout failure shows error and redirects back
    @Test
    void placeOrder_Failure_RedirectsBackWithFlash() throws Exception {
        mockAuthentication();
        when(checkoutService.checkout(eq(1L), anyLong()))
                .thenThrow(new RuntimeException("Insufficient stock"));

        mockMvc.perform(post("/buyer/checkout")
                        .param("addressId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/buyer/checkout"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    // buyer orders page loads
    @Test
    void buyerOrders_LoadsPage() throws Exception {
        mockAuthentication();
        when(orderService.getBuyerOrders(1L)).thenReturn(List.of(testOrder));

        mockMvc.perform(get("/buyer/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("buyer/orders"))
                .andExpect(model().attributeExists("orders", "user"));
    }

    // buyer order detail loads
    @Test
    void buyerOrderDetail_LoadsPage() throws Exception {
        mockAuthentication();
        when(orderService.getBuyerOrderDetail(1L, 1L)).thenReturn(testOrder);

        mockMvc.perform(get("/buyer/orders/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("buyer/order-detail"))
                .andExpect(model().attributeExists("order", "user"));
    }

    // buyer cancel order works
    @Test
    void cancelOrder_Success_RedirectsToOrders() throws Exception {
        mockAuthentication();
        when(orderService.cancelOrder(1L, 1L)).thenReturn(testOrder);

        mockMvc.perform(post("/buyer/orders/1/cancel"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/buyer/orders"))
                .andExpect(flash().attributeExists("successMessage"));
    }

    // buyer cancel non-pending order shows error
    @Test
    void cancelOrder_NotPending_ShowsError() throws Exception {
        mockAuthentication();
        when(orderService.cancelOrder(1L, 1L)).thenThrow(new RuntimeException("Only PENDING orders can be cancelled."));

        mockMvc.perform(post("/buyer/orders/1/cancel"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/buyer/orders"))
                .andExpect(flash().attributeExists("errorMessage"));
    }

    // seller orders page loads
    @Test
    void sellerOrders_LoadsPage() throws Exception {
        mockAuthentication();
        when(orderService.getSellerOrderItems(1L)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/seller/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("seller/orders"))
                .andExpect(model().attributeExists("orderItems", "user"));
    }

    // seller advance status works
    @Test
    void advanceStatus_Valid_Redirects() throws Exception {
        mockAuthentication();
        SellerProfile profile = new SellerProfile();
        profile.setId(10L);
        when(sellerProfileService.getProfileByUserId(1L)).thenReturn(profile);
        when(orderService.advanceOrderStatus(1L, 10L)).thenReturn(testOrder);

        mockMvc.perform(post("/seller/orders/1/advance"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/seller/orders"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(orderService).advanceOrderStatus(1L, 10L);
    }

    // seller without permission cannot advance other seller’s order
    @Test
    void advanceStatus_InvalidPermission_ShowsErrorFlash() throws Exception {
        mockAuthentication();
        SellerProfile profile = new SellerProfile();
        profile.setId(10L);
        when(sellerProfileService.getProfileByUserId(1L)).thenReturn(profile);
        
        when(orderService.advanceOrderStatus(1L, 10L))
                .thenThrow(new AccessDeniedException("Seller cannot advance an order that doesn't contain their items"));

        mockMvc.perform(post("/seller/orders/1/advance"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/seller/orders"))
                .andExpect(flash().attributeExists("errorMessage"));
    }
}