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

@Entity
@Table(name = "alerts", indexes = {
    // CRUCIAL: Cria um índice para deixar a busca por usuário rápida
    @Index(name = "idx_alert_user_id", columnList = "userId") 
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // Permite usar Alert.builder()... no Service
public class Alert {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID) // Otimizado para Postgres/Hibernate 6
    @Column(length = 36)
    private String id;

    @Column(nullable = false)
    private String userId;          // owner / affected user

    @Column(nullable = false)
    private String alertType;       // e.g., PERSON_DETECTED, MOTION, WEAPON

    @Column
    private String cameraId;

    @Column(columnDefinition = "TEXT") // TEXT no Postgres é melhor que varchar(255) para descrições
    private String description;

    @CreationTimestamp // O Hibernate preenche isso automaticamente no save
    @Column(nullable = false, updatable = false) // updatable=false impede alterar a data de criação
    private LocalDateTime createdAt;

    @Builder.Default // Garante que o Builder respeite esse padrão false
    @Column(nullable = false)
    private boolean acknowledged = false;

    @Column
    private String severity;        // INFO, WARNING, CRITICAL
}