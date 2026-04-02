package com.asif.minimarketplace.admin.controller;

import com.asif.minimarketplace.order.entity.Order;
import com.asif.minimarketplace.order.entity.OrderStatus;
import com.asif.minimarketplace.order.service.OrderService;
import com.asif.minimarketplace.product.entity.Product;
import com.asif.minimarketplace.product.service.ProductService;
import com.asif.minimarketplace.seller.entity.ApprovalStatus;
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

import java.util.List;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for AdminController -- covers dashboard stats, user listing,
 * seller approval/rejection, product moderation toggle, and order listing.
 */
@ExtendWith(MockitoExtension.class)
public class AdminControllerTest {

    private MockMvc mockMvc;

    @Mock private UserRepository userRepository;
    @Mock private SellerProfileService sellerProfileService;
    @Mock private ProductService productService;
    @Mock private OrderService orderService;
    @Mock private UserDetails userDetails;

    @InjectMocks
    private AdminController adminController;

    private User adminUser;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(adminController)
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

        adminUser = new User();
        adminUser.setId(1L);
        adminUser.setEmail("admin@example.com");
    }

    // ── admin dashboard loads with stats ───────────────────────────────────
    @Test
    void dashboard_LoadsWithStats() throws Exception {
        when(userDetails.getUsername()).thenReturn("admin@example.com");
        when(userRepository.findByEmail("admin@example.com")).thenReturn(Optional.of(adminUser));
        when(userRepository.count()).thenReturn(10L);
        when(productService.countTotal()).thenReturn(20L);
        when(productService.countActive()).thenReturn(15L);
        when(sellerProfileService.countByStatus(ApprovalStatus.PENDING)).thenReturn(3L);
        when(sellerProfileService.countByStatus(ApprovalStatus.APPROVED)).thenReturn(5L);
        when(orderService.countByStatus(any(OrderStatus.class))).thenReturn(2L);

        mockMvc.perform(get("/admin/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/dashboard"))
                .andExpect(model().attributeExists("user", "totalUsers", "totalProducts",
                        "activeProducts", "pendingSellers", "approvedSellers", "totalOrders"));
    }

    // ── admin sellers list loads ───────────────────────────────────────────
    @Test
    void listSellers_LoadsPage() throws Exception {
        when(sellerProfileService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/admin/sellers"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/sellers"))
                .andExpect(model().attributeExists("sellers"));
    }

    // ── approve seller redirects with success ──────────────────────────────
    @Test
    void approveSeller_RedirectsWithSuccess() throws Exception {
        SellerProfile sp = new SellerProfile();
        sp.setId(1L);
        when(sellerProfileService.approve(1L)).thenReturn(sp);

        mockMvc.perform(post("/admin/sellers/1/approve"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/sellers"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(sellerProfileService).approve(1L);
    }

    // ── reject seller redirects with success ───────────────────────────────
    @Test
    void rejectSeller_RedirectsWithSuccess() throws Exception {
        SellerProfile sp = new SellerProfile();
        sp.setId(1L);
        when(sellerProfileService.reject(1L)).thenReturn(sp);

        mockMvc.perform(post("/admin/sellers/1/reject"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/sellers"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(sellerProfileService).reject(1L);
    }

    // ── admin users list loads ─────────────────────────────────────────────
    @Test
    void listUsers_LoadsPage() throws Exception {
        when(userRepository.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/admin/users"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/users"))
                .andExpect(model().attributeExists("users"));
    }

    // ── admin products list loads ──────────────────────────────────────────
    @Test
    void listProducts_LoadsPage() throws Exception {
        when(productService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/admin/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/products"))
                .andExpect(model().attributeExists("products"));
    }

    // ── toggle product active/inactive redirects ───────────────────────────
    @Test
    void toggleProduct_RedirectsWithSuccess() throws Exception {
        Product p = new Product();
        p.setId(1L);
        p.setName("Test Product");
        p.setActive(false);
        when(productService.toggleActive(1L)).thenReturn(p);

        mockMvc.perform(post("/admin/products/1/toggle"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/admin/products"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(productService).toggleActive(1L);
    }

    // ── admin orders list loads ────────────────────────────────────────────
    @Test
    void listOrders_LoadsPage() throws Exception {
        when(orderService.getAllOrders()).thenReturn(List.of());

        mockMvc.perform(get("/admin/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("admin/orders"))
                .andExpect(model().attributeExists("orders"));
    }
}
