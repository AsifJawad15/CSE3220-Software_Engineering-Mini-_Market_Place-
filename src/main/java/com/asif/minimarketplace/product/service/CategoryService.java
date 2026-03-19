package com.asif.minimarketplace.product.service;

import com.asif.minimarketplace.common.exception.NotFoundException;
import com.asif.minimarketplace.product.entity.Category;
import com.asif.minimarketplace.product.repository.CategoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public List<Category> findAll() {
        return categoryRepository.findAll();
    }

    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Category", id));
    }

    public Category findBySlug(String slug) {
        return categoryRepository.findBySlug(slug)
                .orElseThrow(() -> new NotFoundException("Category", "slug", slug));
    }

    public Category create(String name, String slug) {
        Category category = Category.builder().name(name).slug(slug).build();
        return categoryRepository.save(category);
    }

    public boolean existsByName(String name) {
        return categoryRepository.existsByName(name);
    }
}

