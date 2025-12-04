package com.safevision.alertservice.service;

import com.safevision.alertservice.config.TelephonyProperties;
import com.safevision.alertservice.dto.AlertEventDTO;
import com.safevision.alertservice.dto.UserContactDTO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

@Slf4j
@Service
@EnableConfigurationProperties(TelephonyProperties.class) // Habilita o uso do Record
public class TelephonyService {

    private final RestTemplate twilioClient;
    private final RestClient authServiceClient;
    private final TelephonyProperties props;

    public TelephonyService(RestTemplate restTemplate, 
                            RestClient.Builder restClientBuilder, 
                            TelephonyProperties props) {
        this.twilioClient = restTemplate;
        this.props = props;
        // Configura o cliente interno uma única vez
        this.authServiceClient = restClientBuilder.baseUrl("http://auth-service:8080").build();
    }

    /**
     * Orchestrates the sending of a critical SMS.
     * Steps: Validate -> Fetch Contact -> Build Request -> Send.
     */
    public void sendCriticalSms(AlertEventDTO event) {
        if (!"CRITICAL".equalsIgnoreCase(event.severity())) {
            return;
        }

        log.info("🚨 Initiating Critical SMS sequence for event: {}", event.alertType());

        // 1. Determine Target Phone Number
        String targetPhone = resolveUserPhoneNumber(event.userId());
        
        // 2. Build Message
        String messageBody = """
            ALERTA CRÍTICO SAFEVISION
            Tipo: %s
            Descrição: %s
            Câmera: %s
            AÇÃO IMEDIATA NECESSÁRIA!
            """.formatted(event.alertType(), event.description(), event.cameraId());

        // Debug Log (Mock)
        logMockMessage(targetPhone, messageBody);

        // 3. Send Real SMS (Twilio)
        //executeTwilioRequest(targetPhone, messageBody);
    }

    /**
     * Fetches user contact info from Auth Service via HTTP.
     * Falls back to the default system number if user is not found or service is down.
     */
    private String resolveUserPhoneNumber(String username) {
        try {
            var contact = authServiceClient.get()
                    .uri("/auth/contact/{username}", username)
                    .retrieve()
                    .body(UserContactDTO.class);

            if (contact != null && contact.phoneNumber() != null) {
                return contact.phoneNumber();
            }
        } catch (Exception e) {
            log.warn("⚠️ Could not fetch user contact from Auth Service: {}. Using fallback.", e.getMessage());
        }
        
        log.info("Using fallback system phone number.");
        return props.toNumber();
    }

    /**
     * Encapsulates the complexity of the Twilio API call.
     */
    private void executeTwilioRequest(String to, String body) {
        try {
            var headers = createTwilioHeaders();
            var payload = createTwilioPayload(to, body);
            var request = new HttpEntity<>(payload, headers);

            var response = twilioClient.exchange(
                props.baseUrl(),
                HttpMethod.POST,
                request,
                String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ SMS successfully sent to {}", to);
            } else {
                log.error("❌ Twilio API failed with status: {}", response.getStatusCode());
            }
        } catch (Exception e) {
            log.error("❌ Critical error sending SMS: {}", e.getMessage());
        }
    }

    private HttpHeaders createTwilioHeaders() {
        var headers = new HttpHeaders();
        String auth = props.accountSid() + ":" + props.authToken();
        String encodedAuth = Base64.getEncoder().encodeToString(auth.getBytes(StandardCharsets.UTF_8));
        
        headers.set("Authorization", "Basic " + encodedAuth);
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        return headers;
    }

    private MultiValueMap<String, String> createTwilioPayload(String to, String body) {
        var map = new LinkedMultiValueMap<String, String>();
        map.add("To", to);
        map.add("From", props.fromNumber());
        map.add("Body", body);
        return map;
    }

    private void logMockMessage(String phone, String message) {
        log.debug("\n=== [SMS MOCK] ===\nTo: {}\nMsg: {}\n==================", phone, message);
    }
}