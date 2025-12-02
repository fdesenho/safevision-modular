package com.safevision.authservice.controller;

import com.safevision.authservice.dto.LoginRequest;
import com.safevision.authservice.dto.RegisterRequest;
import com.safevision.authservice.dto.TokenResponse;
import com.safevision.authservice.dto.UserResponse;
import com.safevision.authservice.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    
    private final AuthenticationService authService;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @GetMapping("/validate")
    public ResponseEntity<String> validateToken() {
        return ResponseEntity.ok("Token is valid");
    }
}