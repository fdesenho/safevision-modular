package com.safevision.alertservice.service;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestTemplate;

import com.safevision.alertservice.config.EmailProperties;
import com.safevision.alertservice.dto.AlertEventDTO;
import com.safevision.alertservice.dto.UserContactDTO;

import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@EnableConfigurationProperties(EmailProperties.class)
public class EmailAlertService {

    private final JavaMailSender mailSender;
    private final EmailProperties props;
    private final RestTemplate restTemplate;
    private final RestClient authServiceClient;
    
    
    
    public EmailAlertService(JavaMailSender mailSender, 
    		EmailProperties props, 
    		RestTemplate restTemplate,
    		RestClient.Builder authServiceClient) {
		this.mailSender=mailSender;
    	this.restTemplate=restTemplate;
		this.props = props;
		this.authServiceClient = authServiceClient.baseUrl("http://auth-service:8080").build();
	}


    public void sendHtmlAlert(AlertEventDTO event) {
        if (!props.enabled()) return;

        try {
            log.info("📧 Preparing email for event: {}", event.alertType());

            MimeMessage message = mailSender.createMimeMessage();
            // true = multipart (permite anexos)
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8"); 

            helper.setFrom(props.fromAddress());
            helper.setTo( resolveUserEmail(event.userId()));
            helper.setSubject("🚨 SafeVision Alerta: " + event.alertType());

            // Corpo HTML
            String htmlContent = """
                <html>
                <body style="font-family: Arial, sans-serif;">
                    <h2 style="color: #d32f2f;">⚠️ Detecção de Ameaça Identificada</h2>
                    <p><strong>Tipo:</strong> %s</p>
                    <p><strong>Severidade:</strong> %s</p>
                    <p><strong>Câmera:</strong> %s</p>
                    <p><strong>Descrição:</strong> %s</p>
                    <hr>
                    <p><em>Verifique o painel administrativo imediatamente.</em></p>
                </body>
                </html>
                """.formatted(event.alertType(), event.severity(), event.cameraId(), event.description());

            helper.setText(htmlContent, true); // true = isHtml

            // Anexar Imagem (Se existir)
            if (event.snapshotUrl() != null && !event.snapshotUrl().isEmpty()) {
                try {
                    byte[] imageBytes = restTemplate.getForObject(event.snapshotUrl(), byte[].class);
                    if (imageBytes != null) {
                        helper.addAttachment("evidence_snapshot.jpg", new ByteArrayResource(imageBytes));
                    }
                } catch (Exception imgEx) {
                    log.warn("Falha ao baixar imagem para o email: {}", imgEx.getMessage());
                }
            }

            mailSender.send(message);
            log.info("✅ Email sent successfully.");

        } catch (Exception e) {
            log.error("❌ Failed to send email: {}", e.getMessage());
        }
    }
    private String resolveUserEmail(String username) {
        try {
            var contact = authServiceClient.get()
                    .uri("/auth/contact/{username}", username)
                    .retrieve()
                    .body(UserContactDTO.class);

            if (contact != null && contact.email() != null) {
                return contact.email();
            }
        } catch (Exception e) {
            log.warn("⚠️ Could not fetch user contact from Auth Service: {}. Using fallback.", e.getMessage());
        }
        return null;
        
        
    }
}