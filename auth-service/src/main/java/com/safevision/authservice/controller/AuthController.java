package com.safevision.authservice.controller;

import com.safevision.authservice.dto.*;
import com.safevision.authservice.service.AuthenticationService;
import com.safevision.common.enums.AlertType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;

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

    private final AuthenticationService authenticationService;

    /**
     * Registers a new user in the system.
     *
     * @param request The registration details (username, password, contacts).
     * @return The created user details (excluding password).
     */
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        log.info("Registering new user: {}", request.username());
        return ResponseEntity.ok(authenticationService.register(request));
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
        return ResponseEntity.ok(authenticationService.login(request));
    }
    
    /**
     * Internal endpoint used by Alert Service to fetch user contact info for notifications.
     *
     * @param username The username to look up.
     * @return Contact details (phone, email).
     */
    @GetMapping("/contact/{username}")
    public ResponseEntity<UserContactDTO> getUserContact(@PathVariable String username) {
        return ResponseEntity.ok(authenticationService.getUserContact(username));
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
            authenticationService.updateUser(currentUsername, request)
        );
    }

    /**
     * Simple endpoint to validate if a Token is active.
     */
    @GetMapping("/validate")
    public ResponseEntity<String> validateToken() {
        return ResponseEntity.ok("Token is valid");
    }
    @GetMapping("/camera-url")
    public ResponseEntity<Map<String, String>> getCameraUrl(Authentication authentication) {
        
        String username = authentication.getName();
        
        String url = authenticationService.getUserCameraUrl(username);
        
        return ResponseEntity.ok(Map.of("cameraUrl", url != null ? url : ""));
    }
    
    
    @GetMapping("/profile/{username}")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable String username) {
        return ResponseEntity.ok(authenticationService.getUserProfile(username));
    }
    
 
    @GetMapping("/alert-preferences/{username}")
    public ResponseEntity<List<AlertType>> getPreferences(@PathVariable String username) {
        return ResponseEntity.ok(authenticationService.getAlertPreferences(username));
    }
}
