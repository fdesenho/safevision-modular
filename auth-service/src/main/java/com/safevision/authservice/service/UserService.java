package com.safevision.authservice.service;

import com.safevision.authservice.dto.RegisterRequest;
import com.safevision.authservice.model.AlertPreference;
import com.safevision.authservice.model.User;
import com.safevision.authservice.repository.AlertPreferenceRepository;
import com.safevision.authservice.repository.UserRepository;
import com.safevision.common.enums.AlertType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
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
    private final AlertPreferenceRepository alertPreferenceRepository;

    

    /**
     * Creates a new user after validating uniqueness.
     */
    
	 public User createUser(RegisterRequest request) {
	        log.debug("Attempting to create user: {}", request.username());
	
	        if (repository.existsByUsername(request.username())) {
	            throw new RuntimeException("Usuario ja cadastrado.");
	        }
	        if (request.email() != null && repository.existsByEmail(request.email())) {
	            throw new RuntimeException("Email ja cadastrado");
	        }
	
	        var user = User.builder()
	                .username(request.username())
	                .password(passwordEncoder.encode(request.password()))
	                .email(request.email())
	                .phoneNumber(request.phoneNumber())
	                .cameraConnectionUrl(request.cameraUrl())
	                .roles(request.roles() != null ? request.roles() : Set.of("USER"))
	                .build();
	
	        var savedUser = repository.save(user);
	
	        // Persist alert preferences
	        Set<AlertType> selectedTypes = request.alertTypes() == null || request.alertTypes().isEmpty()
	                ? new HashSet<>()
	                : request.alertTypes();
	
	        for (AlertType t : selectedTypes) {
	            var pref = AlertPreference.builder()
	                    .user(savedUser)
	                    .alertType(t)
	                    .build();
	            alertPreferenceRepository.save(pref);
	        }
	
	        log.info("User {} created with {} alert preferences", savedUser.getUsername(), selectedTypes.size());
	
	        
	
	        return savedUser;
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

       

        // Partial Update Logic
        if (updatedData.getUsername() != null) existingUser.setUsername(updatedData.getUsername());
        if (updatedData.getEmail() != null) existingUser.setEmail(updatedData.getEmail());
        if (updatedData.getPhoneNumber() != null) existingUser.setPhoneNumber(updatedData.getPhoneNumber());

        // Check for Camera URL change
        if (updatedData.getCameraConnectionUrl() != null) {
            String oldUrl = existingUser.getCameraConnectionUrl();
            String newUrl = updatedData.getCameraConnectionUrl();
            
            if (!newUrl.equals(oldUrl)) 
                existingUser.setCameraConnectionUrl(newUrl);
                
        }

        // Update password if provided
       
        var savedUser = repository.save(existingUser);

       

        return savedUser;
    }

    public void deleteUser(String id) {
        repository.deleteById(id);
    }
    
    public User getUserByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
    }

    /**
     * Valida integridade e persiste as alterações.
     * NÃO realiza criptografia de senha (responsabilidade do Auth Service).
     */
    public User saveUser(User userToSave) {
        // Validação de Integridade 1: E-mail Único
        // Verifica se o email existe em ALGUM registro cujo ID NÃO SEJA o meu
        if (userToSave.getEmail() != null) {
            boolean emailTaken = repository.existsByEmailAndIdNot(userToSave.getEmail(), userToSave.getId());
            if (emailTaken) {
                throw new IllegalArgumentException("Este e-mail já está em uso por outro usuário.");
            }
        }

        // Validação de Integridade 2: Telefone Único (Opcional, mas recomendado)
        if (userToSave.getPhoneNumber() != null) {
            boolean phoneTaken = repository.existsByPhoneNumberAndIdNot(userToSave.getPhoneNumber(), userToSave.getId());
            if (phoneTaken) {
                throw new IllegalArgumentException("Este telefone já está em uso por outro usuário.");
            }
        }

        // Salvamento Limpo
        // O JPA detecta que o ID existe e faz um UPDATE
        return repository.save(userToSave);
    }

   
}