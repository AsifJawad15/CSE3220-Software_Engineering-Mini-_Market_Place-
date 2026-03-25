package com.asif.minimarketplace.common.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;

@Data
@Builder
public class OrderItemDTO {
    private Long id;
    private Long productId;
    private String productName;
    private Long sellerProfileId;
    private String sellerShopName;
    private int quantity;
    private BigDecimal priceAtPurchase;
    private BigDecimal subtotal;
}

