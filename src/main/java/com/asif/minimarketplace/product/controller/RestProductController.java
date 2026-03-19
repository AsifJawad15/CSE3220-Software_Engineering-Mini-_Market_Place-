package com.asif.minimarketplace.product.controller;

import com.asif.minimarketplace.common.dto.ApiResponse;
import com.asif.minimarketplace.common.dto.CategoryDTO;
import com.asif.minimarketplace.common.dto.DtoMapper;
import com.asif.minimarketplace.common.dto.ProductDTO;
import com.asif.minimarketplace.product.service.CategoryService;
import com.asif.minimarketplace.product.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Public REST API for product browsing and category listing.
 * All endpoints are publicly accessible (see SecurityConfig).
 */
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
public class RestProductController {

    private final ProductService productService;
    private final CategoryService categoryService;

    // ── Categories ─────────────────────────────────────────────────────────

    /**
     * GET /api/categories
     * Returns all categories.
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<List<CategoryDTO>>> listCategories() {
        List<CategoryDTO> categories = categoryService.findAll().stream()
                .map(DtoMapper::toCategory)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(categories));
    }

    /**
     * GET /api/categories/{id}
     * Returns a single category by ID.
     */
    @GetMapping("/categories/{id}")
    public ResponseEntity<ApiResponse<CategoryDTO>> getCategory(@PathVariable Long id) {
        CategoryDTO dto = DtoMapper.toCategory(categoryService.findById(id));
        return ResponseEntity.ok(ApiResponse.success(dto));
    }

    // ── Products ───────────────────────────────────────────────────────────

    /**
     * GET /api/products?page=0&size=12&sort=createdAt,desc&q=keyword&categoryId=1
     * Paginated, searchable, filterable public product list (active only).
     */
    @GetMapping("/products")
    public ResponseEntity<ApiResponse<Page<ProductDTO>>> listProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "12") int size,
            @RequestParam(required = false) String q,
            @RequestParam(required = false) Long categoryId) {

        PageRequest pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<ProductDTO> result;
        if (q != null && !q.isBlank()) {
            result = productService.searchActive(q, pageable).map(DtoMapper::toProduct);
        } else if (categoryId != null) {
            result = productService.findActiveByCategory(categoryId, pageable).map(DtoMapper::toProduct);
        } else {
            result = productService.findActiveProducts(pageable).map(DtoMapper::toProduct);
        }
        return ResponseEntity.ok(ApiResponse.success(result));
    }

    /**
     * GET /api/products/{id}
     * Returns product detail by ID.
     */
    @GetMapping("/products/{id}")
    public ResponseEntity<ApiResponse<ProductDTO>> getProduct(@PathVariable Long id) {
        ProductDTO dto = DtoMapper.toProduct(productService.findById(id));
        return ResponseEntity.ok(ApiResponse.success(dto));
    }
}

