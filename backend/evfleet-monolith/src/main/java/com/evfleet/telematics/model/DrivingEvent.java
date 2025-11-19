package com.evfleet.telematics.model;

import com.evfleet.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Driving Event Entity
 * 
 * Stores telematics events from vehicle sensors and OBD-II data.
 * Used for driver behavior analysis and safety scoring.
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Entity
@Table(name = "driving_events", indexes = {
    @Index(name = "idx_event_trip", columnList = "trip_id"),
    @Index(name = "idx_event_driver", columnList = "driver_id"),
    @Index(name = "idx_event_vehicle", columnList = "vehicle_id"),
    @Index(name = "idx_event_type", columnList = "event_type"),
    @Index(name = "idx_event_time", columnList = "event_time")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrivingEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trip_id")
    private Long tripId;

    @Column(name = "driver_id", nullable = false)
    private Long driverId;

    @Column(name = "vehicle_id", nullable = false)
    private Long vehicleId;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Enumerated(EnumType.STRING)
    @Column(name = "event_type", nullable = false)
    private EventType eventType;

    @Column(name = "event_time", nullable = false)
    private LocalDateTime eventTime;

    @Column(name = "latitude")
    private Double latitude;

    @Column(name = "longitude")
    private Double longitude;

    @Column(name = "speed")
    private Double speed; // in km/h

    @Column(name = "g_force", precision = 10, scale = 2)
    private BigDecimal gForce; // G-force for acceleration/braking events

    @Column(name = "severity")
    @Enumerated(EnumType.STRING)
    private Severity severity;

    @Column(name = "duration")
    private Integer duration; // in seconds (for idling events)

    @Column(name = "speed_limit")
    private Double speedLimit; // in km/h (for speeding events)

    @Column(length = 500)
    private String description;

    public enum EventType {
        HARSH_BRAKING,
        HARSH_ACCELERATION,
        HARSH_CORNERING,
        SPEEDING,
        IDLING,
        RAPID_LANE_CHANGE,
        DISTRACTED_DRIVING
    }

    public enum Severity {
        LOW,
        MEDIUM,
        HIGH,
        CRITICAL
    }
}
