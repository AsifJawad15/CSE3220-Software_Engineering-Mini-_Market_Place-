package com.asif.minimarketplace.buyer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateBuyerProfileRequest {

    @Size(max = 20, message = "Phone number must be at most 20 characters")
    private String phone;
}


