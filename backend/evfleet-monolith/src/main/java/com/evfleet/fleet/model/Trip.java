package com.evfleet.fleet.model;

import com.evfleet.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Trip Entity
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Entity
@Table(name = "trips", indexes = {
    @Index(name = "idx_trip_vehicle", columnList = "vehicle_id"),
    @Index(name = "idx_trip_driver", columnList = "driver_id"),
    @Index(name = "idx_trip_status", columnList = "status"),
    @Index(name = "idx_trip_start_time", columnList = "start_time")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Trip extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "driver_id")
    private Long driverId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time")
    private LocalDateTime endTime;

    @Column(name = "start_latitude")
    private Double startLatitude;

    @Column(name = "start_longitude")
    private Double startLongitude;

    @Column(name = "end_latitude")
    private Double endLatitude;

    @Column(name = "end_longitude")
    private Double endLongitude;

    @Column(name = "distance")
    private Double distance; // in km

    @Column(name = "duration")
    private Long duration; // in seconds

    @Column(name = "energy_consumed")
    private BigDecimal energyConsumed; // in kWh for EV

    @Column(name = "fuel_consumed")
    private BigDecimal fuelConsumed; // in liters for ICE

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TripStatus status;

    @Column(length = 500)
    private String notes;

    public enum TripStatus {
        SCHEDULED,
        IN_PROGRESS,
        COMPLETED,
        CANCELLED
    }

    public void complete(Double endLat, Double endLon, Double distance, Long duration) {
        this.endTime = LocalDateTime.now();
        this.endLatitude = endLat;
        this.endLongitude = endLon;
        this.distance = distance;
        this.duration = duration;
        this.status = TripStatus.COMPLETED;
    }
}
