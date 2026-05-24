package com.pharmacy.rest;

import com.pharmacy.config.PharmacyUserDetails;
import com.pharmacy.dto.request.SaleCreateRequest;
import com.pharmacy.dto.response.SaleResponse;
import com.pharmacy.model.Sale;
import com.pharmacy.service.SaleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@Tag(name = "Sales", description = "POS transaction processing with FIFO inventory deduction")
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    @Operation(summary = "Create a new sale", description = "Processes a POS sale with FIFO batch deduction, prescription validation, and customer balance update")
    public ResponseEntity<SaleResponse> createSale(@Valid @RequestBody SaleCreateRequest request,
                                                    Authentication authentication) {
        PharmacyUserDetails principal = (PharmacyUserDetails) authentication.getPrincipal();
        Sale sale = saleService.createSale(
                request.getItems(),
                request.isPrescriptionLogged(),
                request.getCustomerId(),
                principal.getUserId()
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(SaleResponse.fromEntity(sale));
    }
}
