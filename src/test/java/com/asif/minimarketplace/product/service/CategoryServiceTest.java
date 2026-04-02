package com.asif.minimarketplace.product.service;

import com.asif.minimarketplace.common.exception.NotFoundException;
import com.asif.minimarketplace.product.entity.Category;
import com.asif.minimarketplace.product.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private CategoryService categoryService;

    private Category category;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Electronics");
        category.setSlug("electronics");
    }

    @Test
    void findAll_ReturnsAllCategories() {
        when(categoryRepository.findAll()).thenReturn(Collections.singletonList(category));

        List<Category> result = categoryService.findAll();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals("Electronics", result.get(0).getName());
    }

    @Test
    void findById_ReturnsCorrectCategory() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(category));

        Category result = categoryService.findById(1L);

        assertNotNull(result);
        assertEquals("Electronics", result.getName());
    }

    @Test
    void findById_ThrowsNotFoundWhenMissing() {
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.findById(1L));
    }

    @Test
    void findBySlug_ReturnsCorrectCategory() {
        when(categoryRepository.findBySlug("electronics")).thenReturn(Optional.of(category));

        Category result = categoryService.findBySlug("electronics");

        assertNotNull(result);
        assertEquals(1L, result.getId());
    }

    @Test
    void findBySlug_ThrowsNotFoundWhenMissing() {
        when(categoryRepository.findBySlug(anyString())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> categoryService.findBySlug("missing"));
    }

    @Test
    void create_ReturnsSavedCategory() {
        when(categoryRepository.save(any(Category.class))).thenReturn(category);

        Category result = categoryService.create("Electronics", "electronics");

        assertNotNull(result);
        assertEquals("Electronics", result.getName());
        assertEquals("electronics", result.getSlug());
        verify(categoryRepository).save(any(Category.class));
    }

    @Test
    void existsByName_ReturnsTrueWhenExists() {
        when(categoryRepository.existsByName("Electronics")).thenReturn(true);

        boolean result = categoryService.existsByName("Electronics");

        assertTrue(result);
    }
}
