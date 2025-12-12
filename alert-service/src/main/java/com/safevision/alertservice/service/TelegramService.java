package com.safevision.alertservice.service;

import com.safevision.alertservice.config.TelegramProperties;
import com.safevision.alertservice.dto.AlertEventDTO;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Service
@RequiredArgsConstructor
@EnableConfigurationProperties(TelegramProperties.class)
public class TelegramService {

    private final TelegramProperties props;
    private final RestTemplate restTemplate;

    private static final String API_URL_TEMPLATE = "https://api.telegram.org/bot%s/%s";
    private static final String DOCKER_INTERNAL_HOST = "minio";
    private static final String EXTERNAL_HOST = "localhost";

    /**
     * Main entry point. Decides strategy (Photo vs Text) based on payload.
     *
     * @param event The alert event containing detection data.
     */
    public void sendAlert(AlertEventDTO event) {
    	
    	if (!props.enabled()) return;

        if (hasValidSnapshot(event)) {
            sendPhotoAlert(event);
        } else {
            sendTextAlert(event);
        }
    }

    /**
     * Orchestrates the complex flow of sending an image:
     * 1. Resolve internal URL (Docker networking)
     * 2. Download image
     * 3. Build Multipart Request
     * 4. Send to Telegram
     * <p>
     * Falls back to text message on any failure.
     */
    private void sendPhotoAlert(AlertEventDTO event) {
        try {
            String internalUrl = resolveDockerUrl(event.snapshotUrl());
            byte[] imageBytes = downloadImage(internalUrl);

            var requestEntity = buildMultipartRequest(event, imageBytes);
            String telegramApiUrl = getApiUrl("sendPhoto");

            restTemplate.postForObject(telegramApiUrl, requestEntity, String.class);
            log.info("✅ Telegram Photo sent successfully.");

        } catch (Exception e) {
            log.error("❌ Failed to send photo ({}). Falling back to text.", e.getMessage());
            sendTextAlert(event);
        }
    }

    /**
     * Sends a simple text-only alert.
     */
    private void sendTextAlert(AlertEventDTO event) {
        try {
            
            String text = """
                🚨 <b>SAFEVISION ALERT</b> 🚨
                ⚠️ <b>Type:</b> %s
                📍 <b>Cam:</b> %s
                📝 <b>Info:</b> %s
                """.formatted(event.alertType(), event.cameraId(), event.description());

            var payload = new LinkedMultiValueMap<String, Object>();
            payload.add("chat_id", props.chatId());
            payload.add("text", text);
            payload.add("parse_mode", "HTML"); // <--- MUDANÇA IMPORTANTE

            restTemplate.postForObject(getApiUrl("sendMessage"), payload, String.class);
            log.info("✅ Telegram Text sent.");
        } catch (Exception e) {
            log.error("❌ Failed to send Telegram text: {}", e.getMessage());
        }
    }
    // ==================================================================================
    // PRIVATE HELPER METHODS (Low-Level Logic)
    // ==================================================================================

    /**
     * Solves the Docker Networking issue.
     * Replaces 'localhost' (browser friendly) with 'minio' (container friendly).
     */
    private String resolveDockerUrl(String url) {
        if (url == null) return "";
        return url.contains(EXTERNAL_HOST) 
                ? url.replace(EXTERNAL_HOST, DOCKER_INTERNAL_HOST) 
                : url;
    }

    /**
     * Downloads the image bytes from the Storage Service.
     * Throws exception if download fails, triggering the fallback.
     */
    private byte[] downloadImage(String url) {
        log.debug("⬇️ Downloading evidence from: {}", url);
        byte[] bytes = restTemplate.getForObject(url, byte[].class);
        
        if (bytes == null || bytes.length == 0) {
            throw new IllegalStateException("Downloaded image is empty");
        }
        return bytes;
    }

    /**
     * Constructs the HTTP Multipart request required by Telegram API for file uploads.
     */
    private HttpEntity<MultiValueMap<String, Object>> buildMultipartRequest(AlertEventDTO event, byte[] imageBytes) {
        var imageResource = new ByteArrayResource(imageBytes) {
            @Override
            public String getFilename() {
                return "evidence_" + System.currentTimeMillis() + ".jpg";
            }
        };

        var headers = new HttpHeaders();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);

        var body = new LinkedMultiValueMap<String, Object>();
        body.add("chat_id", props.chatId());
        body.add("photo", imageResource);
        
        // --- CORREÇÃO AQUI: MUDAMOS PARA HTML ---
        // Usamos <b> para negrito. O underscore do WEAPON_DETECTED não vai mais quebrar.
        String caption = "🚨 <b>ALERT:</b> %s | 📸 Evidence".formatted(event.alertType());
        
        body.add("caption", caption);
        body.add("parse_mode", "HTML"); // <--- MUDANÇA IMPORTANTE

        return new HttpEntity<>(body, headers);
    }

    private String getApiUrl(String method) {
        return API_URL_TEMPLATE.formatted(props.botToken(), method);
    }

    private boolean hasValidSnapshot(AlertEventDTO event) {
        return event.snapshotUrl() != null && !event.snapshotUrl().isBlank();
    }
}