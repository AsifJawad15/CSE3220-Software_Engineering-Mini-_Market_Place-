package com.asif.minimarketplace.common.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

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
}

