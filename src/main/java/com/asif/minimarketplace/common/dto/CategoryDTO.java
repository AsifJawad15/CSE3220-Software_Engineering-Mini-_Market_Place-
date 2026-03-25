package com.asif.minimarketplace.common.dto;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategoryDTO {
    private Long id;
    private String name;
    private String slug;
}

