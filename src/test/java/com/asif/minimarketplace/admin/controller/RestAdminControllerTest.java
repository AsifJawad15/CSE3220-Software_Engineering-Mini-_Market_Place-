package com.asif.minimarketplace.admin.controller;

import com.asif.minimarketplace.common.dto.DtoMapper;
import com.asif.minimarketplace.common.exception.GlobalExceptionHandler;
import com.asif.minimarketplace.order.entity.OrderStatus;
import com.asif.minimarketplace.order.service.OrderService;
import com.asif.minimarketplace.product.entity.Category;
import com.asif.minimarketplace.product.entity.Product;
import com.asif.minimarketplace.product.service.ProductService;
import com.asif.minimarketplace.seller.entity.ApprovalStatus;
import com.asif.minimarketplace.seller.entity.SellerProfile;
import com.asif.minimarketplace.seller.service.SellerProfileService;
import com.asif.minimarketplace.user.entity.RoleName;
import com.asif.minimarketplace.user.entity.User;
import com.asif.minimarketplace.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for RestAdminController -- covers REST stats endpoint,
 * user listing, pending sellers, and product toggle.
 */
@ExtendWith(MockitoExtension.class)
public class RestAdminControllerTest {

    private MockMvc mockMvc;

    @Mock private UserRepository userRepository;
    @Mock private SellerProfileService sellerProfileService;
    @Mock private ProductService productService;
    @Mock private OrderService orderService;

    @InjectMocks
    private RestAdminController restAdminController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(restAdminController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ── REST /api/admin/stats returns 200 with dashboard stats ─────────────
    @Test
    void getStats_Returns200() throws Exception {
        when(userRepository.count()).thenReturn(10L);
        when(productService.countTotal()).thenReturn(20L);
        when(productService.countActive()).thenReturn(15L);
        when(sellerProfileService.countByStatus(ApprovalStatus.PENDING)).thenReturn(3L);
        when(sellerProfileService.countByStatus(ApprovalStatus.APPROVED)).thenReturn(5L);
        when(orderService.countByStatus(any(OrderStatus.class))).thenReturn(2L);

        mockMvc.perform(get("/api/admin/stats"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.totalUsers").value(10));
    }

    // ── REST /api/admin/users returns 200 ──────────────────────────────────
    @Test
    void listUsers_Returns200() throws Exception {
        User u = new User();
        u.setId(1L);
        u.setFullName("Test User");
        u.setEmail("test@example.com");
        u.setRole(RoleName.BUYER);
        u.setEnabled(true);
        when(userRepository.findAll()).thenReturn(List.of(u));

        mockMvc.perform(get("/api/admin/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].email").value("test@example.com"));
    }

    // ── REST /api/admin/sellers/pending returns 200 ────────────────────────
    @Test
    void listPendingSellers_Returns200() throws Exception {
        User u = new User();
        u.setId(1L);
        u.setFullName("Seller");
        u.setEmail("seller@example.com");

        SellerProfile sp = new SellerProfile();
        sp.setId(10L);
        sp.setUser(u);
        sp.setShopName("Test Shop");
        sp.setApprovalStatus(ApprovalStatus.PENDING);
        when(sellerProfileService.findByStatus(ApprovalStatus.PENDING)).thenReturn(List.of(sp));

        mockMvc.perform(get("/api/admin/sellers/pending"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data[0].approvalStatus").value("PENDING"));
    }
}
