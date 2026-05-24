package com.pharmacy.rest;

import com.pharmacy.dto.request.CustomerCreateRequest;
import com.pharmacy.dto.response.CustomerResponse;
import com.pharmacy.model.Customer;
import com.pharmacy.service.CustomerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Patient and customer account management")
public class CustomerController {

    private final CustomerService customerService;

    @GetMapping
    @Operation(summary = "List active customers", description = "Returns all non-deleted customer accounts with current balances")
    public ResponseEntity<List<CustomerResponse>> getAllActive() {
        List<CustomerResponse> responses = customerService.findAllActive().stream()
                .map(CustomerResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    @Operation(summary = "Create a new customer", description = "Adds a new customer account with optional initial balance")
    public ResponseEntity<CustomerResponse> createCustomer(@Valid @RequestBody CustomerCreateRequest request) {
        Customer saved = customerService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(CustomerResponse.fromEntity(saved));
    }
}
