package com.safevision.authservice.controller;

import com.safevision.authservice.dto.*;
import com.safevision.authservice.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

/**
 * REST Controller for handling Authentication and User Management.
 * Exposed via API Gateway under /auth path.
 */
@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authService;

    /**
     * Registers a new user in the system.
     *
     * @param request The registration details (username, password, contacts).
     * @return The created user details (excluding password).
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        log.info("Registering new user: {}", request.username());
        return ResponseEntity.ok(authService.register(request));
    }

    /**
     * Authenticates a user and returns a JWT Token.
     *
     * @param request The login credentials.
     * @return A JWT token if successful.
     */
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        log.debug("Login attempt for user: {}", request.username());
        return ResponseEntity.ok(authService.login(request));
    }
    
    /**
     * Internal endpoint used by Alert Service to fetch user contact info for notifications.
     *
     * @param username The username to look up.
     * @return Contact details (phone, email).
     */
    @GetMapping("/contact/{username}")
    public ResponseEntity<UserContactDTO> getUserContact(@PathVariable String username) {
        return ResponseEntity.ok(authService.getUserContact(username));
    }

    /**
     * Updates the authenticated user's profile (e.g., camera URL, phone number).
     *
     * @param request        The fields to update.
     * @param authentication The security context (current user).
     * @return The updated user details.
     */
    @PutMapping("/update")
    public ResponseEntity<UserResponse> updateUser(
            @RequestBody UserUpdateRequest request, 
            Authentication authentication) {
        
        String currentUsername = authentication.getName();
        log.info("Updating profile for user: {}", currentUsername);
        
        return ResponseEntity.ok(
            authService.updateUser(currentUsername, request)
        );
    }

    /**
     * Simple endpoint to validate if a Token is active.
     */
    @GetMapping("/validate")
    public ResponseEntity<String> validateToken() {
        return ResponseEntity.ok("Token is valid");
    }
}