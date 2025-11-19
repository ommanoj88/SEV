package com.evfleet.analytics.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Vehicle Report Request DTO
 * 
 * Used for requesting comprehensive vehicle PDF reports
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleReportRequest {
    
    private Long vehicleId;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime startDate;
    
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime endDate;
    
    // Report sections to include
    @Builder.Default
    private boolean includeVehicleInfo = true;
    
    @Builder.Default
    private boolean includeEventHistory = false;
    
    @Builder.Default
    private boolean includeTripHistory = true;
    
    @Builder.Default
    private boolean includeMaintenanceHistory = true;
    
    @Builder.Default
    private boolean includeChargingHistory = true;
    
    @Builder.Default
    private boolean includeAlertHistory = false;
    
    @Builder.Default
    private boolean includePerformanceMetrics = true;
    
    @Builder.Default
    private boolean includeCostAnalysis = true;
}
