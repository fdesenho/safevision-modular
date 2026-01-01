package com.safevision.authservice;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

import com.safevision.authservice.dto.LoginRequest;
import com.safevision.authservice.dto.RegisterRequest;
import com.safevision.authservice.repository.UserRepository;
import com.safevision.authservice.service.AuthenticationService;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceExceptionTest {

    @Mock private AuthenticationManager authenticationManager;
    @Mock private UserRepository userRepository;
    @InjectMocks private AuthenticationService authService;

    @Test
    void login_ShouldThrowException_WhenCredentialsInvalid() {
        // 1. Criamos o DTO que o método espera
        var loginRequest = new LoginRequest("errado", "senha");

        // Simula o erro de autenticação do Spring Security
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
            .thenThrow(new BadCredentialsException("Invalid user"));

        // 2. Passamos o objeto loginRequest para o método
        assertThrows(BadCredentialsException.class, () -> {
            authService.login(loginRequest); 
        });
    }
    @Test
    @DisplayName("Deve falhar ao registrar usuário duplicado")
    void register_ShouldThrowException_WhenUserExists() {
    	var registerReq = new RegisterRequest(
    	        "existente", 
    	        "senha123", 
    	        "email@test.com", 
    	        null, // phoneNumber
    	        null, // cameraUrl
    	        null, // roles
    	        null  // alertTypes
    	    );
        
        
    	lenient().when(userRepository.existsByUsername("existente")).thenReturn(true);

        // Ajuste o nome da exceção conforme seu código (ex: RuntimeException ou ConflictException)
        assertThrows(RuntimeException.class, () -> {
            authService.register(registerReq);
        });
    }
    
}