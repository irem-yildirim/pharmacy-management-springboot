package com.pharmacy.controller.api;

import com.pharmacy.model.Purchase;
import com.pharmacy.service.PurchaseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@RestController
@RequestMapping("/api/purchases")
@RequiredArgsConstructor
@Tag(name = "Purchases", description = "Stock batch procurement and inventory intake")
public class PurchaseController {

    private final PurchaseService purchaseService;

    @PostMapping
    @Operation(summary = "Create a purchase batch", description = "Records a new stock intake batch for an existing drug")
    public ResponseEntity<Map<String, Object>> createPurchase(@Valid @RequestBody PurchaseBatchRequest request) {
        Purchase purchase = purchaseService.createPurchase(
                request.getDrugBarcode(),
                request.getQuantity(),
                request.getPurchasePrice(),
                request.getExpirationDate());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(Map.of("message", "Purchase batch created", "drugBarcode", purchase.getDrug().getBarcode()));
    }

    @Data
    public static class PurchaseBatchRequest {
        private String drugBarcode;
        private Integer quantity;
        private BigDecimal purchasePrice;
        private LocalDate expirationDate;
    }
}
