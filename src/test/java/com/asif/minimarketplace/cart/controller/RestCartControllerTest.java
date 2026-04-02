package com.asif.minimarketplace.cart.controller;

import com.asif.minimarketplace.cart.entity.Cart;
import com.asif.minimarketplace.cart.service.CartService;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class RestCartControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CartService cartService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private RestCartController restCartController;

    private User testUser;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(restCartController)
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
    }

    private void mockAuthentication() {
        when(userDetails.getUsername()).thenReturn("buyer@example.com");
        when(userRepository.findByEmail("buyer@example.com")).thenReturn(Optional.of(testUser));
    }

    // REST get cart returns 200
    @Test
    void getCart_Returns200() throws Exception {
        mockAuthentication();
        when(cartService.getOrCreateCart(1L)).thenReturn(testCart);
        when(cartService.calculateTotal(testCart)).thenReturn(BigDecimal.ZERO);

        mockMvc.perform(get("/api/buyer/cart"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testCart.getId().intValue()));
    }

    // REST add item returns 201
    @Test
    void addItem_Returns201() throws Exception {
        mockAuthentication();
        when(cartService.addItem(1L, 10L, 2)).thenReturn(testCart);
        when(cartService.calculateTotal(testCart)).thenReturn(BigDecimal.ZERO);

        mockMvc.perform(post("/api/buyer/cart/items")
                        .param("productId", "10")
                        .param("quantity", "2"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true));

        verify(cartService).addItem(1L, 10L, 2);
    }

    // REST patch quantity returns 200
    @Test
    void updateItem_QuantityGreaterThanZero_Returns200() throws Exception {
        mockAuthentication();
        when(cartService.updateItemQuantity(1L, 5L, 3)).thenReturn(testCart);
        when(cartService.calculateTotal(testCart)).thenReturn(BigDecimal.ZERO);

        mockMvc.perform(patch("/api/buyer/cart/items/5")
                        .param("quantity", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));

        verify(cartService).updateItemQuantity(1L, 5L, 3);
    }

    // REST patch quantity to zero returns no content
    @Test
    void updateItem_QuantityZeroOrLess_ReturnsNoContent() throws Exception {
        mockAuthentication();
        when(cartService.updateItemQuantity(1L, 5L, 0)).thenReturn(testCart);

        mockMvc.perform(patch("/api/buyer/cart/items/5")
                        .param("quantity", "0"))
                .andExpect(status().isNoContent());

        verify(cartService).updateItemQuantity(1L, 5L, 0);
    }

    // REST delete item returns 204
    @Test
    void removeItem_Returns204() throws Exception {
        mockAuthentication();

        mockMvc.perform(delete("/api/buyer/cart/items/5"))
                .andExpect(status().isNoContent());

        verify(cartService).removeItem(1L, 5L);
    }

    // REST clear cart returns 204
    @Test
    void clearCart_Returns204() throws Exception {
        mockAuthentication();
        when(cartService.getOrCreateCart(1L)).thenReturn(testCart);

        mockMvc.perform(delete("/api/buyer/cart"))
                .andExpect(status().isNoContent());

        verify(cartService).clearCart(testCart);
    }
}