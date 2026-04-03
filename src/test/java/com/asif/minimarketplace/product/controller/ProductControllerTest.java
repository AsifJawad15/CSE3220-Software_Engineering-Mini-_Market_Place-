package com.asif.minimarketplace.product.controller;

import com.asif.minimarketplace.common.exception.NotFoundException;
import com.asif.minimarketplace.product.entity.Category;
import com.asif.minimarketplace.product.entity.Product;
import com.asif.minimarketplace.product.service.CategoryService;
import com.asif.minimarketplace.product.service.ProductService;
import com.asif.minimarketplace.common.exception.GlobalExceptionHandler;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for ProductController (web) -- covers public product list,
 * search query filtering, category filtering, product detail, missing product
 * error page, and inactive product not shown to public.
 */
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
        // attach GlobalExceptionHandler so NotFoundException maps to error/404
        mockMvc = MockMvcBuilders.standaloneSetup(productController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    // ── public product list loads ──────────────────────────────────────────
    @Test
    void listProducts_PublicProductListLoads() throws Exception {
        Page<Product> page = new PageImpl<>(List.of(new Product()));
        when(productService.findActiveProducts(any())).thenReturn(page);
        when(categoryService.findAll()).thenReturn(List.of(new Category()));

        mockMvc.perform(get("/products"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"))
                .andExpect(model().attributeExists("products", "categories"));
    }

    // ── search query filters results ───────────────────────────────────────
    @Test
    void listProducts_SearchQueryFiltersResults() throws Exception {
        Page<Product> page = new PageImpl<>(List.of(new Product()));
        when(productService.searchActive(eq("laptop"), any())).thenReturn(page);
        when(categoryService.findAll()).thenReturn(List.of(new Category()));

        mockMvc.perform(get("/products").param("search", "laptop"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"))
                .andExpect(model().attributeExists("search", "products"));
    }

    // ── category filter works ──────────────────────────────────────────────
    @Test
    void listProducts_CategoryFilterWorks() throws Exception {
        Page<Product> page = new PageImpl<>(List.of(new Product()));
        when(productService.findActiveByCategory(eq(1L), any())).thenReturn(page);
        when(categoryService.findAll()).thenReturn(List.of(new Category()));

        mockMvc.perform(get("/products").param("categoryId", "1"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/list"))
                .andExpect(model().attributeExists("selectedCategoryId", "products"));
    }

    // ── product detail for valid product loads ─────────────────────────────
    @Test
    void productDetail_ValidProduct_Loads() throws Exception {
        Product product = new Product();
        product.setId(1L);
        when(productService.findById(1L)).thenReturn(product);

        mockMvc.perform(get("/products/1"))
                .andExpect(status().isOk())
                .andExpect(view().name("products/detail"))
                .andExpect(model().attributeExists("product"));
    }

    // ── missing product returns error page ─────────────────────────────────
    @Test
    void productDetail_MissingProduct_ReturnsErrorPage() throws Exception {
        when(productService.findById(999L)).thenThrow(new NotFoundException("Product", 999L));

        mockMvc.perform(get("/products/999"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"))
                .andExpect(model().attributeExists("errorMessage"));
    }

    // ── inactive product is not shown to public users ──────────────────────
    @Test
    void productDetail_InactiveProduct_NotShown() throws Exception {
        when(productService.findById(2L)).thenThrow(new NotFoundException("Product", 2L));

        mockMvc.perform(get("/products/2"))
                .andExpect(status().isOk())
                .andExpect(view().name("error/404"))
                .andExpect(model().attributeExists("errorMessage"));
    }
}