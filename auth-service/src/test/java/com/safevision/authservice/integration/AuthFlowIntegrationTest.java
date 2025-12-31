package com.safevision.authservice.integration;


import com.fasterxml.jackson.databind.ObjectMapper;
import com.safevision.authservice.AbstractIntegrationTest;
import com.safevision.authservice.dto.LoginRequest;
import com.safevision.authservice.dto.RegisterRequest;
import com.safevision.authservice.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@AutoConfigureMockMvc
class AuthFlowIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper; // Para converter Objetos em JSON

    @BeforeEach
    void setup() {
        // Limpa o banco antes de cada teste para garantir isolamento
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("Should register a new user successfully and save to Database")
    void shouldRegisterUser() throws Exception {
        // 1. Arrange (Prepara o DTO)
        RegisterRequest request = new RegisterRequest(
                "integration_user",
                "pass123",
                "test@safevision.com",
                "+554899999999",
                "rtsp://cam01",
                Set.of("USER"),
                Set.of()
        );

        // 2. Act & Assert (Chama o Endpoint)
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print()) // Loga o request/response no console
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", notNullValue()))
                .andExpect(jsonPath("$.username", is("integration_user")));

        // 3. Validação Extra: Checa se realmente gravou no Postgres
        assertTrue(userRepository.findByUsername("integration_user").isPresent());
    }

    @Test
    @DisplayName("Should login and return JWT token")
    void shouldLoginSuccessfully() throws Exception {
        // 1. Arrange: Cria o usuário primeiro (direto no banco ou via endpoint)
        RegisterRequest register = new RegisterRequest(
                "login_user", "strongPass!", "login@safevision.com", null, null, null, null
        );
        
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(register)))
                .andExpect(status().isOk());

        // 2. Act: Tenta logar
        LoginRequest login = new LoginRequest("login_user", "strongPass!");

        // 3. Assert: Espera receber o Token
        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(login)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token", notNullValue()));
    }

    @Test
    @DisplayName("Should fail when registering duplicate username")
    void shouldFailDuplicateUser() throws Exception {
        // Arrange
        RegisterRequest request = new RegisterRequest(
                "duplicate_user", "123", "dup@test.com", null, null, null, null
        );

        // Primeiro cadastro: OK
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk());

        // Segundo cadastro: Deve falhar (400 ou 500 dependendo do seu GlobalExceptionHandler)
        // Como você lança RuntimeException no Service, o Spring devolve 500 por padrão se não tratado
        mockMvc.perform(post("/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isInternalServerError()); // Ajuste para isBadRequest() se tiver Handler
    }
}