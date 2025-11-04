package com.evfleet.analytics.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "utilization_reports")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UtilizationReport {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "vehicle_id", nullable = false)
    private String vehicleId;

    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDateTime periodEnd;

    @Column(name = "active_hours", precision = 10, scale = 2)
    private BigDecimal activeHours;

    @Column(name = "idle_hours", precision = 10, scale = 2)
    private BigDecimal idleHours;

    @Column(name = "charging_hours", precision = 10, scale = 2)
    private BigDecimal chargingHours;

    @Column(name = "utilization_percentage", precision = 5, scale = 2)
    private BigDecimal utilizationPercentage;

    @Column(name = "distance_traveled", precision = 10, scale = 2)
    private BigDecimal distanceTraveled;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
