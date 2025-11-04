package com.evfleet.driver.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "driver_behavior")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DriverBehavior {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "driver_id", nullable = false)
    private String driverId;

    @Column(name = "trip_id")
    private String tripId;

    @Column(name = "harsh_braking")
    private Integer harshBraking = 0;

    @Column(name = "harsh_acceleration")
    private Integer harshAcceleration = 0;

    @Column(name = "overspeeding")
    private Integer overspeeding = 0;

    @Column(name = "idle_time")
    private Integer idleTime = 0;

    @Column(name = "score")
    private Integer score;

    @Column(name = "timestamp")
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        timestamp = LocalDateTime.now();
    }
}
