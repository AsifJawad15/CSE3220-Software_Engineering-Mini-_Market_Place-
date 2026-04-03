package com.asif.minimarketplace.product.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 200)
    private String name;

    @Size(max = 5000)
    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.01", message = "Price must be greater than 0")
    private BigDecimal price;

    @Min(value = 0, message = "Stock cannot be negative")
    private int stockQuantity;

    @Size(max = 500)
    private String imageUrl;

    @NotNull(message = "Category is required")
    private Long categoryId;

    private boolean active = true;
}

