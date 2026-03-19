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

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private ProductService productService;

    @InjectMocks private InventoryService inventoryService;

    private Product product;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .name("Widget")
                .price(BigDecimal.TEN)
                .stockQuantity(20)
                .active(true)
                .build();
        product.setId(1L);
    }

    @Test
    void validateStock_sufficient_noException() {
        when(productService.findById(1L)).thenReturn(product);
        assertThatCode(() -> inventoryService.validateStock(1L, 10))
                .doesNotThrowAnyException();
    }

    @Test
    void validateStock_insufficient_throwsException() {
        when(productService.findById(1L)).thenReturn(product);
        assertThatThrownBy(() -> inventoryService.validateStock(1L, 100))
                .isInstanceOf(InsufficientStockException.class);
    }

    @Test
    void decreaseStock_sufficient_updatesQuantity() {
        when(productService.findById(1L)).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        inventoryService.decreaseStock(1L, 5);
        assertThat(product.getStockQuantity()).isEqualTo(15);
        verify(productRepository).save(product);
    }

    @Test
    void decreaseStock_insufficient_throwsException() {
        when(productService.findById(1L)).thenReturn(product);

        assertThatThrownBy(() -> inventoryService.decreaseStock(1L, 25))
                .isInstanceOf(InsufficientStockException.class);
        verify(productRepository, never()).save(any());
    }

    @Test
    void increaseStock_addsQuantity() {
        when(productService.findById(1L)).thenReturn(product);
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        inventoryService.increaseStock(1L, 10);
        assertThat(product.getStockQuantity()).isEqualTo(30);
        verify(productRepository).save(product);
    }
}

