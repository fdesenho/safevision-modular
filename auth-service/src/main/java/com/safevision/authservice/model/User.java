package com.safevision.authservice.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * JPA Entity representing a Registered User.
 * <p>
 * This class maps to the 'users' table and holds authentication credentials,
 * contact information for alerts, and configuration for external devices (Vision Agent).
 * </p>
 */
@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User {

    // ========================================================================
    // IDENTITY
    // ========================================================================

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(length = 36, updatable = false, nullable = false)
    private String id;

    // ========================================================================
    // AUTHENTICATION CREDENTIALS
    // ========================================================================

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false)
    private String password;

    // ========================================================================
    // CONTACT INFORMATION (For Alerts/Notifications)
    // ========================================================================

    @Column(unique = true)
    private String email;

    /**
     * Mobile number formatted for SMS providers (e.g., Twilio E.164 format).
     */
    @Column(name = "phone_number")
    private String phoneNumber;

    // ========================================================================
    // DEVICE CONFIGURATION (Vision Agent)
    // ========================================================================

    /**
     * The RTSP or HTTP URL of the camera stream associated with this user.
     * Used by the Vision Agent to capture video.
     */
    @Column(name = "camera_connection_url")
    private String cameraConnectionUrl;

    /**
     * A friendly name for the user's device (e.g., "Body Cam 01").
     */
    @Column(name = "device_name")
    private String deviceName;

    // ========================================================================
    // SECURITY & ROLES
    // ========================================================================

    /**
     * Roles assigned to the user (e.g., "USER", "ADMIN").
     * Fetched EAGERly to ensure Spring Security has permissions available during the login context.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "role")
    @Builder.Default
    private Set<String> roles = new HashSet<>();

    // ========================================================================
    // AUDIT
    // ========================================================================

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @Builder.Default
    private Set<AlertPreference> alertPreferences = new HashSet<>();

	
}