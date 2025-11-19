package com.evfleet.telematics.dto;

import com.evfleet.telematics.model.DrivingEvent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * Response DTO for driving events
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DrivingEventResponse {

    private Long id;
    private Long tripId;
    private Long driverId;
    private Long vehicleId;
    private Long companyId;
    private String eventType;
    private LocalDateTime eventTime;
    private Double latitude;
    private Double longitude;
    private Double speed;
    private BigDecimal gForce;
    private String severity;
    private Integer duration;
    private Double speedLimit;
    private String description;
    private LocalDateTime createdAt;

    public static DrivingEventResponse fromEntity(DrivingEvent event) {
        return DrivingEventResponse.builder()
                .id(event.getId())
                .tripId(event.getTripId())
                .driverId(event.getDriverId())
                .vehicleId(event.getVehicleId())
                .companyId(event.getCompanyId())
                .eventType(event.getEventType().name())
                .eventTime(event.getEventTime())
                .latitude(event.getLatitude())
                .longitude(event.getLongitude())
                .speed(event.getSpeed())
                .gForce(event.getGForce())
                .severity(event.getSeverity() != null ? event.getSeverity().name() : null)
                .duration(event.getDuration())
                .speedLimit(event.getSpeedLimit())
                .description(event.getDescription())
                .createdAt(event.getCreatedAt())
                .build();
    }
}
