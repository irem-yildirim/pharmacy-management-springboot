package com.pharmacy.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CustomerCreateRequest {

    @NotBlank(message = "Customer name must not be blank")
    @Size(max = 100, message = "Customer name must not exceed 100 characters")
    private String name;

    @Pattern(regexp = "^\\d{10}$", message = "Phone number must be exactly 10 digits")
    private String phone;

    @DecimalMin(value = "0.0", inclusive = true, message = "Initial balance must be greater than or equal to 0")
    @Builder.Default
    private BigDecimal balance = BigDecimal.ZERO;
}
