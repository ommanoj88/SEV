package com.evfleet.charging.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "charging_networks", indexes = {
    @Index(name = "idx_network_provider", columnList = "provider")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargingNetwork {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Network name is required")
    @Column(nullable = false, unique = true, length = 100)
    private String name;

    @NotBlank(message = "Provider is required")
    @Column(nullable = false, length = 100)
    private String provider;

    @Column(columnDefinition = "TEXT")
    private String apiEndpoint;

    @Column(columnDefinition = "TEXT")
    private String apiKey; // Should be encrypted in production

    @Column(columnDefinition = "TEXT")
    private String apiSecret; // Should be encrypted in production

    @Column(length = 50)
    private String authType; // API_KEY, OAUTH2, BASIC

    @Column(columnDefinition = "TEXT")
    private String webhookUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private NetworkStatus status = NetworkStatus.ACTIVE;

    @Column
    private Integer totalStations = 0;

    @Column(columnDefinition = "TEXT")
    private String supportedConnectors;

    @Column(length = 100)
    private String supportEmail;

    @Column(length = 20)
    private String supportPhone;

    @Column(columnDefinition = "TEXT")
    private String description;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum NetworkStatus {
        ACTIVE,
        INACTIVE,
        MAINTENANCE
    }
}
