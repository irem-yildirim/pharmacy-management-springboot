package com.pharmacy.controller.api;

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

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
@Tag(name = "Sales", description = "POS transaction processing with FIFO inventory deduction")
public class SaleController {

    private final SaleService saleService;

    @GetMapping
    @Operation(summary = "List sales", description = "Returns all sales or filter by customerId")
    public ResponseEntity<List<SaleResponse>> getAllSales(@RequestParam(required = false) Long customerId) {
        List<Sale> sales = customerId != null
                ? saleService.findByCustomerId(customerId)
                : saleService.findAll();
        List<SaleResponse> responses = sales.stream()
                .map(SaleResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

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
