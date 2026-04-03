package com.asif.minimarketplace.buyer.controller;

import com.asif.minimarketplace.buyer.dto.AddressRequest;
import com.asif.minimarketplace.buyer.dto.UpdateBuyerProfileRequest;
import com.asif.minimarketplace.buyer.entity.Address;
import com.asif.minimarketplace.buyer.entity.BuyerProfile;
import com.asif.minimarketplace.buyer.service.BuyerProfileService;
import com.asif.minimarketplace.cart.entity.Cart;
import com.asif.minimarketplace.cart.service.CartService;
import com.asif.minimarketplace.order.service.OrderService;
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
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
public class BuyerControllerTest {

    private MockMvc mockMvc;

    @Mock
    private BuyerProfileService buyerProfileService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private CartService cartService;
    @Mock
    private OrderService orderService;
    @Mock
    private UserDetails userDetails;

    @InjectMocks
    private BuyerController buyerController;

    private User testUser;
    private BuyerProfile testProfile;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(buyerController)
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

        testProfile = new BuyerProfile();
        testProfile.setId(1L);
        testProfile.setUser(testUser);
    }

    private void mockAuthentication() {
        when(userDetails.getUsername()).thenReturn("buyer@example.com");
        when(userRepository.findByEmail("buyer@example.com")).thenReturn(Optional.of(testUser));
    }

    @Test
    void dashboard_LoadsForBuyer() throws Exception {
        mockAuthentication();
        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(testProfile);
        when(buyerProfileService.getAddresses(1L)).thenReturn(new ArrayList<>());
        
        Cart mockCart = new Cart();
        mockCart.setItems(new ArrayList<>());
        when(cartService.getOrCreateCart(1L)).thenReturn(mockCart);
        when(orderService.getBuyerOrders(1L)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/buyer/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("buyer/dashboard"))
                .andExpect(model().attributeExists("user", "profile", "addressCount", "cartItemCount", "orderCount"));
    }

    @Test
    void profile_LoadsProfilePage() throws Exception {
        mockAuthentication();
        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(testProfile);

        mockMvc.perform(get("/buyer/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("buyer/profile"))
                .andExpect(model().attributeExists("user", "profile", "updateRequest"));
    }

    @Test
    void updateProfile_ValidRequest_Redirects() throws Exception {
        mockAuthentication();
        
        mockMvc.perform(post("/buyer/profile")
                        .param("phone", "1234567890"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/buyer/profile"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(buyerProfileService).updateProfile(eq(1L), any(UpdateBuyerProfileRequest.class));
    }

    @Test
    void updateProfile_InvalidRequest_StaysOnPage() throws Exception {
        mockAuthentication();
        when(buyerProfileService.getProfileByUserId(1L)).thenReturn(testProfile);

        mockMvc.perform(post("/buyer/profile")
                        .param("phone", "123456789012345678901234567890")) // Exceeds size 20 to trigger validation
                .andExpect(status().isOk())
                .andExpect(view().name("buyer/profile"))
                .andExpect(model().attributeExists("user", "profile", "updateRequest"));

        verify(buyerProfileService, never()).updateProfile(anyLong(), any());
    }

    @Test
    void addresses_LoadsPage() throws Exception {
        mockAuthentication();
        when(buyerProfileService.getAddresses(1L)).thenReturn(new ArrayList<>());

        mockMvc.perform(get("/buyer/addresses"))
                .andExpect(status().isOk())
                .andExpect(view().name("buyer/addresses"))
                .andExpect(model().attributeExists("user", "addresses", "newAddress"));
    }

    @Test
    void addAddress_ValidRequest_Redirects() throws Exception {
        mockAuthentication();

        mockMvc.perform(post("/buyer/addresses")
                        .param("label", "Home")
                        .param("line1", "123 Main St")
                        .param("city", "Cityville")
                        .param("postal", "12345")
                        .param("country", "Country")
                        .param("phone", "1234567"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/buyer/addresses"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(buyerProfileService).addAddress(eq(1L), any(AddressRequest.class));
    }

    @Test
    void addAddress_InvalidRequest_StaysOnPage() throws Exception {
        mockAuthentication();
        when(buyerProfileService.getAddresses(1L)).thenReturn(new ArrayList<>());

        mockMvc.perform(post("/buyer/addresses")
                        .param("line1", "") // Empty to trigger validation errors for @NotBlank
                        .param("city", "")
                        .param("country", ""))
                .andExpect(status().isOk())
                .andExpect(view().name("buyer/addresses"))
                .andExpect(model().attributeExists("user", "addresses"));

        verify(buyerProfileService, never()).addAddress(anyLong(), any());
    }

    @Test
    void setDefaultAddress_Works() throws Exception {
        mockAuthentication();

        mockMvc.perform(post("/buyer/addresses/1/default"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/buyer/addresses"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(buyerProfileService).setDefaultAddress(1L, 1L);
    }
    
    @Test
    void deleteAddress_Works() throws Exception {
        mockAuthentication();

        mockMvc.perform(post("/buyer/addresses/1/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/buyer/addresses"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(buyerProfileService).deleteAddress(1L, 1L);
    }
}