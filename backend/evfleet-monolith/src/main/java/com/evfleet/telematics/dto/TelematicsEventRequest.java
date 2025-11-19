package com.evfleet.telematics.dto;

import com.evfleet.telematics.model.DrivingEvent;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Request DTO for ingesting telematics events
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TelematicsEventRequest {

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @NotNull(message = "Event type is required")
    private String type; // HARSH_BRAKING, SPEEDING, etc.

    @NotNull(message = "Event timestamp is required")
    private LocalDateTime timestamp;

    private Double latitude;
    private Double longitude;
    private Double speed; // in km/h
    private BigDecimal gForce; // G-force for acceleration/braking
    private String severity; // LOW, MEDIUM, HIGH, CRITICAL
    private Integer duration; // in seconds (for idling)
    private Double speedLimit; // in km/h (for speeding events)
    private String description;

    // Optional: Include trip context if event occurs during a trip
    private Long tripId;
}
