package com.safevision.authservice.integration;

import static org.hamcrest.Matchers.hasItem;
import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.util.HashSet;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safevision.authservice.AbstractIntegrationTest;
import com.safevision.authservice.dto.LoginRequest;
import com.safevision.authservice.dto.RegisterRequest;
import com.safevision.authservice.dto.UserUpdateRequest;
import com.safevision.authservice.model.User;
import com.safevision.authservice.repository.UserRepository;
import com.safevision.common.enums.AlertType;

@AutoConfigureMockMvc
@Transactional // Garante que o banco seja limpo após cada teste
class AuthFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired private MockMvc mockMvc;
    @Autowired private UserRepository userRepository;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private PasswordEncoder passwordEncoder;

    private final String TEST_USER = "testuser";

    @BeforeEach
    void setup() {
        userRepository.deleteAll();
        
        // Criamos o usuário que será usado nos testes de busca/sessão
        User user = new User();
        user.setUsername(TEST_USER);
        user.setPassword(passwordEncoder.encode("password123"));
        user.setEmail("test@safevision.com");
        user.setPhoneNumber("+554899999999");
        user.setRoles(new HashSet<>(Set.of("USER")));
        userRepository.save(user);
    }

    @Test
    @DisplayName("POST /auth/register - Success")
    void shouldRegisterUser() throws Exception {
        var request = new RegisterRequest(
            "new_user", "pass123", "new@test.com", 
            "+55489999", "rtsp://cam", Set.of("USER"), Set.of(AlertType.EMAIL)
        );

        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("new_user"));

        assert(userRepository.existsByUsername("new_user"));
    }

    @Test
    @WithMockUser(username = TEST_USER)
    @DisplayName("GET /auth/profile/{username} - Success")
    void shouldReturnUserProfile() throws Exception {
        // Corrigido: Removido /api e adicionado o path variable {username}
        mockMvc.perform(get("/auth/profile/{username}", TEST_USER))
               .andExpect(status().isOk())
               .andExpect(jsonPath("$.username").value(TEST_USER))
               .andExpect(jsonPath("$.email").value("test@safevision.com"));
    }

    @Test
    @WithMockUser(username = TEST_USER)
    @DisplayName("PUT /auth/update - Success")
    void shouldUpdateUserProfile() throws Exception {
        var updateRequest = new UserUpdateRequest(
                "updated@test.com", "+55488888", "rtsp://new", null, null);

        mockMvc.perform(put("/auth/update")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk());
        
        var updated = userRepository.findByUsername(TEST_USER).orElseThrow();
        assert(updated.getEmail().equals("updated@test.com"));
    }

    @Test
    @WithMockUser(username = TEST_USER)
    @DisplayName("GET /auth/contact/{username} - Success")
    void shouldReturnUserContactInfo() throws Exception {
        mockMvc.perform(get("/auth/contact/{username}", TEST_USER))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("test@safevision.com"));
    }

    @Test
    @WithMockUser(username = TEST_USER)
    @DisplayName("GET /auth/alert-preferences/{username} - Success")
    void shouldReturnUserAlertPreferences() throws Exception {
        // Corrigido para bater com a URL do seu Controller
        mockMvc.perform(get("/auth/alert-preferences/{username}", TEST_USER))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = TEST_USER)
    @DisplayName("GET /auth/validate - Success")
    void shouldValidateToken() throws Exception {
        mockMvc.perform(get("/auth/validate"))
                .andExpect(status().isOk())
                .andExpect(content().string("Token is valid"));
    }
    
    @Test
    @DisplayName("POST /auth/login - Failure (Unauthorized)")
    void login_ShouldReturn401_WhenCredentialsInvalid() throws Exception {
        // Teste de integração real: enviamos senha errada para o endpoint real
        var loginReq = new LoginRequest(TEST_USER, "wrong_password");
        
        mockMvc.perform(post("/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginReq)))
                .andExpect(status().isUnauthorized());
    }
    @Test
    @DisplayName("GET /auth/profile/{username} - Not Found")
    void shouldReturn404WhenUserNotFound() throws Exception {
        // Chamamos um usuário que garantidamente não existe após o deleteAll do setup
        mockMvc.perform(get("/auth/profile/non_existent_user"))
               .andExpect(status().isNotFound()); // Ou o status que seu GlobalExceptionHandler retornar
    }
}