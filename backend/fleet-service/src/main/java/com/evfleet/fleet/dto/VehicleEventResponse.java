package com.evfleet.fleet.dto;

import com.evfleet.fleet.model.VehicleEvent;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for VehicleEvent response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleEventResponse {
    private Long id;
    private Long vehicleId;
    private String vehicleNumber;
    private VehicleEvent.EventType eventType;
    private String eventSubtype;
    private LocalDateTime eventTimestamp;
    private VehicleEvent.EventSeverity severity;
    private String eventData;
    
    // Location information
    private Double latitude;
    private Double longitude;
    private String locationName;
    
    // Related entities
    private Long driverId;
    private String driverName;
    private Long tripId;
    private Long maintenanceId;
    private Long chargingSessionId;
    
    // Event metrics
    private Double batterySoc;
    private Double fuelLevel;
    private Double odometer;
    private Double speed;
    
    // Metadata
    private String source;
    private Long userId;
    private String userName;
    private Long companyId;
    
    // Additional context
    private String description;
    private String notes;
    
    // Audit trail
    private LocalDateTime createdAt;
}
