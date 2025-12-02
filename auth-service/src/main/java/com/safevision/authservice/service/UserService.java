package com.safevision.authservice.service;

import java.util.Optional;
import java.util.Set;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.safevision.authservice.dto.RegisterRequest; // <--- Importe o DTO
import com.safevision.authservice.model.User;
import com.safevision.authservice.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    // --- MÉTODO CORRIGIDO ---
    // Agora aceita RegisterRequest em vez de User
    public User createUser(RegisterRequest request) {
        
        // 1. Verifica se já existe (Boa prática)
        if (repository.findByUsername(request.username()).isPresent()) {
            throw new RuntimeException("O usuário já existe: " + request.username());
        }

        // 2. Converte DTO -> Entity (Usando o Builder)
        User user = User.builder()
                .username(request.username())
                // IMPORTANTE: Criptografa a senha aqui
                .password(passwordEncoder.encode(request.password()))
                // Se vier null, define "USER" como padrão. Se vier preenchido, usa o que veio.
                .roles(request.roles() != null && !request.roles().isEmpty() ? request.roles() : Set.of("USER"))
                .build();

        // 3. Salva no banco
        return repository.save(user);
    }

    // Método usado pelo Spring Security no Login
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = getUserByUsername(username);

        // Converte as Roles (String) para Authority do Spring
        String[] roles = user.getRoles().toArray(new String[0]);

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(roles)
                .build();
    }

    // Método auxiliar para buscar usuário completo (usado no Controller)
    public User getUserByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado"));
    }

    public Optional<User> getUser(String id) {
        return repository.findById(id);
    }

    public User updateUser(User user) {
        // Lógica de update: só encripta se a senha mudou
        if (user.getPassword() != null && !user.getPassword().isEmpty()) {
            user.setPassword(passwordEncoder.encode(user.getPassword()));
        }
        return repository.save(user);
    }

    public void deleteUser(String id) {
        repository.deleteById(id);
    }
}