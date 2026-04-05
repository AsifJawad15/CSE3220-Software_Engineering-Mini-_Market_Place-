package com.asif.minimarketplace.seller.controller;

import com.asif.minimarketplace.product.dto.ProductRequest;
import com.asif.minimarketplace.product.entity.Category;
import com.asif.minimarketplace.product.entity.Product;
import com.asif.minimarketplace.product.service.CategoryService;
import com.asif.minimarketplace.product.service.ProductService;
import com.asif.minimarketplace.product.service.TagService;
import com.asif.minimarketplace.seller.dto.UpdateSellerProfileRequest;
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

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for SellerController -- covers dashboard, profile,
 * product CRUD, approval gating, and cross-seller edit blocking.
 */
@ExtendWith(MockitoExtension.class)
public class SellerControllerTest {

    private MockMvc mockMvc;

    @Mock private SellerProfileService sellerProfileService;
    @Mock private ProductService productService;
    @Mock private CategoryService categoryService;
    @Mock private TagService tagService;
    @Mock private UserRepository userRepository;
    @Mock private UserDetails userDetails;

    @InjectMocks
    private SellerController sellerController;

    private User testUser;
    private SellerProfile sellerProfile;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(sellerController)
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
        testUser.setEmail("seller@example.com");

        sellerProfile = new SellerProfile();
        sellerProfile.setId(10L);
        sellerProfile.setUser(testUser);
        sellerProfile.setShopName("Test Shop");
        sellerProfile.setApprovalStatus(ApprovalStatus.APPROVED);
    }

    private void mockAuth() {
        when(userDetails.getUsername()).thenReturn("seller@example.com");
        when(userRepository.findByEmail("seller@example.com")).thenReturn(Optional.of(testUser));
    }

    // ── seller dashboard loads with product count ──────────────────────────
    @Test
    void dashboard_LoadsWithProductCount() throws Exception {
        mockAuth();
        when(sellerProfileService.getProfileByUserId(1L)).thenReturn(sellerProfile);
        when(productService.countBySeller(10L)).thenReturn(5L);

        mockMvc.perform(get("/seller/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("seller/dashboard"))
                .andExpect(model().attributeExists("user", "profile", "productCount"));
    }

    // ── seller profile page loads ──────────────────────────────────────────
    @Test
    void profilePage_LoadsWithProfileData() throws Exception {
        mockAuth();
        when(sellerProfileService.getProfileByUserId(1L)).thenReturn(sellerProfile);

        mockMvc.perform(get("/seller/profile"))
                .andExpect(status().isOk())
                .andExpect(view().name("seller/profile"))
                .andExpect(model().attributeExists("user", "profile", "updateRequest"));
    }

    // ── valid profile update redirects with success ────────────────────────
    @Test
    void updateProfile_Valid_RedirectsWithSuccess() throws Exception {
        mockAuth();

        mockMvc.perform(post("/seller/profile")
                        .param("shopName", "New Shop")
                        .param("phone", "1234567890"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/seller/profile"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(sellerProfileService).updateProfile(eq(1L), any(UpdateSellerProfileRequest.class));
    }

    // ── invalid profile update returns same page ───────────────────────────
    @Test
    void updateProfile_Invalid_ReturnsSamePage() throws Exception {
        mockAuth();
        when(sellerProfileService.getProfileByUserId(1L)).thenReturn(sellerProfile);

        // shopName > 100 chars triggers @Size validation
        String longName = "A".repeat(101);
        mockMvc.perform(post("/seller/profile")
                        .param("shopName", longName))
                .andExpect(status().isOk())
                .andExpect(view().name("seller/profile"))
                .andExpect(model().attributeExists("user", "profile"));
    }

    // ── seller product list loads ──────────────────────────────────────────
    @Test
    void listProducts_LoadsPage() throws Exception {
        mockAuth();
        when(sellerProfileService.getProfileByUserId(1L)).thenReturn(sellerProfile);
        when(productService.findBySeller(10L)).thenReturn(List.of());

        mockMvc.perform(get("/seller/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("seller/products"))
                .andExpect(model().attributeExists("user", "profile", "products"));
    }

    // ── approved seller can see create form ────────────────────────────────
    @Test
    void showCreateForm_ApprovedSeller_ShowsForm() throws Exception {
        mockAuth();
        when(sellerProfileService.getProfileByUserId(1L)).thenReturn(sellerProfile);
        when(categoryService.findAll()).thenReturn(List.of());
        when(tagService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/seller/products/new"))
                .andExpect(status().isOk())
                .andExpect(view().name("seller/product-form"))
                .andExpect(model().attribute("editMode", false))
                .andExpect(model().attributeExists("productRequest", "categories", "allTags"));
    }

    // ── pending seller is redirected from create form ──────────────────────
    @Test
    void showCreateForm_PendingSeller_RedirectsToDashboard() throws Exception {
        mockAuth();
        sellerProfile.setApprovalStatus(ApprovalStatus.PENDING);
        when(sellerProfileService.getProfileByUserId(1L)).thenReturn(sellerProfile);

        mockMvc.perform(get("/seller/products/new"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/seller/dashboard"));
    }

    // ── valid product creation redirects ───────────────────────────────────
    @Test
    void createProduct_Valid_RedirectsToProducts() throws Exception {
        mockAuth();
        when(sellerProfileService.getProfileByUserId(1L)).thenReturn(sellerProfile);

        mockMvc.perform(post("/seller/products/new")
                        .param("name", "New Product")
                        .param("price", "29.99")
                        .param("stockQuantity", "10")
                        .param("categoryId", "1"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/seller/products"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(productService).create(any(ProductRequest.class), eq(sellerProfile));
    }

    // ── seller can edit own product ────────────────────────────────────────
    @Test
    void showEditForm_OwnProduct_ShowsForm() throws Exception {
        mockAuth();
        when(sellerProfileService.getProfileByUserId(1L)).thenReturn(sellerProfile);

        Product product = new Product();
        product.setId(5L);
        product.setName("My Product");
        product.setPrice(BigDecimal.TEN);
        product.setSeller(sellerProfile);
        product.setTags(new java.util.HashSet<>());
        Category cat = new Category();
        cat.setId(1L);
        product.setCategory(cat);

        when(productService.findById(5L)).thenReturn(product);
        when(categoryService.findAll()).thenReturn(List.of(cat));
        when(tagService.findAll()).thenReturn(List.of());

        mockMvc.perform(get("/seller/products/5/edit"))
                .andExpect(status().isOk())
                .andExpect(view().name("seller/product-form"))
                .andExpect(model().attribute("editMode", true))
                .andExpect(model().attribute("productId", 5L));
    }

    // ── seller cannot edit another seller's product ────────────────────────
    @Test
    void showEditForm_OtherSellersProduct_RedirectsToProducts() throws Exception {
        mockAuth();
        when(sellerProfileService.getProfileByUserId(1L)).thenReturn(sellerProfile);

        SellerProfile otherSeller = new SellerProfile();
        otherSeller.setId(99L);

        Product product = new Product();
        product.setId(5L);
        product.setSeller(otherSeller);

        when(productService.findById(5L)).thenReturn(product);

        mockMvc.perform(get("/seller/products/5/edit"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/seller/products"));
    }

    // ── delete product redirects with success ──────────────────────────────
    @Test
    void deleteProduct_RedirectsWithSuccess() throws Exception {
        mockAuth();
        when(sellerProfileService.getProfileByUserId(1L)).thenReturn(sellerProfile);

        mockMvc.perform(post("/seller/products/5/delete"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/seller/products"))
                .andExpect(flash().attributeExists("successMessage"));

        verify(productService).delete(5L, sellerProfile);
    }
}
