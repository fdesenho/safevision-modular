package com.safevision.alertservice.integration;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.math.BigDecimal;
import java.util.UUID;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.safevision.alertservice.AbstractIntegrationTest;
import com.safevision.alertservice.dto.AlertEventDTO;
import com.safevision.alertservice.model.Alert;
import com.safevision.alertservice.repository.AlertRepository;

@AutoConfigureMockMvc
@WithMockUser(username = "user-test-123", roles = {"USER", "ADMIN"}) // Define um usuário padrão para a classe
class AlertControllerIntegrationTest extends AbstractIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private AlertRepository alertRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private final String TEST_USER = "user-test-123";

    @BeforeEach
    void setup() {
        alertRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /alert/event - Success")
    void receiveEvent_Success() throws Exception {
        var event = new AlertEventDTO(TEST_USER, "WEAPON_DETECTED", "Description", "CRITICAL", "CAM-01", null, new BigDecimal("-27.5"), new BigDecimal("-48.5"), null);

        mockMvc.perform(post("/alert/event")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(event)))
                .andDo(print())
                .andExpect(status().isCreated());
    }

    @Test
    @DisplayName("POST /alert/event - Bad Request (Invalid Data)")
    void receiveEvent_BadRequest() throws Exception {
        // Envia um evento com userId null para disparar o badRequest() do seu controller
        var invalidEvent = new AlertEventDTO(null, null, null, null, null, null, null, null, null);

        mockMvc.perform(post("/alert/event")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(invalidEvent)))
                .andDo(print())
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /alert/user/{username} - List Success")
    void getMyAlerts_Success() throws Exception {
        saveAlert(TEST_USER, "FIRE_DETECTED", false);
        saveAlert(TEST_USER, "INTRUSION", false);

        mockMvc.perform(get("/alert/user/" + TEST_USER))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("PATCH /alert/{id}/ack - Success")
    void acknowledgeAlert_Success() throws Exception {
        var alert = saveAlert(TEST_USER, "WEAPON", false);

        mockMvc.perform(patch("/alert/{id}/ack", alert.getId()))
                .andDo(print())
                .andExpect(status().isNoContent());

        var updatedAlert = alertRepository.findById(alert.getId()).orElseThrow();
        assert(updatedAlert.isAcknowledged());
    }

    @Test
    @DisplayName("GET /alert/history/{userId} - Pagination Success")
    void getHistory_Success() throws Exception {
        for (int i = 0; i < 15; i++) {
            saveAlert(TEST_USER, "TEST_TYPE_" + i, true);
        }

        mockMvc.perform(get("/alert/history/{userId}", TEST_USER)
                .param("page", "0")
                .param("size", "10"))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(10)))
                .andExpect(jsonPath("$.page.totalElements").value(15)); // 👈 Adicionado o .page aqui
    }

    private Alert saveAlert(String userId, String type, boolean ack) {
        var alert = Alert.builder()
                .userId(userId)
                .alertType(type)
                .description("Test")
                .severity("HIGH")
                .acknowledged(ack)
                .createdAt(java.time.LocalDateTime.now())
                .build();
        return alertRepository.save(alert);
    }
}