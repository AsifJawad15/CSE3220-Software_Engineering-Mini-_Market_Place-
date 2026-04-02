package com.asif.minimarketplace.product.controller;

import com.asif.minimarketplace.common.exception.GlobalExceptionHandler;
import com.asif.minimarketplace.common.exception.NotFoundException;
import com.asif.minimarketplace.product.entity.Category;
import com.asif.minimarketplace.product.entity.Product;
import com.asif.minimarketplace.product.service.CategoryService;
import com.asif.minimarketplace.product.service.ProductService;
import com.asif.minimarketplace.seller.entity.SellerProfile;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.math.BigDecimal;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for RestProductController -- covers public REST product
 * list (/api/products), product detail 404, and categories endpoint.
 */
@ExtendWith(MockitoExtension.class)
public class RestProductControllerTest {

    private MockMvc mockMvc;

    @Mock
    private ProductService productService;
    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private RestProductController restProductController;

    @BeforeEach
    void setUp() {
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.getObjectMapper().disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

        mockMvc = MockMvcBuilders.standaloneSetup(restProductController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setMessageConverters(converter)
                .build();
    }

    // ── public REST /api/products returns 200 ──────────────────────────────
    @Test
    void listProducts_Returns200() throws Exception {
        Product p = new Product();
        p.setId(1L);
        p.setName("Test Product");
        p.setPrice(BigDecimal.TEN);
        Category c = new Category();
        c.setId(1L);
        c.setName("Test Cat");
        p.setCategory(c);
        SellerProfile sp = new SellerProfile();
        sp.setId(1L);
        sp.setShopName("Test Shop");
        p.setSeller(sp);
        when(productService.findActiveProducts(any())).thenReturn(new PageImpl<>(List.of(p), PageRequest.of(0, 12), 1));

        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    // ── public REST /api/products/{id} returns 404 for bad id ──────────────
    @Test
    void getProduct_BadId_Returns404() throws Exception {
        when(productService.findById(999L)).thenThrow(new NotFoundException("Product", 999L));

        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.success").value(false));
    }

    // ── public REST /api/categories returns 200 ────────────────────────────
    @Test
    void listCategories_Returns200() throws Exception {
        Category c = new Category();
        c.setId(1L);
        c.setName("Electronics");
        when(categoryService.findAll()).thenReturn(List.of(c));

        mockMvc.perform(get("/api/categories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }
}