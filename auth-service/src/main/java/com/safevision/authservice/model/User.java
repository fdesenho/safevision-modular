package com.safevision.authservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder // Facilita a criação de objetos no Service
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36)
    private String id;

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    // --- NOVOS CAMPOS ---

    // Cria uma tabela auxiliar (users_roles) automaticamente para guardar a lista de Strings
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Builder.Default // Garante que o builder inicie vazio se não informado
    private Set<String> roles = new HashSet<>();

    @CreationTimestamp // O Hibernate preenche a data atual no insert
    @Column(updatable = false) // A data de criação nunca muda
    private LocalDateTime createdAt;
}