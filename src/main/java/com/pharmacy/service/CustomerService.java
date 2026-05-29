package com.pharmacy.service;

import com.pharmacy.advice.CustomerNotFoundException;
import com.pharmacy.advice.DuplicateEntryException;
import com.pharmacy.dto.request.CustomerCreateRequest;
import com.pharmacy.model.Customer;
import com.pharmacy.repository.CustomerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
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
        if (request.getPhone() != null && customerRepository.existsByPhone(request.getPhone())) {
            throw new DuplicateEntryException("Customer with phone number " + request.getPhone() + " already exists.");
        }
        Customer customer = Customer.builder()
                .name(request.getName())
                .phone(request.getPhone())
                .balance(request.getBalance())
                .isActive(true)
                .build();
        return customerRepository.save(customer);
    }

    public List<Customer> search(String query) {
        return customerRepository
                .findByIsActiveTrueAndNameContainingIgnoreCaseOrIsActiveTrueAndPhoneContaining(query, query);
    }
}
