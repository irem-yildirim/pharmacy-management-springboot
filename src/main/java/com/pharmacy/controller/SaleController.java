package com.pharmacy.controller;

import com.pharmacy.dto.request.SaleCreateRequest;
import com.pharmacy.dto.response.SaleResponse;
import com.pharmacy.entity.Sale;
import com.pharmacy.service.SaleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/sales")
@RequiredArgsConstructor
public class SaleController {

    private final SaleService saleService;

    @PostMapping
    public ResponseEntity<SaleResponse> createSale(@Valid @RequestBody SaleCreateRequest request) {
        Sale sale = saleService.createSale(request.getItems(), request.isPrescriptionLogged());
        return ResponseEntity.status(HttpStatus.CREATED).body(SaleResponse.fromEntity(sale));
    }
}
