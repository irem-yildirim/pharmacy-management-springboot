package com.pharmacy.rest;

import com.pharmacy.config.PharmacyUserDetails;
import com.pharmacy.model.Sale;
import com.pharmacy.model.User;
import com.pharmacy.repository.SaleRepository;
import com.pharmacy.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Staff account management and individual performance tracking")
public class UserController {

    private final UserRepository userRepository;
    private final SaleRepository saleRepository;
    private final PasswordEncoder passwordEncoder;

    @GetMapping
    @Operation(summary = "List all users", description = "Returns all staff accounts with passwords hidden")
    public ResponseEntity<List<User>> getAll() {
        List<User> users = userRepository.findAll().stream()
                .peek(u -> u.setPassword(null))
                .collect(Collectors.toList());
        return ResponseEntity.ok(users);
    }

    @PostMapping
    @Operation(summary = "Register a new staff user", description = "Creates a new user account with BCrypt-encoded password")
    public ResponseEntity<?> create(@Valid @RequestBody User user) {
        if (userRepository.findByUsername(user.getUsername()).isPresent()) {
            return ResponseEntity.badRequest().body("Username already exists!");
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setIsActive(true);
        User saved = userRepository.save(user);
        saved.setPassword(null);
        return ResponseEntity.status(HttpStatus.CREATED).body(saved);
    }

    @GetMapping("/performance")
    @Operation(summary = "Get logged-in user's performance", description = "Returns total revenue, sales count, and recent transactions for the authenticated user")
    public ResponseEntity<?> getPerformance(Authentication authentication) {
        PharmacyUserDetails principal = (PharmacyUserDetails) authentication.getPrincipal();
        User loggedUser = userRepository.findById(principal.getUserId()).orElse(null);

        if (loggedUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("User not found");
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
