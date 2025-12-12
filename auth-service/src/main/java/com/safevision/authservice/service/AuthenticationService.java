package com.safevision.authservice.service;

import com.safevision.authservice.dto.*;
import com.safevision.authservice.model.AlertPreference;
import com.safevision.authservice.model.User;
import com.safevision.common.enums.AlertType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate; // <--- 1. Import necessário
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    
   

    /**
     * DTO interno para enviar a mensagem ao Python
     */
    public record CameraConfigMessage(String action, String userId, String cameraUrl) implements Serializable {}

    public UserResponse register(RegisterRequest request) {
        log.info("Processing registration for username: {}", request.username());
        User user = userService.createUser(request);
        
        log.info("User registered successfully. ID: {}", user.getId());
        return new UserResponse(user.getId(), user.getUsername(), user.getRoles());
    }

    public TokenResponse login(LoginRequest request) {
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        User user = userService.getUserByUsername(userDetails.getUsername());
        String token = jwtService.generateToken(user);
        
        return new TokenResponse(token);
    }
    
  
    public String getUserCameraUrl(String username) {
        User user = userService.getUserByUsername(username);
        return user.getCameraConnectionUrl();
    }

    public UserContactDTO getUserContact(String username) {
        User user = userService.getUserByUsername(username);
        return new UserContactDTO(
            user.getId(),
            user.getUsername(),
            user.getPhoneNumber(),
            user.getEmail()
        );
    }

    public UserResponse updateUser(String username, UserUpdateRequest request) {
        log.info("Updating profile for user: {}", username);

        User currentUser = userService.getUserByUsername(username);

       
       
      
        if (request.cameraConnectionUrl() != null) {
            currentUser.setCameraConnectionUrl(request.cameraConnectionUrl());
       
        }
        
        if (request.password() != null) currentUser.setPassword(request.password());

        User updatedUser = userService.updateUser(currentUser);

       

        log.info("Profile updated successfully for user: {}", username);
        
        return new UserResponse(
            updatedUser.getId(),
            updatedUser.getUsername(),
            updatedUser.getRoles()
        );
    }
    public List<AlertType> getAlertPreferences(String username) {
        User user = userService.getUserByUsername(username);
        
        if (user.getAlertPreferences() == null) {
            return List.of();
        }

        // TRANSFORMAÇÃO:
        // Pega a lista de Entidades (AlertPreference) -> Extrai apenas o Enum (AlertType)
        return user.getAlertPreferences().stream()
                .map(AlertPreference::getAlertType)
                .collect(Collectors.toList());
    }
}