package com.asif.minimarketplace.buyer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddressRequest {

    @Size(max = 50, message = "Label must be at most 50 characters")
    private String label;

    @NotBlank(message = "Address line 1 is required")
    @Size(max = 200)
    private String line1;

    @NotBlank(message = "City is required")
    @Size(max = 100)
    private String city;

    @Size(max = 20)
    private String postal;

    @NotBlank(message = "Country is required")
    @Size(max = 100)
    private String country;

    @Size(max = 20)
    private String phone;

    private boolean makeDefault;
}
