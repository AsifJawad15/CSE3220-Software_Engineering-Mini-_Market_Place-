package com.asif.minimarketplace.product.service;

import com.asif.minimarketplace.common.exception.InsufficientStockException;
import com.asif.minimarketplace.product.entity.Product;
import com.asif.minimarketplace.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductService productService;

    @InjectMocks
    private InventoryService inventoryService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId(10L);
        product.setName("Test Product");
        product.setStockQuantity(50);
    }

    @Test
    void validateStock_PassesWhenEnoughStock() {
        when(productService.findById(10L)).thenReturn(product);
        
        assertDoesNotThrow(() -> inventoryService.validateStock(10L, 20));
    }

    @Test
    void validateStock_FailsWhenInsufficientStock() {
        when(productService.findById(10L)).thenReturn(product);
        
        assertThrows(InsufficientStockException.class, () -> inventoryService.validateStock(10L, 60));
    }

    @Test
    void decreaseStock_Works() {
        when(productService.findById(10L)).thenReturn(product);
        
        inventoryService.decreaseStock(10L, 20);

        assertEquals(30, product.getStockQuantity());
        verify(productRepository).save(product);
    }

    @Test
    void decreaseStock_CannotGoBelowZero() {
        when(productService.findById(10L)).thenReturn(product);
        
        assertThrows(InsufficientStockException.class, () -> inventoryService.decreaseStock(10L, 60));
        
        assertEquals(50, product.getStockQuantity()); // Stock remains unchanged
        verify(productRepository, never()).save(product);
    }
}