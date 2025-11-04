package com.evfleet.charging.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "route_optimizations", indexes = {
    @Index(name = "idx_route_vehicle", columnList = "vehicleId"),
    @Index(name = "idx_route_created", columnList = "createdAt")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RouteOptimization {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Vehicle ID is required")
    @Column(nullable = false)
    private Long vehicleId;

    @NotBlank(message = "Origin is required")
    @Column(nullable = false, length = 500)
    private String origin;

    @Column
    private Double originLat;

    @Column
    private Double originLng;

    @NotBlank(message = "Destination is required")
    @Column(nullable = false, length = 500)
    private String destination;

    @Column
    private Double destinationLat;

    @Column
    private Double destinationLng;

    @Column(columnDefinition = "TEXT")
    private String chargingStops; // JSON array of station IDs

    @Column
    private Integer numberOfStops = 0;

    @Column
    private Long estimatedTimeMinutes;

    @Column(precision = 10, scale = 2)
    private BigDecimal estimatedCost;

    @Column(precision = 10, scale = 2)
    private BigDecimal totalDistance; // in km

    @Column(precision = 10, scale = 2)
    private BigDecimal estimatedEnergyRequired; // kWh

    @Column(precision = 5, scale = 2)
    private BigDecimal currentBatteryLevel; // Percentage

    @Column(precision = 5, scale = 2)
    private BigDecimal estimatedFinalBatteryLevel; // Percentage

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OptimizationStatus status = OptimizationStatus.PLANNED;

    @Column(columnDefinition = "TEXT")
    private String routeDetails; // JSON with full route information

    @Column(columnDefinition = "TEXT")
    private String weatherConditions;

    @Column(columnDefinition = "TEXT")
    private String trafficConditions;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public enum OptimizationStatus {
        PLANNED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }
}
