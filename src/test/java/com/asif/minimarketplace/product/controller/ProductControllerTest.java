package com.asif.minimarketplace.product.controller;

import com.asif.minimarketplace.product.entity.Category;
import com.asif.minimarketplace.product.entity.Product;
import com.asif.minimarketplace.product.service.CategoryService;
import com.asif.minimarketplace.product.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private ProductController productController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(productController).build();
    }

    @Test
    void listProducts_ShouldReturnProductsPage() throws Exception {
        Page<Product> productPage = new PageImpl<>(List.of(new Product()));
        when(productService.findActiveProducts(any())).thenReturn(productPage);
        when(categoryService.findAll()).thenReturn(List.of(new Category()));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"))
                .andExpect(model().attributeExists("products"))
                .andExpect(model().attributeExists("categories"));
    }

    @Test
    void listProducts_WithSearchQuery_ShouldReturnSearchedProducts() throws Exception {
        Page<Product> productPage = new PageImpl<>(List.of(new Product()));
        when(productService.searchActive(eq("laptop"), any())).thenReturn(productPage);
        when(categoryService.findAll()).thenReturn(List.of(new Category()));

        mockMvc.perform(get("/products").param("search", "laptop"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"))
                .andExpect(model().attributeExists("search"))
                .andExpect(model().attributeExists("products"));
    }

    @Test
    void listProducts_WithCategoryId_ShouldReturnCategoryProducts() throws Exception {
        Page<Product> productPage = new PageImpl<>(List.of(new Product()));
        when(productService.findActiveByCategory(eq(1L), any())).thenReturn(productPage);
        when(categoryService.findAll()).thenReturn(List.of(new Category()));

        mockMvc.perform(get("/products").param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"))
                .andExpect(model().attributeExists("selectedCategoryId"))
                .andExpect(model().attributeExists("products"));
    }

    @Test
    void productDetail_ShouldReturnProductDetails() throws Exception {
        Product product = new Product();
        when(productService.findById(1L)).thenReturn(product);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/detail"))
                .andExpect(model().attributeExists("product"));
    }
}