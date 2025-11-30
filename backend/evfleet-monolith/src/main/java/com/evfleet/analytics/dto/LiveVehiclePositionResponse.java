package com.evfleet.analytics.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Live Vehicle Position Response DTO
 * 
 * Lightweight DTO for real-time vehicle tracking.
 * Contains only essential position and status data for map rendering.
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LiveVehiclePositionResponse {

    private Long companyId;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime timestamp;

    private int totalVehicles;
    private int trackableVehicles;

    private List<VehiclePosition> vehicles;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VehiclePosition {
        private Long vehicleId;
        private String vehicleNumber;
        private String licensePlate;
        
        // GPS Coordinates
        private BigDecimal latitude;
        private BigDecimal longitude;
        private BigDecimal heading; // Direction in degrees (0-360)
        private BigDecimal speedKmh;
        
        // Status
        private String status; // DRIVING, PARKED, CHARGING, IDLE, OFFLINE
        private String statusColor; // For map pin colors
        
        // Battery (for EVs)
        private BigDecimal batteryPercent;
        private String batteryStatus; // CRITICAL, LOW, NORMAL, HIGH, FULL
        
        // Current trip
        private Long currentTripId;
        private String currentDriverName;
        private String destination;
        
        // Last update
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime lastUpdateTime;
        private long secondsSinceUpdate;
        
        // Quick info
        private BigDecimal todayDistanceKm;
        private String fuelType; // EV, ICE, HYBRID, CNG, LPG
    }

    // ========== CLUSTER DATA (for zoomed out view) ==========

    private List<VehicleCluster> clusters;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class VehicleCluster {
        private BigDecimal latitude;
        private BigDecimal longitude;
        private int vehicleCount;
        private String areaName;
    }

    // ========== GEOFENCE ALERTS ==========

    private List<GeofenceAlert> geofenceAlerts;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class GeofenceAlert {
        private Long vehicleId;
        private String vehicleNumber;
        private String alertType; // ENTERED, EXITED, SPEEDING
        private String geofenceName;
        @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
        private LocalDateTime alertTime;
    }
}
