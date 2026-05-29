package com.pharmacy.service;

import com.pharmacy.dto.request.UserCreateRequest;
import com.pharmacy.model.Role;
import com.pharmacy.model.Sale;
import com.pharmacy.model.User;
import com.pharmacy.advice.DuplicateEntryException;
import com.pharmacy.repository.SaleRepository;
import com.pharmacy.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@SuppressWarnings("null")
public class UserService {

    private final UserRepository userRepository;
    private final SaleRepository saleRepository;
    private final PasswordEncoder passwordEncoder;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    public boolean usernameExists(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    @Transactional
    public User createUser(UserCreateRequest request) {
        if (usernameExists(request.getUsername())) {
            throw new DuplicateEntryException("Username " + request.getUsername() + " is already registered.");
        }
        User user = User.builder()
                .name(request.getName())
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.valueOf(request.getRole()))
                .isActive(true)
                .build();
        return userRepository.save(user);
    }

    public Map<String, Object> getUserPerformance(Long userId) {
        List<Sale> sales = saleRepository.findByUser_IdOrderBySaleDateDesc(userId);

        BigDecimal totalRevenue = sales.stream()
                .map(Sale::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        List<Map<String, Object>> recentSales = sales.stream()
                .map(sale -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", sale.getId());
                    map.put("saleDate", sale.getSaleDate().toString());
                    map.put("totalAmount", sale.getTotalAmount());
                    map.put("customerName", sale.getCustomer() != null ? sale.getCustomer().getName() : "Cash Sale");
                    map.put("prescriptionLogged", sale.getIsPrescriptionLogged());
                    return map;
                })
                .collect(Collectors.toList());

        Map<String, Object> response = new HashMap<>();
        response.put("totalRevenue", totalRevenue);
        response.put("salesCount", sales.size());
        response.put("sales", recentSales);

        return response;
    }
}
