package com.safevision.authservice.dto;

import com.safevision.common.enums.AlertType;
import io.swagger.v3.oas.annotations.media.Schema;
import java.util.Set;

@Schema(description = "User registration request payload")
public record RegisterRequest(
    @Schema(required = true, example = "fdesenho")
    String username,
    
    @Schema(required = true, example = "strongpassword123")
    String password,
    
    @Schema(required = true, example = "fabio@safevision.com")
    String email,
    
    @Schema(example = "+5548991234567")
    String phoneNumber,
    
    @Schema(description = "The streaming URL for the vision agent", example = "rtsp://admin:123@192.168.1.50:554")
    String cameraUrl,
    
    @Schema(description = "Requested roles (Admin approval may be required)")
    Set<String> roles,
    
    @Schema(description = "Initial alert notification types")
    Set<AlertType> alertTypes  
) {}