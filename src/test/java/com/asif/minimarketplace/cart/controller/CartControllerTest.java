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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class CartControllerTest {

    private MockMvc mockMvc;

    @Mock
    private CartService cartService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private CartController cartController;

    private User testUser;
    private Cart testCart;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(cartController)
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

    // buyer cart page loads
    @Test
    void viewCart_LoadsPage() throws Exception {
        mockAuthentication();
        when(cartService.getOrCreateCart(1L)).thenReturn(testCart);
        when(cartService.calculateTotal(testCart)).thenReturn(BigDecimal.ZERO);

        mockMvc.perform(get("/buyer/cart"))
                .andExpect(status().isOk())
                .andExpect(view().name("buyer/cart"))
                .andExpect(model().attributeExists("cart", "cartTotal", "user"));
    }

    // add item from web flow redirects correctly
    @Test
    void addToCart_ValidRequest_Redirects() throws Exception {
        mockAuthentication();

        mockMvc.perform(post("/buyer/cart/add")
                        .param("productId", "10")
                        .param("quantity", "2"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/buyer/cart"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(cartService).addItem(1L, 10L, 2);
    }
}