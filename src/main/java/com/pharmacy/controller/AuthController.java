package com.pharmacy.controller;

import com.pharmacy.entity.User;
import com.pharmacy.repository.UserRepository;
import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCrypt;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserRepository userRepository;

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> body, HttpSession session) {
        String username = body.get("username");
        String password = body.get("password");

        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || !user.getIsActive() || !BCrypt.checkpw(password, user.getPassword())) {
            return ResponseEntity.badRequest().body("Invalid username or password");
        }

        session.setAttribute("user", user);
        return ResponseEntity.ok(user);
    }
}
