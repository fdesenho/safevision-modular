package com.safevision.alertservice.service;

import com.safevision.alertservice.dto.AlertEventDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

@Service
@RequiredArgsConstructor
public class TelephonyService {

    // Injete o RestTemplate (Certifique-se de ter o @Bean na sua Main Application)
    private final RestTemplate restTemplate;

    // Injete todos os segredos do YAML
    @Value("${telephony.account-sid}") 
    private String accountSid;
    
    @Value("${telephony.auth-token}")
    private String authToken;
    
    @Value("${telephony.from-number}")
    private String fromNumber;
    
    @Value("${telephony.to-number}")
    private String toNumber;

    @Value("${telephony.base-url}")
    private String apiUrl;

    public void sendCriticalSms(AlertEventDTO event) {
    	messageCriticalTest(event);
    	
    	
		/*
		 * // 1. O Alerta só é disparado se for CRÍTICO if
		 * (!event.severity().equalsIgnoreCase("CRITICAL")) { return; }
		 * 
		 * String alertMessage = String.format(
		 * "ALERTA CRÍTICO SAFEVISION - %s: %s (Câmera %s). AÇÃO IMEDIATA!",
		 * event.alertType(), event.description(), event.cameraId() );
		 * 
		 * // 2. Montar Cabeçalhos (Basic Auth para Twilio) // Twilio usa Basic Auth
		 * onde o Username é o Account SID e o Password é o Auth Token HttpHeaders
		 * headers = new HttpHeaders(); String auth = accountSid + ":" + authToken;
		 * byte[] encodedAuth = java.util.Base64.getEncoder().encode(auth.getBytes());
		 * String authHeader = "Basic " + new String(encodedAuth);
		 * 
		 * headers.set("Authorization", authHeader);
		 * headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED); // Twilio
		 * espera form data
		 * 
		 * // 3. Montar o Corpo da Requisição (Form Data) MultiValueMap<String, String>
		 * map = new LinkedMultiValueMap<>(); map.add("To", toNumber); map.add("From",
		 * fromNumber); map.add("Body", alertMessage);
		 * 
		 * HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map,
		 * headers);
		 * 
		 * // 4. Enviar (Ação de Produção) try { ResponseEntity<String> response =
		 * restTemplate.exchange( apiUrl, HttpMethod.POST, request, String.class );
		 * 
		 * if (response.getStatusCode().is2xxSuccessful()) {
		 * System.out.println("✅ SMS/Chamada Crítica enviada para " + toNumber); } else
		 * { System.err.println("❌ Falha na API de Telefonia. Status: " +
		 * response.getStatusCode()); } } catch (Exception e) {
		 * System.err.println("❌ ERRO GRAVE ao conectar na API de Telefonia: " +
		 * e.getMessage()); // Aqui seria o ponto para reverter para um sistema de
		 * e-mail ou log local. }
		 */
    }

	private void messageCriticalTest(AlertEventDTO event) {
		String message = String.format(
                "ALERTA CRÍTICO: %s na câmera %s. AÇÃO IMEDIATA!",
                event.alertType(), 
                event.cameraId()
            );
            
            // --- SIMULAÇÃO: REGISTRO DA CHAMADA NO LOG ---
            System.out.println("\n===============================================");
            System.out.println("📞 [SMS/CHAMADA MOCK] Sucesso!");
            System.out.println("  De: " + fromNumber + " Para: " + toNumber);
            System.out.println("  Mensagem: " + message);
            System.out.println("===============================================\n");
            // --- FIM DA SIMULAÇÃO ---
	}
}