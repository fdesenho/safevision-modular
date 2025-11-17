package com.safevision.alertservice.model;

import java.time.Instant;
import java.util.UUID;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "alerts")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class Alert {

    @Id
    @Column(length = 36)
    private String id = UUID.randomUUID().toString();

    @Column(nullable = false)
    private String userId;          // owner / affected user

    @Column(nullable = false)
    private String alertType;       // e.g., PERSON_DETECTED, MOTION, WEAPON

    @Column
    private String cameraId;

    @Column(columnDefinition = "text")
    private String description;

    @Column(nullable = false)
    private Instant createdAt = Instant.now();

    @Column(nullable = false)
    private boolean acknowledged = false;

    @Column
    private String severity;        // INFO, WARNING, CRITICAL
}
