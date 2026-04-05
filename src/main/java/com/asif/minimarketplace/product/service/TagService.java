package com.asif.minimarketplace.product.service;

import com.asif.minimarketplace.common.exception.NotFoundException;
import com.asif.minimarketplace.product.entity.Tag;
import com.asif.minimarketplace.product.repository.TagRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;

    public List<Tag> findAll() {
        return tagRepository.findAll();
    }

    public Tag findById(Long id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Tag", id));
    }

    public Set<Tag> findByIds(Set<Long> ids) {
        if (ids == null || ids.isEmpty()) {
            return new HashSet<>();
        }
        return tagRepository.findByIdIn(ids);
    }

    public Tag create(String name, String slug) {
        Tag tag = Tag.builder().name(name).slug(slug).build();
        return tagRepository.save(tag);
    }

    public boolean existsByName(String name) {
        return tagRepository.existsByName(name);
    }
}

