package com.evfleet.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Vehicle Utilization Report Response
 * Contains utilization metrics for a vehicle
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VehicleUtilizationResponse {

    private Long vehicleId;
    private String vehicleName;
    private String vehicleNumber;

    // Utilization metrics
    private Double utilizationRate;  // Percentage (0-100)
    private Double activeHours;
    private Integer trips;
    private Double distance;  // in km
    private Double efficiency;  // km/kWh or km/L
    private String status;  // "optimal", "underutilized", "severely-underutilized"

    // Additional hours breakdown (optional for detailed view)
    private Double hoursIdle;
    private Double hoursCharging;
}
