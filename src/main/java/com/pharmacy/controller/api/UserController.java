package com.pharmacy.controller.api;

import com.pharmacy.config.PharmacyUserDetails;
import com.pharmacy.dto.request.UserCreateRequest;
import com.pharmacy.dto.response.UserResponse;
import com.pharmacy.model.Role;
import com.pharmacy.model.User;
import com.pharmacy.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Staff account management and individual performance tracking")
public class UserController {

    private final UserService userService;

    @GetMapping
    @Operation(summary = "List all users", description = "Returns all staff accounts with passwords hidden")
    public ResponseEntity<List<UserResponse>> getAll() {
        List<UserResponse> responses = userService.findAll().stream()
                .map(UserResponse::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    @Operation(summary = "Register a new staff user", description = "Creates a new user account with BCrypt-encoded password")
    public ResponseEntity<UserResponse> create(@Valid @RequestBody UserCreateRequest request) {
        if (userService.usernameExists(request.getUsername())) {
            return ResponseEntity.badRequest().build();
        }
        User user = User.builder()
                .name(request.getName())
                .username(request.getUsername())
                .password(request.getPassword())
                .role(Role.valueOf(request.getRole()))
                .build();
        User saved = userService.createUser(user);
        return ResponseEntity.status(HttpStatus.CREATED).body(UserResponse.fromEntity(saved));
    }

    @GetMapping("/performance")
    @Operation(summary = "Get logged-in user's performance", description = "Returns total revenue, sales count, and recent transactions for the authenticated user")
    public ResponseEntity<?> getPerformance(Authentication authentication) {
        PharmacyUserDetails principal = (PharmacyUserDetails) authentication.getPrincipal();
        return ResponseEntity.ok(userService.getUserPerformance(principal.getUserId()));
    }
}
