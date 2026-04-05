package com.asif.minimarketplace.common.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class ProductDTO {
    private Long id;
    private String name;
    private String description;
    private BigDecimal price;
    private int stockQuantity;
    private String imageUrl;
    private boolean active;
    private Long categoryId;
    private String categoryName;
    private Long sellerId;
    private String sellerShopName;
    private List<TagDTO> tags;

    @Data
    @Builder
    public static class TagDTO {
        private Long id;
        private String name;
        private String slug;
    }
}

