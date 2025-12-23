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
@EnableConfigurationProperties(TelephonyProperties.class) 
public class TelephonyService {

    private final RestTemplate twilioClient;
    private final RestClient authServiceClient;
    private final TelephonyProperties props;

    public TelephonyService(RestTemplate restTemplate, 
                            RestClient.Builder restClientBuilder, 
                            TelephonyProperties props) {
        this.twilioClient = restTemplate;
        this.props = props;
        this.authServiceClient = restClientBuilder.baseUrl("http://auth-service:8080").build();
    }

    /**
     * Orchestrates the sending of a critical SMS.
     * Steps: Validate -> Fetch Contact -> Build Request -> Send.
     */
    public void sendCriticalSms(AlertEventDTO alert) {
        if (!"CRITICAL".equalsIgnoreCase(alert.severity())) {
            return;
        }

        log.info("🚨 Initiating Critical SMS sequence for event: {}", alert.alertType());

        
        String targetPhone = resolveUserPhoneNumber(alert.userId());
        
        
        String messageBody = """
            ALERTA CRÍTICO SAFEVISION
            Tipo: %s
            Descrição: %s
            Câmera: %s
            AÇÃO IMEDIATA NECESSÁRIA!
            """.formatted(alert.alertType(), alert.description(), alert.cameraId());

        
       // logMockMessage(targetPhone, messageBody);

        
        executeTwilioRequest(targetPhone, messageBody);
        sendSmsWithImage(targetPhone, messageBody, alert.snapshotUrl());
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
    /**
     * Sends an SMS/MMS with an image attachment (MediaUrl).
     * Twilio automatically converts to MMS when MediaUrl is provided.
     */
    public void sendSmsWithImage(String userId, String message, String imageUrl) {

        log.info("📸 Sending image MMS to user {} with image: {}", userId, imageUrl);

       
        String targetPhone = resolveUserPhoneNumber(userId);

       
        try {
            var headers = createTwilioHeaders();

            var payload = new LinkedMultiValueMap<String, String>();
            payload.add("To", targetPhone);
            payload.add("From", props.fromNumber());
            payload.add("Body", message);
            payload.add("MediaUrl", imageUrl); // <-- AQUI adiciona a foto

            var request = new HttpEntity<>(payload, headers);

            var response = twilioClient.exchange(
                    props.baseUrl(),
                    HttpMethod.POST,
                    request,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful()) {
                log.info("✅ MMS sent successfully to {}", targetPhone);
            } else {
                log.error("❌ Twilio returned error: {}", response.getStatusCode());
            }

        } catch (Exception e) {
            log.error("❌ Error sending MMS: {}", e.getMessage());
        }
    }


    private void logMockMessage(String phone, String message) {
        log.debug("\n=== [SMS MOCK] ===\nTo: {}\nMsg: {}\n==================", phone, message);
    }
}