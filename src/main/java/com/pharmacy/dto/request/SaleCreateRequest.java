package com.pharmacy.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SaleCreateRequest {

    @NotEmpty(message = "Sale items list must not be empty")
    @Valid
    private List<SaleItemRequest> items;

    private boolean prescriptionLogged;
}
