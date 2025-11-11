package com.evfleet.fleet.dto;

import com.evfleet.fleet.model.Vehicle;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for VehicleCurrentState response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleCurrentStateResponse {
    private Long vehicleId;
    private String vehicleNumber;
    
    // Basic status
    private Vehicle.VehicleStatus status;
    private LocalDateTime lastUpdated;
    
    // Location
    private Double latitude;
    private Double longitude;
    private String locationName;
    private Double heading;
    private Double speed;
    
    // Battery/Fuel status
    private Double batterySoc;
    private Double batteryHealth;
    private Double batteryTemperature;
    private Double fuelLevel;
    
    // Odometer and usage
    private Double odometer;
    private Double totalDistance;
    private Double totalEnergyConsumed;
    private Double totalFuelConsumed;
    
    // Current activity
    private Long currentDriverId;
    private String currentDriverName;
    private Long currentTripId;
    private Boolean isCharging;
    private Boolean isInMaintenance;
    private Boolean isInTrip;
    
    // Charging info
    private Long chargingStationId;
    private String chargingStationName;
    private LocalDateTime chargingStartedAt;
    private LocalDateTime estimatedChargingCompletion;
    
    // Maintenance info
    private LocalDateTime lastMaintenanceDate;
    private LocalDateTime nextMaintenanceDueDate;
    private String maintenanceStatus;
    
    // Alert status
    private Integer activeAlertsCount;
    private Integer criticalAlertsCount;
    private LocalDateTime lastAlertTimestamp;
    
    // Connection status
    private Boolean isConnected;
    private LocalDateTime lastTelemetryReceived;
    private Integer signalStrength;
    
    // Performance metrics
    private Double averageSpeedLastTrip;
    private Double efficiencyScore;
    
    // Company reference
    private Long companyId;
    private String companyName;
    
    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
