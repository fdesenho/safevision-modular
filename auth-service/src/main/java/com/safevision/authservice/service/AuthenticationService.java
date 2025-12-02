package com.safevision.authservice.service;

import com.safevision.authservice.dto.LoginRequest;
import com.safevision.authservice.dto.RegisterRequest;
import com.safevision.authservice.dto.TokenResponse;
import com.safevision.authservice.dto.UserResponse;
import com.safevision.authservice.model.User;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthenticationService {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    public UserResponse register(RegisterRequest request) {
        User user = userService.createUser(request);
        return new UserResponse(user.getId(), user.getUsername(), user.getRoles());
    }

    public TokenResponse login(LoginRequest request) {
        // 1. Autentica (Spring Security faz o trabalho pesado)
        Authentication auth = authenticationManager.authenticate(
            new UsernamePasswordAuthenticationToken(request.username(), request.password())
        );

        // 2. Recupera os detalhes do usuário
        UserDetails userDetails = (UserDetails) auth.getPrincipal();
        
        // 3. Busca o usuário completo para ter os dados customizados (ID, Roles, etc)
        // O ideal seria que o UserDetails já fosse o nosso User, mas para manter compatibilidade:
        User user = userService.getUserByUsername(userDetails.getUsername());

        // 4. Gera o token
        String token = jwtService.generateToken(user);
        
        return new TokenResponse(token);
    }
}