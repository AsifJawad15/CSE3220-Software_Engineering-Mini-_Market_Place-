package com.asif.minimarketplace.order.controller;

import com.asif.minimarketplace.common.exception.AccessDeniedException;
import com.asif.minimarketplace.common.exception.GlobalExceptionHandler;
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

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class RestOrderControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CheckoutService checkoutService;
    @Mock
    private OrderService orderService;
    @Mock
    private SellerProfileService sellerProfileService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private RestOrderController restOrderController;

    private User testUser;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(restOrderController)
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
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        testUser = new User();
        testUser.setId(1L);
        testUser.setEmail("user@example.com");

        testOrder = new Order();
        testOrder.setId(100L);
        com.asif.minimarketplace.buyer.entity.BuyerProfile bp = new com.asif.minimarketplace.buyer.entity.BuyerProfile(); bp.setUser(testUser); testOrder.setBuyerProfile(bp);
        testOrder.setItems(new ArrayList<>());
    }

    private void mockAuthentication() {
        when(userDetails.getUsername()).thenReturn("user@example.com");
        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(testUser));
    }

    // REST checkout returns 201
    @Test
    void checkout_Returns201() throws Exception {
        mockAuthentication();
        when(checkoutService.checkout(eq(1L), anyLong())).thenReturn(testOrder);

        mockMvc.perform(post("/api/buyer/orders/checkout")
                        .param("addressId", "1"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testOrder.getId().intValue()));
    }

    // REST buyer orders returns 200
    @Test
    void getBuyerOrders_Returns200() throws Exception {
        mockAuthentication();
        when(orderService.getBuyerOrders(1L)).thenReturn(List.of(testOrder));

        mockMvc.perform(get("/api/buyer/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // REST buyer order detail returns 403 for wrong buyer
    @Test
    void getBuyerOrderDetail_WrongBuyer_Returns403() throws Exception {
        mockAuthentication();
        when(orderService.getBuyerOrderDetail(1L, 100L))
                .thenThrow(new AccessDeniedException("This order does not belong to you"));

        mockMvc.perform(get("/api/buyer/orders/100"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.success").value(false));
    }
    
    // Test exact match of getBuyerOrderDetail 200
    @Test
    void getBuyerOrderDetail_Returns200() throws Exception {
        mockAuthentication();
        when(orderService.getBuyerOrderDetail(1L, 100L)).thenReturn(testOrder);
        
        mockMvc.perform(get("/api/buyer/orders/100"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // REST seller orders returns 200
    @Test
    void getSellerOrders_Returns200() throws Exception {
        mockAuthentication();
        OrderItem item = new OrderItem();
        item.setId(200L);
        item.setOrder(testOrder); // Needed for DTO mapping to prevent NPE
        // Also needs a Product
        com.asif.minimarketplace.product.entity.Product product = new com.asif.minimarketplace.product.entity.Product();
        product.setId(300L);
        product.setName("Test Product");
        item.setProduct(product);
        item.setPriceAtPurchase(new java.math.BigDecimal("10.00")); com.asif.minimarketplace.seller.entity.SellerProfile sp = new com.asif.minimarketplace.seller.entity.SellerProfile(); sp.setId(400L); item.setSellerProfile(sp);
        item.setQuantity(2);
        
        when(orderService.getSellerOrderItems(1L)).thenReturn(List.of(item));

        mockMvc.perform(get("/api/seller/orders"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}