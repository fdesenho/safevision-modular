package com.safevision.authservice.service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.safevision.authservice.dto.RegisterRequest;
import com.safevision.authservice.model.User;
import com.safevision.authservice.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Core service for User Management.
 * Implements Spring Security's UserDetailsService for authentication.
 * Handles CRUD operations and triggers configuration updates via RabbitMQ.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final RabbitTemplate rabbitTemplate;
    private final String visionConfigQueue;

    

    /**
     * Creates a new user after validating uniqueness.
     */
    public User createUser(RegisterRequest request) {
        log.debug("Attempting to create user: {}", request.username());

        if (repository.existsByUsername(request.username())) {
            throw new RuntimeException("Username already exists.");
        }
        if (request.email() != null && repository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already registered.");
        }

        var user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .cameraConnectionUrl(request.cameraUrl())
                .roles(request.roles() != null ? request.roles() : Set.of("USER"))
                .build();

        return repository.save(user);
    }

    /**
     * Loads user by username (Spring Security contract).
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        var user = getUserByUsername(username);
        
        // Convert Set<String> roles to String[] for Spring Security
        String[] roles = user.getRoles().toArray(new String[0]);

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(roles)
                .build();
    }

    public User getUserByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    public Optional<User> getUser(String id) {
        return repository.findById(id);
    }

    /**
     * Updates an existing user profile.
     * Triggers a RabbitMQ event if the camera URL changes.
     */
    public User updateUser(User updatedData) {
        var existingUser = repository.findById(updatedData.getId())
                .orElseThrow(() -> new RuntimeException("User not found for update"));

        boolean cameraChanged = false;

        // Partial Update Logic
        if (updatedData.getUsername() != null) existingUser.setUsername(updatedData.getUsername());
        if (updatedData.getEmail() != null) existingUser.setEmail(updatedData.getEmail());
        if (updatedData.getPhoneNumber() != null) existingUser.setPhoneNumber(updatedData.getPhoneNumber());

        // Check for Camera URL change
        if (updatedData.getCameraConnectionUrl() != null) {
            String oldUrl = existingUser.getCameraConnectionUrl();
            String newUrl = updatedData.getCameraConnectionUrl();
            
            if (!newUrl.equals(oldUrl)) {
                existingUser.setCameraConnectionUrl(newUrl);
                cameraChanged = true;
            }
        }

        // Update password if provided
        if (updatedData.getPassword() != null && !updatedData.getPassword().isEmpty()) {
            existingUser.setPassword(passwordEncoder.encode(updatedData.getPassword()));
        }

        var savedUser = repository.save(existingUser);

        // Notify Vision Agent if camera changed
        if (cameraChanged) {
            sendCameraConfigToVisionAgent(savedUser.getCameraConnectionUrl());
        }

        return savedUser;
    }

    public void deleteUser(String id) {
        repository.deleteById(id);
    }

    /**
     * Sends the new camera URL to the Vision Agent via RabbitMQ.
     */
    private void sendCameraConfigToVisionAgent(String newUrl) {
        try {
            var message = Map.of("cameraUrl", newUrl);
            rabbitTemplate.convertAndSend(visionConfigQueue, message);
            log.info("📡 Camera configuration sent to Vision Agent: {}", newUrl);
        } catch (Exception e) {
            log.error("❌ Failed to send config to RabbitMQ: {}", e.getMessage());
        }
    }
}