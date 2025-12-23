package com.safevision.alertservice.model;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.hibernate.annotations.CreationTimestamp;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

/**
 * JPA Entity representing a persisted Security Alert.
 * <p>
 * This class maps to the 'alerts' table in PostgreSQL. It stores the full context
 * of a threat, including the visual evidence (snapshot) and physical location (GPS/Address).
 * </p>
 */
@Entity
@Table(name = "alerts", indexes = {
    @Index(name = "idx_alert_user_id", columnList = "userId"),
    @Index(name = "idx_alert_user_date", columnList = "userId, createdAt DESC")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private String userId;

    @Column(nullable = false)
    private String alertType;

    @Column
    private String severity;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column
    private String cameraId;

    @Column(length = 1024)
    private String snapshotUrl;

    // --- GEOLOCATION DATA ---

    /**
     * GPS Latitude. Nullable to allow operation in GPS-denied environments.
     */
    @Column(precision = 10, scale = 7)
    private BigDecimal latitude;

    /**
     * GPS Longitude. Nullable to allow operation in GPS-denied environments.
     */
    @Column(precision = 10, scale = 7)
    private BigDecimal longitude;

    /**
     * Human-readable address derived from Reverse Geocoding.
     * Example: "123 Main St, Springfield".
     */
    @Column(length = 500)
    private String address;

   

    @Builder.Default
    @Column(nullable = false)
    private boolean acknowledged = false;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}