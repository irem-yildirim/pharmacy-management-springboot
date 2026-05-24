package com.pharmacy.controller;

import com.pharmacy.dto.request.SaleCreateRequest;
import com.pharmacy.dto.response.SaleResponse;
import com.pharmacy.entity.Sale;
import com.pharmacy.entity.User;
import com.pharmacy.repository.UserRepository;
import com.pharmacy.service.SaleService;
import jakarta.servlet.http.HttpSession;
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
    private final UserRepository userRepository;

    @PostMapping
    public ResponseEntity<SaleResponse> createSale(@Valid @RequestBody SaleCreateRequest request, HttpSession session) {
        User user = (User) session.getAttribute("user");
        if (user == null) {
            // Default to Admin with ID 1 if no session is active (e.g. testing)
            user = userRepository.findById(1L).orElse(null);
        }
        Sale sale = saleService.createSale(
                request.getItems(), 
                request.isPrescriptionLogged(), 
                request.getCustomerId(), 
                user
        );
        return ResponseEntity.status(HttpStatus.CREATED).body(SaleResponse.fromEntity(sale));
    }
}
