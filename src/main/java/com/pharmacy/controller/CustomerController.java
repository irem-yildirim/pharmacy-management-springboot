package com.pharmacy.controller;

import com.pharmacy.entity.Customer;
import com.pharmacy.repository.CustomerRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
@RequiredArgsConstructor
@Tag(name = "Customers", description = "Patient and customer account management")
public class CustomerController {

    private final CustomerRepository customerRepository;

    @GetMapping
    @Operation(summary = "List active customers", description = "Returns all non-deleted customer accounts with current balances")
    public ResponseEntity<List<Customer>> getAllActive() {
        return ResponseEntity.ok(customerRepository.findByIsActiveTrue());
    }

    @PostMapping
    public ResponseEntity<Customer> createCustomer(@Valid @RequestBody Customer customer) {
        customer.setIsActive(true);
        Customer saved = customerRepository.save(customer);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }
}
