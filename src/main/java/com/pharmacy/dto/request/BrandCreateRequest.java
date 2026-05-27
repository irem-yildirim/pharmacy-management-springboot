package com.pharmacy.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class BrandCreateRequest {

    @NotBlank
    private String name;
}
