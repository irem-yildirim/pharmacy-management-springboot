package com.pharmacy.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;

@Data
public class PurchaseBatchRequest {

    @NotBlank
    private String drugBarcode;

    @NotNull
    @Min(1)
    private Integer quantity;

    @NotNull
    private BigDecimal purchasePrice;

    @NotNull
    private LocalDate expirationDate;
}
