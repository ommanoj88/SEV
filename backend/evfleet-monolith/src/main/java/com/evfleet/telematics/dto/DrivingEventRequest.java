package com.evfleet.telematics.dto;

import com.evfleet.telematics.model.DrivingEvent;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO for creating a new driving event.
 * 
 * Used to receive telematics data from vehicle sensors
 * and OBD-II devices.
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrivingEventRequest {

    private Long tripId;  // Optional - can be null if not part of a trip

    @NotNull(message = "Driver ID is required")
    private Long driverId;

    @NotNull(message = "Vehicle ID is required")
    private Long vehicleId;

    @NotNull(message = "Company ID is required")
    private Long companyId;

    @NotNull(message = "Event type is required")
    private DrivingEvent.EventType eventType;

    private LocalDateTime eventTime;  // Defaults to now if null

    private Double latitude;

    private Double longitude;

    @Positive(message = "Speed must be positive")
    private Double speed;  // in km/h

    private BigDecimal gForce;  // G-force measurement

    private DrivingEvent.Severity severity;  // If null, will be calculated

    @Positive(message = "Duration must be positive")
    private Integer duration;  // in seconds (for idling events)

    @Positive(message = "Speed limit must be positive")
    private Double speedLimit;  // in km/h (for speeding events)

    private String description;  // Optional description
}
