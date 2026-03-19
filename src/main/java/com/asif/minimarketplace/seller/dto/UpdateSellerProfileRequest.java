package com.asif.minimarketplace.seller.dto;

import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateSellerProfileRequest {

    @Size(max = 100, message = "Shop name must be at most 100 characters")
    private String shopName;

    @Size(max = 20, message = "Phone number must be at most 20 characters")
    private String phone;
}
