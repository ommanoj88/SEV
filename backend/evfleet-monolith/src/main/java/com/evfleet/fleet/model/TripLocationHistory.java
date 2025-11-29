package com.evfleet.fleet.model;

import com.evfleet.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Trip Location History Entity
 * 
 * Stores the path/route of a trip as a series of location points.
 * Used for trip replay and teleportation detection.
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Entity
@Table(name = "trip_location_history", indexes = {
    @Index(name = "idx_location_trip", columnList = "trip_id"),
    @Index(name = "idx_location_timestamp", columnList = "recorded_at")
})
@Data
@EqualsAndHashCode(callSuper = false)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TripLocationHistory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "trip_id", nullable = false)
    private Long tripId;

    @Column(name = "latitude", nullable = false)
    private Double latitude;

    @Column(name = "longitude", nullable = false)
    private Double longitude;

    @Column(name = "recorded_at", nullable = false)
    private LocalDateTime recordedAt;

    /**
     * Speed in km/h at this point (calculated from previous point)
     */
    @Column(name = "speed")
    private Double speed;

    /**
     * Distance from previous point in km
     */
    @Column(name = "distance_from_previous")
    private Double distanceFromPrevious;

    /**
     * Cumulative distance from trip start in km
     */
    @Column(name = "cumulative_distance")
    private Double cumulativeDistance;

    /**
     * Whether this location update triggered a teleportation warning
     */
    @Column(name = "teleportation_warning")
    private Boolean teleportationWarning;

    /**
     * Sequence number for ordering points in the trip
     */
    @Column(name = "sequence_number", nullable = false)
    private Integer sequenceNumber;
}
