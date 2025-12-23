package com.safevision.authservice.service;

import java.io.Serializable;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder; // 👈 Import Adicionado
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.safevision.authservice.dto.LoginRequest;
import com.safevision.authservice.dto.RegisterRequest;
import com.safevision.authservice.dto.TokenResponse;
import com.safevision.authservice.dto.UserContactDTO;
import com.safevision.authservice.dto.UserProfileDTO;
import com.safevision.authservice.dto.UserResponse;
import com.safevision.authservice.dto.UserUpdateRequest;
import com.safevision.authservice.model.AlertPreference;
import com.safevision.authservice.model.User;
import com.safevision.common.enums.AlertType;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;

    
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

    /**
     * Atualiza o perfil do usuário, incluindo preferências de alerta (One-To-Many).
     */
    @Transactional
    public UserResponse updateUser(String username, UserUpdateRequest request) {
        log.info("Processing update request for user: {}", username);

        
        User currentUser = userService.getUserByUsername(username);

        
        if (StringUtils.isNotBlank(request.email())) {
        
            currentUser.setEmail(request.email().trim());
        }

        
        if (StringUtils.isNotBlank(request.phoneNumber())) {
            currentUser.setPhoneNumber(request.phoneNumber().trim());
        }

        
        if (request.cameraConnectionUrl() != null) {
             currentUser.setCameraConnectionUrl(StringUtils.stripToNull(request.cameraConnectionUrl()));
        }

        
        if (StringUtils.isNotBlank(request.password())) {
            String rawPassword = request.password().trim();
            
        
            if (rawPassword.length() < 6) {
                throw new IllegalArgumentException("A nova senha deve ter no mínimo 6 caracteres.");
            }

           
            String encodedPassword = passwordEncoder.encode(rawPassword);
            currentUser.setPassword(encodedPassword);
            
            log.info("Password updated for user: {}", username);
        }

        
        if (request.alertPreferences() != null) {
        
            currentUser.getAlertPreferences().clear();

            if (!request.alertPreferences().isEmpty()) {
                request.alertPreferences().forEach(type -> {
                    AlertPreference pref = AlertPreference.builder()
                            .user(currentUser)
                            .alertType(type)
                            .build();
                    currentUser.getAlertPreferences().add(pref);
                });
            }
        }

        
        User savedUser = userService.saveUser(currentUser);

        return new UserResponse(
            savedUser.getId(),
            savedUser.getUsername(),
            savedUser.getRoles()
        );
    }

    


    public UserProfileDTO getUserProfile(String username) {
        User user = userService.getUserByUsername(username);
        
       
        List<AlertType> prefs = (user.getAlertPreferences() == null) ? List.of() :
            user.getAlertPreferences().stream()
                .map(AlertPreference::getAlertType)
                .collect(Collectors.toList());

        return new UserProfileDTO(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getPhoneNumber(),
            user.getCameraConnectionUrl(), 
            prefs
        );
    }
    
    
    

    public List<AlertType> getAlertPreferences(String username) {
        User user = userService.getUserByUsername(username);
        
        if (user.getAlertPreferences() == null) {
            return List.of();
        }

       
        return user.getAlertPreferences().stream()
                .map(AlertPreference::getAlertType)
                .collect(Collectors.toList());
    }
}