package com.safevision.alertservice.service;

import java.math.BigDecimal;
import java.util.concurrent.CompletableFuture;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.JsonNode;

import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for interacting with external Geocoding APIs (OpenStreetMap).
 * <p>
 * Design Principles:
 * 1. Asynchronous: Uses CompletableFuture to avoid blocking the main alert processing thread.
 * 2. Graceful Degradation: Returns null if the external API fails, ensuring the system stays up.
 * </p>
 */
@Slf4j
@Service
public class GeocodingService {

    private final RestClient restClient;

    public GeocodingService(RestClient.Builder builder) {
        // Initializes RestClient with the OSM base URL and mandatory User-Agent
        this.restClient = builder
            .baseUrl("https://nominatim.openstreetmap.org")
            .defaultHeader("User-Agent", "SafeVision-App/1.0 (contact@safevision.com)")
            .build();
    }

    /**
     * Performs an asynchronous reverse geocoding lookup.
     *
     * @param lat Latitude
     * @param lon Longitude
     * @return A CompletableFuture containing the human-readable address string, or null if failed.
     */
    public CompletableFuture<String> getAddressFromCoordinates(BigDecimal lat, BigDecimal lon) {
        if (lat == null || lon == null) {
            return CompletableFuture.completedFuture(null);
        }

        return CompletableFuture.supplyAsync(() -> {
            try {
                // Execute HTTP GET to Nominatim
                JsonNode response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                        .path("/reverse")
                        .queryParam("format", "jsonv2")
                        .queryParam("lat", lat.doubleValue()) 
                        .queryParam("lon", lon.doubleValue())
                        .queryParam("zoom", 18)
                        .build())
                    .retrieve()
                    .body(JsonNode.class);

                // Parse response
                if (response != null && response.has("display_name")) {
                    String fullAddress = response.get("display_name").asText();
                    
                    // Simplify address for better UI display (take first 3 components)
                    String[] parts = fullAddress.split(",");
                    if (parts.length >= 3) {
                         return parts[0] + ", " + parts[1] + ", " + parts[2];
                    }
                    return fullAddress;
                }
            } catch (Exception e) {
                // Log warning but do not throw exception to caller
                log.warn("🌍 Geocoding failed for [{}, {}]: {}", lat, lon, e.getMessage());
            }
            return null;
        });
    }
}