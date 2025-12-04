package com.safevision.alertservice.model;

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
 * JPA Entity representing a Security Alert.
 * <p>
 * This entity stores the details of a threat detected by the Recognition Service.
 * It includes metadata about the user, camera, threat type, and visual evidence (snapshot).
 * </p>
 */
@Entity
@Table(name = "alerts", indexes = {
    // Performance: Index to optimize queries by 'userId' (e.g., fetching alert history)
    @Index(name = "idx_alert_user_id", columnList = "userId") 
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

    // --- CORE DATA ---

    /**
     * The ID of the user who owns the camera/alert.
     */
    @Column(nullable = false)
    private String userId;

    /**
     * The classification of the event (e.g., WEAPON_DETECTED, THREAT_STARE).
     */
    @Column(nullable = false)
    private String alertType;

    /**
     * The risk level: INFO, WARNING, or CRITICAL.
     */
    @Column
    private String severity;

    /**
     * A human-readable description of the event.
     * Uses TEXT type in Postgres to support long descriptions.
     */
    @Column(columnDefinition = "TEXT")
    private String description;

    // --- METADATA & EVIDENCE ---

    @Column
    private String cameraId;

    /**
     * Public URL pointing to the evidence image stored in MinIO.
     * Length set to 1024 to accommodate long signed URLs or deep paths.
     */
    @Column(length = 1024)
    private String snapshotUrl;

    // --- STATUS & AUDIT ---

    /**
     * Flag indicating if the user has seen/acknowledged this alert.
     * Defaults to false.
     */
    @Builder.Default
    @Column(nullable = false)
    private boolean acknowledged = false;

    /**
     * The exact timestamp when the alert was persisted.
     * Managed automatically by Hibernate.
     */
    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
}