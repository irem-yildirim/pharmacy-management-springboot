package com.pharmacy.service;

import com.pharmacy.advice.CustomerNotFoundException;
import com.pharmacy.dto.request.CustomerCreateRequest;
import com.pharmacy.model.Customer;
import com.pharmacy.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomerService {

    private final CustomerRepository customerRepository;

    public List<Customer> findAllActive() {
        return customerRepository.findByIsActiveTrue();
    }

    public Customer findById(Long id) {
        return customerRepository.findById(id)
                .orElseThrow(() -> new CustomerNotFoundException("Customer not found with id: " + id));
    }

    @Transactional
    public Customer create(CustomerCreateRequest request) {
        Customer customer = Customer.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .balance(request.getBalance())
                .isActive(true)
                .build();
        return customerRepository.save(customer);
    }
}
