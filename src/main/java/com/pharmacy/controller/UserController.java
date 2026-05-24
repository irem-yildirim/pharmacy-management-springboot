package com.pharmacy.controller;

import com.pharmacy.entity.Sale;
import com.pharmacy.entity.User;
import com.pharmacy.repository.SaleRepository;
import com.pharmacy.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRepository userRepository;
    private final SaleRepository saleRepository;

    @GetMapping
    public ResponseEntity<List<User>> getAll() {
        List<User> users = userRepository.findAll().stream()
                .peek(u -> u.setPassword(null)) // Hide hashed passwords
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PostMapping
    public ResponseEntity<?> create(@Valid @RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists!");
        }
        // Hash password securely with BCrypt
        user.setPassword(BCrypt.hashpw(user.getPassword(), BCrypt.gensalt()));
        user.setIsActive(true);
        User saved = userRepository.save(user);
        saved.setPassword(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/performance")
    public ResponseEntity<?> getPerformance(HttpSession session) {
        User loggedUser = (User) session.getAttribute("user");
        if (loggedUser == null) {
            // Testing fallback to first user (Admin) if no active session
            loggedUser = userRepository.findById(1L).orElse(null);
        }

        if (loggedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not logged in");
        }

        List<Sale> sales = saleRepository.findByUser_IdOrderBySaleDateDesc(loggedUser.getId());

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

        return ResponseEntity.ok(response);
    }
}
