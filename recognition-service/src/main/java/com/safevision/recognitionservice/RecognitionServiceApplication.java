package com.safevision.recognitionservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.web.client.RestTemplate;

@SpringBootApplication
public class RecognitionServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RecognitionServiceApplication.class, args);
    }
    
   
}
