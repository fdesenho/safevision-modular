package com.safevision.authservice.controller;

import com.safevision.authservice.dto.RegisterRequest;
import com.safevision.authservice.dto.UserResponse;
import com.safevision.authservice.model.User;
import com.safevision.authservice.service.JwtService;
import com.safevision.authservice.service.UserService;
import lombok.RequiredArgsConstructor;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;

    @PostMapping("/register")
    public ResponseEntity<UserResponse> register(@RequestBody RegisterRequest request) {
        // O Service agora recebe o DTO, cria o User, encripta e salva
        User user = userService.createUser(request);
        
        // Retorna apenas dados seguros (ID, Username, Roles) - SEM SENHA
        return ResponseEntity.ok(new UserResponse(
            user.getId(), 
            user.getUsername(), 
            user.getRoles()
        ));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        try {
            // 1. Autentica (Verifica se username existe e se a senha bate com o hash)
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.username(), request.password())
            );

            // 2. Recupera o usuário autenticado (que veio do UserDetailsService)
            // O Principal aqui é o UserDetails do Spring, mas como convertemos lá no Service,
            // podemos tentar recuperar os dados originais se necessário, ou carregar do banco.
            // Para simplificar e garantir as Roles, vamos usar o user retornado pelo auth.getPrincipal()
            
            // Nota: O UserDetails do Spring não é exatamente a sua entidade User.
            // Vamos ajustar o JwtService para ler do UserDetails ou buscar o User completo.
            // Para manter simples agora:
            
            org.springframework.security.core.userdetails.User userDetails = 
                (org.springframework.security.core.userdetails.User) auth.getPrincipal();
                
            // Buscamos o user completo para ter o ID e as Roles customizadas
            User user = userService.getUserByUsername(userDetails.getUsername());

            // 3. Gera o Token com ID e Roles
            String token = jwtService.generateToken(user);

            return ResponseEntity.ok(new TokenResponse(token));

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Credenciais Inválidas");
        }
    }

    // Endpoint simples para validar o token (usado pelo Gateway ou Front)
    @GetMapping("/validate")
    public ResponseEntity<String> validateToken() {
        return ResponseEntity.ok("Token is valid");
    }

    // DTOs locais (ou mova para pacote dto)
    public record LoginRequest(String username, String password) {}
    public record TokenResponse(String token) {}
}