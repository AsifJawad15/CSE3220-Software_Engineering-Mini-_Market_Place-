package com.asif.minimarketplace.product.entity;

import com.asif.minimarketplace.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "tags")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Tag extends BaseEntity {

    @Column(name = "name", nullable = false, unique = true, length = 60)
    private String name;

    @Column(name = "slug", nullable = false, unique = true, length = 80)
    private String slug;

    @ManyToMany(mappedBy = "tags")
    @Builder.Default
    private Set<Product> products = new HashSet<>();
}

