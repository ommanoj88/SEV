package com.evfleet.charging.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;

@Entity
@Table(name = "charging_sessions", indexes = {
    @Index(name = "idx_session_vehicle", columnList = "vehicleId"),
    @Index(name = "idx_session_station", columnList = "stationId"),
    @Index(name = "idx_session_status", columnList = "status"),
    @Index(name = "idx_session_start_time", columnList = "startTime")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ChargingSession {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "Vehicle ID is required")
    @Column(nullable = false)
    private Long vehicleId;

    @NotNull(message = "Station ID is required")
    @Column(nullable = false)
    private Long stationId;

    @NotNull(message = "Start time is required")
    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column
    private LocalDateTime endTime;

    @DecimalMin(value = "0.0", message = "Energy consumed cannot be negative")
    @Column(precision = 10, scale = 2)
    private BigDecimal energyConsumed = BigDecimal.ZERO; // kWh

    @DecimalMin(value = "0.0", message = "Cost cannot be negative")
    @Column(precision = 10, scale = 2)
    private BigDecimal cost = BigDecimal.ZERO;

    @DecimalMin(value = "0.0", message = "Starting battery level cannot be negative")
    @DecimalMax(value = "100.0", message = "Starting battery level cannot exceed 100")
    @Column(precision = 5, scale = 2)
    private BigDecimal startBatteryLevel; // Percentage

    @DecimalMin(value = "0.0", message = "End battery level cannot be negative")
    @DecimalMax(value = "100.0", message = "End battery level cannot exceed 100")
    @Column(precision = 5, scale = 2)
    private BigDecimal endBatteryLevel; // Percentage

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SessionStatus status = SessionStatus.INITIATED;

    @Column
    private Long durationMinutes;

    @Column(length = 100)
    private String transactionId; // From charging network provider

    @Column(columnDefinition = "TEXT")
    private String errorMessage;

    @Column(length = 50)
    private String paymentMethod;

    @Column(length = 100)
    private String paymentStatus;

    @CreationTimestamp
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    public enum SessionStatus {
        INITIATED,
        CHARGING,
        COMPLETED,
        FAILED,
        CANCELLED
    }

    public void completeSession(BigDecimal endBattery, BigDecimal energyUsed, BigDecimal totalCost) {
        this.endTime = LocalDateTime.now();
        this.endBatteryLevel = endBattery;
        this.energyConsumed = energyUsed;
        this.cost = totalCost;
        this.status = SessionStatus.COMPLETED;
        this.durationMinutes = Duration.between(startTime, endTime).toMinutes();
    }

    public void failSession(String error) {
        this.endTime = LocalDateTime.now();
        this.status = SessionStatus.FAILED;
        this.errorMessage = error;
        this.durationMinutes = Duration.between(startTime, endTime).toMinutes();
    }

    public void cancelSession() {
        this.endTime = LocalDateTime.now();
        this.status = SessionStatus.CANCELLED;
        this.durationMinutes = Duration.between(startTime, endTime).toMinutes();
    }

    public boolean isActive() {
        return status == SessionStatus.INITIATED || status == SessionStatus.CHARGING;
    }
}
