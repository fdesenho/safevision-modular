package com.safevision.authservice.controller;

import com.safevision.authservice.dto.*;
import com.safevision.authservice.service.AuthenticationService;
import com.safevision.common.enums.AlertType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Endpoints for user registration, login, and profile management")
public class AuthController {

    private final AuthenticationService authenticationService;

    @Operation(summary = "Register a new user", description = "Creates a new user account and stores contact preferences.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "User registered successfully"),
        @ApiResponse(responseCode = "400", description = "Invalid input data or username already exists")
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        log.info("Registering new user: {}", request.username());
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @Operation(summary = "Authenticate user", description = "Validates credentials and returns a JWT access token.")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Authentication successful"),
        @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<TokenResponse> login(@RequestBody LoginRequest request) {
        log.debug("Login attempt for user: {}", request.username());
        return ResponseEntity.ok(authenticationService.login(request));
    }

    @Operation(summary = "Get user contact (Internal)", description = "Fetch contact info for the Alert Service. Used via Feign Client.")
    @GetMapping("/contact/{username}")
    public ResponseEntity<UserContactDTO> getUserContact(@PathVariable String username) {
        return ResponseEntity.ok(authenticationService.getUserContact(username));
    }

    @Operation(summary = "Update user profile", description = "Updates phone, email, or camera settings for the authenticated user.")
    @PutMapping("/update")
    public ResponseEntity<UserResponse> updateUser(
            @RequestBody UserUpdateRequest request,
            @Parameter(hidden = true) Authentication authentication) {
        
        String currentUsername = authentication.getName();
        log.info("Updating profile for user: {}", currentUsername);
        return ResponseEntity.ok(authenticationService.updateUser(currentUsername, request));
    }

    @Operation(summary = "Validate Token", description = "Check if the current JWT is still valid and active.")
    @GetMapping("/validate")
    public ResponseEntity<String> validateToken() {
        return ResponseEntity.ok("Token is valid");
    }

    @Operation(summary = "Retrieve Camera URL", description = "Get the assigned RTSP/Stream URL for the user's camera.")
    @GetMapping("/camera-url")
    public ResponseEntity<Map<String, String>> getCameraUrl(@Parameter(hidden = true) Authentication authentication) {
        String username = authentication.getName();
        String url = authenticationService.getUserCameraUrl(username);
        return ResponseEntity.ok(Map.of("cameraUrl", url != null ? url : ""));
    }

    @Operation(summary = "Get Profile Details", description = "Retrieve full profile information for a specific username.")
    @GetMapping("/profile/{username}")
    public ResponseEntity<UserProfileDTO> getUserProfile(@PathVariable String username) {
        return ResponseEntity.ok(authenticationService.getUserProfile(username));
    }

    @Operation(summary = "Get Alert Preferences", description = "Retrieve the notification preferences (Email, SMS, Push) for a user.")
    @GetMapping("/alert-preferences/{username}")
    public ResponseEntity<List<AlertType>> getPreferences(@PathVariable String username) {
        return ResponseEntity.ok(authenticationService.getAlertPreferences(username));
    }
}