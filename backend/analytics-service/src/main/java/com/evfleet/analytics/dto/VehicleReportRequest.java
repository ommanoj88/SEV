package com.evfleet.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO for Vehicle Report Request
 * Used to specify parameters for generating vehicle reports
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class VehicleReportRequest {
    
    private Long vehicleId;
    private String vehicleNumber;
    
    // Time range for report
    private LocalDateTime startDate;
    private LocalDateTime endDate;
    
    // Report sections to include
    private Boolean includeVehicleInfo = true;
    private Boolean includeEventHistory = true;
    private Boolean includeTripHistory = true;
    private Boolean includeMaintenanceHistory = true;
    private Boolean includeChargingHistory = true;
    private Boolean includeAlertHistory = true;
    private Boolean includePerformanceMetrics = true;
    private Boolean includeCostAnalysis = true;
    
    // Report format options
    private String reportFormat = "PDF"; // PDF, CSV, EXCEL
    private String reportLanguage = "en";
    
    // Company information
    private Long companyId;
    private String companyName;
    
    // User requesting the report
    private Long userId;
    private String userName;
}
