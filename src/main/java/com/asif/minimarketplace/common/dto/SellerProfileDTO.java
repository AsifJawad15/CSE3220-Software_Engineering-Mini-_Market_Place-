package com.asif.minimarketplace.common.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class SellerProfileDTO {
    private Long id;
    private Long userId;
    private String fullName;
    private String email;
    private String shopName;
    private String phone;
    private String approvalStatus;
}

