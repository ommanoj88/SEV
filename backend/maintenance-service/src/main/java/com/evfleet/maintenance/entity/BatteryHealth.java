package com.evfleet.maintenance.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "battery_health")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BatteryHealth {

    @Id
    @Column(name = "id")
    private String id;

    @Column(name = "vehicle_id", nullable = false)
    private String vehicleId;

    @Column(name = "soh", nullable = false, precision = 5, scale = 2)
    private BigDecimal soh;

    @Column(name = "soc", nullable = false, precision = 5, scale = 2)
    private BigDecimal soc;

    @Column(name = "cycle_count")
    private Integer cycleCount;

    @Column(name = "temperature", precision = 5, scale = 2)
    private BigDecimal temperature;

    @Column(name = "degradation_rate", precision = 5, scale = 4)
    private BigDecimal degradationRate;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @Column(name = "created_at")
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
        if (cycleCount == null) {
            cycleCount = 0;
        }
    }
}
