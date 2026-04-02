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

    @Test
    void findById_ThrowsNotFoundWhenMissing() {
        when(productRepository.findById(100L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> productService.findById(100L));
    }

    @Test
    void create_ThrowsNotFoundForInvalidCategory() {
        when(categoryService.findById(5L)).thenThrow(new NotFoundException("Category not found"));

        assertThrows(NotFoundException.class, () -> productService.create(productRequest, seller));
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void searchActive_ByNameWorks() {
        org.springframework.data.domain.Page<Product> page = new org.springframework.data.domain.PageImpl<>(java.util.Collections.singletonList(product));
        when(productRepository.findByActiveTrueAndNameContainingIgnoreCase(eq("Smart"), any())).thenReturn(page);

        org.springframework.data.domain.Page<Product> result = productService.searchActive("Smart", org.springframework.data.domain.PageRequest.of(0, 10));

        assertFalse(result.isEmpty());
        assertTrue(result.getContent().get(0).getName().contains("Smart"));
    }

    @Test
    void update_ThrowsAccessDeniedForOtherSeller() {
        SellerProfile otherSeller = new SellerProfile();
        otherSeller.setId(99L);
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        assertThrows(AccessDeniedException.class, () -> productService.update(100L, productRequest, otherSeller));
    }

    @Test
    void delete_DeletesOwnProduct() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        productService.delete(100L, seller);

        verify(productRepository).delete(product);
    }

    @Test
    void delete_ThrowsAccessDeniedForOtherSeller() {
        SellerProfile otherSeller = new SellerProfile();
        otherSeller.setId(99L);
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));

        assertThrows(AccessDeniedException.class, () -> productService.delete(100L, otherSeller));
    }

    @Test
    void update_UpdatesAndReturnsProduct() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(categoryService.findById(5L)).thenReturn(category);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.update(100L, productRequest, seller);

        assertNotNull(result);
        assertEquals("Smartphone Update", product.getName());
        assertEquals(new BigDecimal("550.00"), product.getPrice());
        verify(productRepository).save(product);
    }

    @Test
    void findActiveProducts_ReturnsActiveItems() {
        org.springframework.data.domain.Page<Product> page = new org.springframework.data.domain.PageImpl<>(java.util.Collections.singletonList(product));
        when(productRepository.findByActiveTrue(any(org.springframework.data.domain.Pageable.class))).thenReturn(page);

        org.springframework.data.domain.Page<Product> result = productService.findActiveProducts(org.springframework.data.domain.PageRequest.of(0, 10));

        assertFalse(result.isEmpty());
        assertTrue(result.getContent().get(0).isActive());
    }

    @Test
    void findActiveByCategory_Works() {
        org.springframework.data.domain.Page<Product> page = new org.springframework.data.domain.PageImpl<>(java.util.Collections.singletonList(product));
        when(productRepository.findByActiveTrueAndCategoryId(eq(5L), any())).thenReturn(page);

        org.springframework.data.domain.Page<Product> result = productService.findActiveByCategory(5L, org.springframework.data.domain.PageRequest.of(0, 10));

        assertFalse(result.isEmpty());
        assertEquals(5L, result.getContent().get(0).getCategory().getId());
    }

    @Test
    void toggleActive_TogglesActiveStatus() {
        when(productRepository.findById(100L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(i -> i.getArgument(0));

        boolean initialStatus = product.isActive();
        Product toggled = productService.toggleActive(100L);

        assertNotEquals(initialStatus, toggled.isActive());
        verify(productRepository).save(product);
    }

    @Test
    void countBySeller_Works() {
        when(productRepository.countBySellerId(10L)).thenReturn(5L);

        long count = productService.countBySeller(10L);

        assertEquals(5L, count);
    }

    @Test
    void countActive_Works() {
        when(productRepository.countByActiveTrue()).thenReturn(15L);

        long count = productService.countActive();

        assertEquals(15L, count);
    }

    @Test
    void countTotal_Works() {
        when(productRepository.count()).thenReturn(30L);

        long count = productService.countTotal();

        assertEquals(30L, count);
    }
}