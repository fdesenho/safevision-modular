package com.safevision.authservice.service;

import com.safevision.authservice.dto.*;
import com.safevision.authservice.model.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

/**
 * Facade Service for Authentication and User Management.
 * <p>
 * This service orchestrates the interaction between the Web Layer (Controller),
 * Security Layer (AuthenticationManager/JWT), and Data Layer (UserService).
 * It implements the core business logic for login, registration, and profile updates.
 * </p>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    /**
     * Registers a new user.
     *
     * @param request The registration details.
     * @return The created user (safe DTO).
     */
    public UserResponse register(RegisterRequest request) {
        log.info("Processing registration for username: {}", request.username());
        User user = userService.createUser(request);
        
        log.info("User registered successfully. ID: {}", user.getId());
        return new UserResponse(user.getId(), user.getUsername(), user.getRoles());
    }

    /**
     * Authenticates a user and issues a JWT.
     *
     * @param request The login credentials.
     * @return The JWT Token wrapper.
     */
    public TokenResponse login(LoginRequest request) {
        log.debug("Authenticating user: {}", request.username());

        // 1. Authenticate via Spring Security (Validates password hash)
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        // 2. Retrieve User Details
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        
        // 3. Fetch full User entity to get custom fields (ID, Roles) for the Token
        User user = userService.getUserByUsername(userDetails.getUsername());

        // 4. Generate Signed JWT
        String token = jwtService.generateToken(user);
        
        log.info("Login successful for user: {}", user.getUsername());
        return new TokenResponse(token);
    }

    /**
     * Retrieves contact information for a specific user.
     * Used by internal services (Alert Service) for notifications.
     *
     * @param username The username to lookup.
     * @return Contact details (phone, email).
     */
    public UserContactDTO getUserContact(String username) {
        User user = userService.getUserByUsername(username);
        return new UserContactDTO(
            user.getId(),
            user.getUsername(),
            user.getPhoneNumber(),
            user.getEmail()
        );
    }

    /**
     * Updates the user profile based on the provided request DTO.
     * Handles partial updates (only non-null fields are updated).
     *
     * @param username The username of the user to update.
     * @param request  The fields to update.
     * @return The updated user details.
     */
    public UserResponse updateUser(String username, UserUpdateRequest request) {
        log.info("Updating profile for user: {}", username);

        // 1. Fetch current user state
        User currentUser = userService.getUserByUsername(username);

        // 2. Map DTO fields to Entity (Partial Update)
        if (request.email() != null) currentUser.setEmail(request.email());
        if (request.phoneNumber() != null) currentUser.setPhoneNumber(request.phoneNumber());
        if (request.cameraConnectionUrl() != null) currentUser.setCameraConnectionUrl(request.cameraConnectionUrl());
        if (request.password() != null) currentUser.setPassword(request.password());

        // 3. Save changes (Triggers RabbitMQ event if camera changed)
        User updatedUser = userService.updateUser(currentUser);

        log.info("Profile updated successfully for user: {}", username);
        
        // 4. Return response
        return new UserResponse(
            updatedUser.getId(),
            updatedUser.getUsername(),
            updatedUser.getRoles()
        );
    }
}