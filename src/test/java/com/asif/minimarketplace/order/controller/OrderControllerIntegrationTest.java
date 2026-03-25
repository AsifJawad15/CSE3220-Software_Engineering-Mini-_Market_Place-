package com.asif.minimarketplace.order.controller;

import com.asif.minimarketplace.buyer.entity.BuyerProfile;
import com.asif.minimarketplace.buyer.repository.BuyerProfileRepository;
import com.asif.minimarketplace.cart.entity.Cart;
import com.asif.minimarketplace.cart.entity.CartItem;
import com.asif.minimarketplace.cart.repository.CartRepository;
import com.asif.minimarketplace.order.repository.OrderRepository;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class OrderControllerIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private BuyerProfileRepository buyerProfileRepository;
    @Autowired private SellerProfileRepository sellerProfileRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CartRepository cartRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private PasswordEncoder passwordEncoder;

    private User buyerUser;
    private BuyerProfile buyerProfile;
    private Product product;

    @BeforeEach
    void setUp() {
        // Use a category already seeded by DataInitializer
        Category cat = categoryRepository.findBySlug("electronics").orElseGet(
                () -> categoryRepository.save(Category.builder().name("TestCat").slug("test-cat").build()));

        buyerUser = userRepository.findByEmail("orderbuyer@test.com").orElseGet(
                () -> userRepository.save(User.builder()
                        .fullName("Order Buyer").email("orderbuyer@test.com")
                        .password(passwordEncoder.encode("password123"))
                        .role(RoleName.BUYER).enabled(true).build()));

        buyerProfile = buyerProfileRepository.findByUserId(buyerUser.getId()).orElseGet(
                () -> buyerProfileRepository.save(BuyerProfile.builder().user(buyerUser).build()));

        User sellerUser = userRepository.findByEmail("orderseller@test.com").orElseGet(
                () -> userRepository.save(User.builder()
                        .fullName("Order Seller").email("orderseller@test.com")
                        .password(passwordEncoder.encode("password123"))
                        .role(RoleName.SELLER).enabled(true).build()));

        SellerProfile sellerProfile = sellerProfileRepository.findByUserId(sellerUser.getId()).orElseGet(
                () -> sellerProfileRepository.save(
                        SellerProfile.builder().user(sellerUser).shopName("Order Shop").build()));

        product = productRepository.save(Product.builder()
                .name("Order Test Book")
                .description("A book for order testing")
                .price(BigDecimal.valueOf(15.00))
                .stockQuantity(50)
                .active(true)
                .category(cat)
                .seller(sellerProfile)
                .build());
    }

    @Test
    @WithMockUser(username = "orderbuyer@test.com", roles = "BUYER")
    void checkoutPage_withCartItems_returns200() throws Exception {
        Cart cart = cartRepository.findByBuyerProfileId(buyerProfile.getId()).orElseGet(
                () -> cartRepository.save(Cart.builder().buyerProfile(buyerProfile)
                        .items(new ArrayList<>()).build()));
        CartItem item = CartItem.builder().cart(cart).product(product).quantity(1)
                .unitPriceSnapshot(product.getPrice()).build();
        cart.getItems().add(item);
        cartRepository.save(cart);

        mockMvc.perform(get("/buyer/checkout"))
                .andExpect(status().isOk())
                .andExpect(view().name("buyer/checkout"));
    }

    @Test
    @WithMockUser(username = "orderbuyer@test.com", roles = "BUYER")
    void buyerOrders_returns200() throws Exception {
        mockMvc.perform(get("/buyer/orders"))
                .andExpect(status().isOk())
                .andExpect(view().name("buyer/orders"));
    }

    @Test
    void checkoutPage_unauthenticated_redirectsToLogin() throws Exception {
        mockMvc.perform(get("/buyer/checkout"))
                .andExpect(status().is3xxRedirection());
    }

    @Test
    @WithMockUser(username = "orderbuyer@test.com", roles = "BUYER")
    void postCheckout_withCartItems_redirectsToOrders() throws Exception {
        Cart cart = cartRepository.findByBuyerProfileId(buyerProfile.getId()).orElseGet(
                () -> cartRepository.save(Cart.builder().buyerProfile(buyerProfile)
                        .items(new ArrayList<>()).build()));
        CartItem item = CartItem.builder().cart(cart).product(product).quantity(2)
                .unitPriceSnapshot(product.getPrice()).build();
        cart.getItems().add(item);
        cartRepository.save(cart);

        mockMvc.perform(post("/buyer/checkout").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/buyer/orders"));
    }
}
