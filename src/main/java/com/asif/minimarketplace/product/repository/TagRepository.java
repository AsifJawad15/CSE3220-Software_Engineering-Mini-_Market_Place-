package com.asif.minimarketplace.product.repository;

import com.asif.minimarketplace.product.entity.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.Set;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {
    Optional<Tag> findBySlug(String slug);
    boolean existsByName(String name);
    Set<Tag> findByIdIn(Set<Long> ids);
}

