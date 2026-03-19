package com.asif.minimarketplace.common.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class UserDTO {
    private Long id;
    private String fullName;
    private String email;
    private String role;
    private boolean enabled;
}

