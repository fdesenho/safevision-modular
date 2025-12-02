package com.safevision.authservice.service;

import java.util.Map; // Import para o Map
import java.util.Optional;
import java.util.Set;

import org.springframework.amqp.rabbit.core.RabbitTemplate; // Import RabbitTemplate
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.safevision.authservice.dto.RegisterRequest;
import com.safevision.authservice.model.User;
import com.safevision.authservice.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final RabbitTemplate rabbitTemplate; // Injeta o carteiro do RabbitMQ
    private final String visionConfigurationQueueName;
    public User createUser(RegisterRequest request) {
        if (repository.existsByUsername(request.username())) {
            throw new RuntimeException("Erro: Usuário já existe.");
        }
        if (request.email() != null && repository.existsByEmail(request.email())) {
            throw new RuntimeException("Erro: E-mail já cadastrado.");
        }

        User user = User.builder()
                .username(request.username())
                .password(passwordEncoder.encode(request.password()))
                .email(request.email())
                .phoneNumber(request.phoneNumber())
                .cameraConnectionUrl(request.cameraUrl())
                .roles(request.roles() != null ? request.roles() : Set.of("USER"))
                .build();

        return repository.save(user);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = getUserByUsername(username);
        String[] roles = user.getRoles().toArray(new String[0]);

        return org.springframework.security.core.userdetails.User
                .withUsername(user.getUsername())
                .password(user.getPassword())
                .roles(roles)
                .build();
    }

    public User getUserByUsername(String username) {
        return repository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));
    }

    public Optional<User> getUser(String id) {
        return repository.findById(id);
    }

    public User updateUser(User dadosAtualizados) {
        User usuarioExistente = repository.findById(dadosAtualizados.getId())
                .orElseThrow(() -> new RuntimeException("Usuário não encontrado para atualização"));

        boolean cameraChanged = false;

        if (dadosAtualizados.getUsername() != null) usuarioExistente.setUsername(dadosAtualizados.getUsername());
        if (dadosAtualizados.getEmail() != null) usuarioExistente.setEmail(dadosAtualizados.getEmail());
        if (dadosAtualizados.getPhoneNumber() != null) usuarioExistente.setPhoneNumber(dadosAtualizados.getPhoneNumber());
        
        // Lógica de detecção de mudança de câmera
        if (dadosAtualizados.getCameraConnectionUrl() != null) {
            // Verifica se realmente mudou para não enviar mensagem à toa
            if (!dadosAtualizados.getCameraConnectionUrl().equals(usuarioExistente.getCameraConnectionUrl())) {
                usuarioExistente.setCameraConnectionUrl(dadosAtualizados.getCameraConnectionUrl());
                cameraChanged = true;
            }
        }
        
        if (dadosAtualizados.getPassword() != null && !dadosAtualizados.getPassword().isEmpty()) {
            usuarioExistente.setPassword(passwordEncoder.encode(dadosAtualizados.getPassword()));
        }

        User salvo = repository.save(usuarioExistente);

        // --- GATILHO DE CONFIGURAÇÃO ---
        // Se a URL mudou, avisa o Python para reiniciar a captura
        if (cameraChanged) {
            sendCameraConfigToVisionAgent(salvo.getCameraConnectionUrl());
        }

        return salvo;
    }

    public void deleteUser(String id) {
        repository.deleteById(id);
    }

    // Método privado para enviar a mensagem
    private void sendCameraConfigToVisionAgent(String newUrl) {
        try {
            // O Python espera um JSON: {"cameraUrl": "http://..."}
            Map<String, String> message = Map.of("cameraUrl", newUrl);
            
            rabbitTemplate.convertAndSend(visionConfigurationQueueName, message);
            
            System.out.println("📡 Configuração de câmera enviada para o Vision Agent: " + newUrl);
        } catch (Exception e) {
            System.err.println("❌ Erro ao enviar config para RabbitMQ: " + e.getMessage());
        }
    }
}