package com.pharmacy.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrugCreateRequest {

    @NotBlank(message = "Barcode must not be blank")
    @Pattern(regexp = "^\\d{13}$", message = "Barcode must be exactly 13 digits")
    private String barcode;

    @NotBlank(message = "Name must not be blank")
    @Size(max = 200, message = "Name must not exceed 200 characters")
    private String name;

    @NotNull(message = "Category ID must not be null")
    private Long categoryId;

    @NotNull(message = "Brand ID must not be null")
    private Long brandId;

    private Long presTypeId;

    @NotNull(message = "Current selling price must not be null")
    @DecimalMin(value = "0.0", inclusive = true, message = "Current selling price must be greater than or equal to 0")
    private BigDecimal currentSellingPrice;

    @Min(value = 0, message = "Min stock alert must be greater than or equal to 0")
    @Builder.Default
    private Integer minStockAlert = 10;
}
