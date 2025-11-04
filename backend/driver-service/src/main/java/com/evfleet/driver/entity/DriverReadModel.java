package com.evfleet.driver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "driver_read_model")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverReadModel {

    @Id
    @Column(name = "driver_id", nullable = false)
    private String driverId;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "email")
    private String email;

    @Column(name = "rating", precision = 3, scale = 2)
    private BigDecimal rating;

    @Column(name = "total_trips")
    private Integer totalTrips;

    @Column(name = "average_score", precision = 5, scale = 2)
    private BigDecimal averageScore;

    @Column(name = "total_distance", precision = 10, scale = 2)
    private BigDecimal totalDistance;

    @Column(name = "total_hours", precision = 10, scale = 2)
    private BigDecimal totalHours;

    @Column(name = "current_vehicle_id")
    private String currentVehicleId;

    @Column(name = "last_trip_date")
    private LocalDateTime lastTripDate;

    @Column(name = "performance_rank")
    private Integer performanceRank;

    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @PrePersist
    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
