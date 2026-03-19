package com.asif.minimarketplace.product.service;

import com.asif.minimarketplace.common.exception.InsufficientStockException;
import com.asif.minimarketplace.product.entity.Product;
import com.asif.minimarketplace.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class InventoryService {

    private final ProductRepository productRepository;
    private final ProductService productService;

    /**
     * Validate that enough stock is available.
     */
    public void validateStock(Long productId, int requestedQty) {
        Product product = productService.findById(productId);
        if (product.getStockQuantity() < requestedQty) {
            throw new InsufficientStockException(
                    product.getName(), requestedQty, product.getStockQuantity());
        }
    }

    /**
     * Decrease stock for a product. Throws if insufficient.
     */
    @Transactional
    public void decreaseStock(Long productId, int qty) {
        Product product = productService.findById(productId);
        if (product.getStockQuantity() < qty) {
            throw new InsufficientStockException(
                    product.getName(), qty, product.getStockQuantity());
        }
        product.setStockQuantity(product.getStockQuantity() - qty);
        productRepository.save(product);
        log.debug("Stock decreased by {} for product {}", qty, product.getName());
    }

    /**
     * Increase stock for a product (e.g., on order cancellation).
     */
    @Transactional
    public void increaseStock(Long productId, int qty) {
        Product product = productService.findById(productId);
        product.setStockQuantity(product.getStockQuantity() + qty);
        productRepository.save(product);
        log.debug("Stock increased by {} for product {}", qty, product.getName());
    }
}

