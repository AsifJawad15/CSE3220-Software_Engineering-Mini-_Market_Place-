package com.asif.minimarketplace.product.controller;

import com.asif.minimarketplace.product.entity.Category;
import com.asif.minimarketplace.product.entity.Product;
import com.asif.minimarketplace.product.repository.CategoryRepository;
import com.asif.minimarketplace.product.repository.ProductRepository;
import com.asif.minimarketplace.seller.entity.SellerProfile;
import com.asif.minimarketplace.seller.repository.SellerProfileRepository;
import com.asif.minimarketplace.user.entity.RoleName;
import com.asif.minimarketplace.user.entity.User;
import com.asif.minimarketplace.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class ProductControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private SellerProfileRepository sellerProfileRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private Product savedProduct;

    @BeforeEach
    void setUp() {
        // Use seeded category from DataInitializer instead of deleting/recreating
        Category cat = categoryRepository.findBySlug("electronics").orElseGet(
                () -> categoryRepository.save(Category.builder().name("TestElectronics").slug("test-electronics").build()));

        User sellerUser = userRepository.findByEmail("prodseller@test.com").orElseGet(
                () -> userRepository.save(User.builder()
                        .fullName("Prod Seller").email("prodseller@test.com")
                        .password(passwordEncoder.encode("password123"))
                        .role(RoleName.SELLER).enabled(true).build()));

        SellerProfile sellerProfile = sellerProfileRepository.findByUserId(sellerUser.getId()).orElseGet(
                () -> sellerProfileRepository.save(
                        SellerProfile.builder().user(sellerUser).shopName("Prod Test Shop").build()));

        savedProduct = productRepository.save(Product.builder()
                .name("Test Laptop")
                .description("A great laptop")
                .price(BigDecimal.valueOf(999.99))
                .stockQuantity(10)
                .active(true)
                .category(cat)
                .seller(sellerProfile)
                .build());
    }

    @Test
    void listProducts_returns200_withProducts() throws Exception {
        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    void productDetail_validId_returns200() throws Exception {
        mockMvc.perform(get("/products/" + savedProduct.getId()))
                .andExpect(status().isOk())
                .andExpect(view().name("products/detail"))
                .andExpect(model().attributeExists("product"));
    }

    @Test
    void productDetail_invalidId_returnsErrorPage() throws Exception {
        mockMvc.perform(get("/products/99999"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"))
                .andExpect(model().attribute("errorCode", 404));
    }

    @Test
    void listProducts_withSearch_filtersResults() throws Exception {
        mockMvc.perform(get("/products").param("search", "Laptop"))
                .andExpect(status().isOk())
                .andExpect(model().attributeExists("products"));
    }
}

