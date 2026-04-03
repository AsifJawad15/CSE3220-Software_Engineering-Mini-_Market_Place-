package com.asif.minimarketplace.common.dto;

import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
public class CartDTO {
    private Long id;
    private List<CartItemDTO> items;
    private BigDecimal total;
    private int itemCount;
}

