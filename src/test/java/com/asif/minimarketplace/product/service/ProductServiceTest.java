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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock private ProductRepository productRepository;
    @Mock private CategoryService categoryService;

    @InjectMocks private ProductService productService;

    private Product product;
    private Category category;
    private SellerProfile seller;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Electronics");
        category.setSlug("electronics");

        seller = new SellerProfile();
        seller.setId(10L);
        seller.setShopName("Test Shop");

        product = Product.builder()
                .name("Laptop")
                .description("A test laptop")
                .price(BigDecimal.valueOf(999.99))
                .stockQuantity(50)
                .active(true)
                .category(category)
                .seller(seller)
                .build();
        product.setId(1L);
    }

    @Test
    void findById_exists_returnsProduct() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        Product result = productService.findById(1L);
        assertThat(result.getName()).isEqualTo("Laptop");
    }

    @Test
    void findById_notExists_throwsNotFound() {
        when(productRepository.findById(99L)).thenReturn(Optional.empty());
        assertThatThrownBy(() -> productService.findById(99L))
                .isInstanceOf(NotFoundException.class);
    }

    @Test
    void findActiveProducts_returnsPage() {
        PageRequest pageable = PageRequest.of(0, 10);
        Page<Product> page = new PageImpl<>(List.of(product));
        when(productRepository.findByActiveTrue(pageable)).thenReturn(page);

        Page<Product> result = productService.findActiveProducts(pageable);
        assertThat(result.getContent()).hasSize(1);
    }

    @Test
    void create_success() {
        ProductRequest request = new ProductRequest();
        request.setName("Laptop");
        request.setDescription("A laptop");
        request.setPrice(BigDecimal.valueOf(999.99));
        request.setStockQuantity(50);
        request.setCategoryId(1L);
        request.setActive(true);

        when(categoryService.findById(1L)).thenReturn(category);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.create(request, seller);
        assertThat(result.getName()).isEqualTo("Laptop");
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void update_ownerMatch_success() {
        ProductRequest request = new ProductRequest();
        request.setName("Updated Laptop");
        request.setDescription("Updated");
        request.setPrice(BigDecimal.valueOf(1099.99));
        request.setStockQuantity(40);
        request.setCategoryId(1L);
        request.setActive(true);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(categoryService.findById(1L)).thenReturn(category);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        Product result = productService.update(1L, request, seller);
        assertThat(result).isNotNull();
        verify(productRepository).save(any(Product.class));
    }

    @Test
    void update_notOwner_throwsAccessDenied() {
        SellerProfile otherSeller = new SellerProfile();
        otherSeller.setId(99L);

        ProductRequest request = new ProductRequest();
        request.setName("Hacked");
        request.setCategoryId(1L);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));

        assertThatThrownBy(() -> productService.update(1L, request, otherSeller))
                .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void delete_ownerMatch_success() {
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        productService.delete(1L, seller);
        verify(productRepository).delete(product);
    }

    @Test
    void toggleActive_flipsState() {
        product.setActive(true);
        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenAnswer(inv -> inv.getArgument(0));

        Product result = productService.toggleActive(1L);
        assertThat(result.isActive()).isFalse();
    }
}

