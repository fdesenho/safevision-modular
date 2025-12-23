package com.safevision.authservice.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * Global exception handler for the Auth Service.
 * Centralizes error handling logic to ensure consistent API responses.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final String AUTH_ERROR_MESSAGE = "Invalid username or password.";

    /**
     * Handles generic runtime exceptions (usually business logic violations).
     * Returns 400 Bad Request.
     */
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException ex) {
        log.warn("⚠️ Business Logic Error: {}", ex.getMessage());
        
        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(new ErrorResponse(ex.getMessage()));
    }

    /**
     * Handles security/authentication exceptions.
     * Returns 401 Unauthorized.
     */
    @ExceptionHandler({AuthenticationException.class, UsernameNotFoundException.class})
    public ResponseEntity<ErrorResponse> handleAuthException(Exception ex) {
       
        log.warn("⛔ Authentication Failed: {}", ex.getMessage());

        return ResponseEntity
                .status(HttpStatus.UNAUTHORIZED)
                .body(new ErrorResponse(AUTH_ERROR_MESSAGE));
    }

    /**
     * Internal Record to define the error JSON structure.
     * Java 21 Feature: Concise, immutable data carrier.
     */
    public record ErrorResponse(String error) {}
}