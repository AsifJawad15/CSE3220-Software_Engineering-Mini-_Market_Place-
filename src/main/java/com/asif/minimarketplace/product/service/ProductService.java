package com.asif.minimarketplace.product.service;

import com.asif.minimarketplace.common.exception.AccessDeniedException;
import com.asif.minimarketplace.common.exception.NotFoundException;
import com.asif.minimarketplace.product.dto.ProductRequest;
import com.asif.minimarketplace.product.entity.Category;
import com.asif.minimarketplace.product.entity.Product;
import com.asif.minimarketplace.product.repository.ProductRepository;
import com.asif.minimarketplace.seller.entity.SellerProfile;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;

    // ── Public browsing ────────────────────────────────────────────────────

    public Page<Product> findActiveProducts(Pageable pageable) {
        return productRepository.findByActiveTrue(pageable);
    }

    public Page<Product> findActiveByCategory(Long categoryId, Pageable pageable) {
        return productRepository.findByActiveTrueAndCategoryId(categoryId, pageable);
    }

    public Page<Product> searchActive(String query, Pageable pageable) {
        return productRepository.findByActiveTrueAndNameContainingIgnoreCase(query, pageable);
    }

    public Product findById(Long id) {
        return productRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NotFoundException("Product", id));
    }

    // ── Seller product management ──────────────────────────────────────────

    public List<Product> findBySeller(Long sellerId) {
        return productRepository.findBySellerId(sellerId);
    }

    public long countBySeller(Long sellerId) {
        return productRepository.countBySellerId(sellerId);
    }

    @Transactional
    public Product create(ProductRequest request, SellerProfile seller) {
        Category category = categoryService.findById(request.getCategoryId());
        Product product = Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .price(request.getPrice())
                .stockQuantity(request.getStockQuantity())
                .imageUrl(request.getImageUrl())
                .active(request.isActive())
                .category(category)
                .seller(seller)
                .build();
        Product saved = productRepository.save(product);
        log.info("Product created: {} by seller {}", saved.getName(), seller.getShopName());
        return saved;
    }

    @Transactional
    public Product update(Long productId, ProductRequest request, SellerProfile seller) {
        Product product = findById(productId);
        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new AccessDeniedException("You do not own this product");
        }
        Category category = categoryService.findById(request.getCategoryId());
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());
        product.setActive(request.isActive());
        product.setCategory(category);
        return productRepository.save(product);
    }

    @Transactional
    public void delete(Long productId, SellerProfile seller) {
        Product product = findById(productId);
        if (!product.getSeller().getId().equals(seller.getId())) {
            throw new AccessDeniedException("You do not own this product");
        }
        productRepository.delete(product);
        log.info("Product deleted: {} by seller {}", product.getName(), seller.getShopName());
    }

    // ── Admin methods ──────────────────────────────────────────────────────

    public List<Product> findAll() {
        return productRepository.findAllWithDetails();
    }

    public long countActive() {
        return productRepository.countByActiveTrue();
    }

    public long countTotal() {
        return productRepository.count();
    }

    @Transactional
    public Product toggleActive(Long productId) {
        Product product = findById(productId);
        product.setActive(!product.isActive());
        return productRepository.save(product);
    }
}

