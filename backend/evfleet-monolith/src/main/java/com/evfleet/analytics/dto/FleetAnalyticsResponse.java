package com.evfleet.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Comprehensive Fleet Analytics Response
 * Includes vehicle status breakdown, battery metrics, and utilization data
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FleetAnalyticsResponse {

    // Vehicle counts by status
    private Integer totalVehicles;
    private Integer activeVehicles;
    private Integer inactiveVehicles;
    private Integer chargingVehicles;
    private Integer maintenanceVehicles;
    private Integer inTripVehicles;

    // Battery metrics
    private Double averageBatteryLevel;  // Average SOC
    private Double averageBatteryHealth;  // Average SOH

    // Trip and distance metrics
    private Long totalTrips;
    private Double totalDistance;  // in km
    private Double totalEnergyConsumed;  // in kWh

    // Utilization metrics
    private Double utilizationRate;  // Percentage
    private Double averageUtilization;  // Hours per day

    // Summary object for compatibility
    private SummaryData summary;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryData {
        private Integer totalVehicles;
        private Long totalTrips;
        private Double totalDistance;
        private Double averageUtilization;
    }
}
