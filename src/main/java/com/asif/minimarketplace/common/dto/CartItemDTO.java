package com.asif.minimarketplace.common.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class CartItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private String productImageUrl;
    private int quantity;
    private BigDecimal unitPriceSnapshot;
    private BigDecimal subtotal;
}

