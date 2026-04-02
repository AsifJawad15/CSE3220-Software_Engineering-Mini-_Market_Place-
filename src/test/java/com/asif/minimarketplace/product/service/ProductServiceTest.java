package com.asif.minimarketplace.product.service;

import com.asif.minimarketplace.common.exception.AccessDeniedException;
import com.asif.minimarketplace.common.exception.NotFoundException;
import com.asif.minimarketplace.product.dto.ProductRequest;
import com.asif.minimarketplace.product.entity.Category;
import com.asif.minimarketplace.product.entity.Product;
import com.asif.minimarketplace.product.repository.ProductRepository;
import com.asif.minimarketplace.seller.entity.SellerProfile;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private SellerProfile seller;
    private Category category;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        seller = new SellerProfile();
        seller.setId(10L);
        seller.setShopName("My Shop");

        category = new Category();
        category.setId(5L);
        category.setName("Electronics");

        product = new Product();
        product.setId(100L);
        product.setName("Smartphone");
        product.setPrice(new BigDecimal("500.00"));
        product.setSeller(seller);
        product.setCategory(category);
        product.setActive(true);

        productRequest = new ProductRequest();
        productRequest.setName("Smartphone Update");
        productRequest.setPrice(new BigDecimal("550.00"));
        productRequest.setCategoryId(5L);
    }

    @Test
    void findById_ReturnsCorrectProduct() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        Product result = productService.findById(100L);

        assertNotNull(result);
        assertEquals("Smartphone", result.getName());
    }
}