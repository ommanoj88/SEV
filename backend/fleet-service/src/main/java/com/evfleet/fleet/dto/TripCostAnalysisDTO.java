package com.evfleet.fleet.dto;

import com.evfleet.fleet.model.FuelType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for Trip Cost Analysis Response
 * Contains comprehensive cost and efficiency metrics for a trip
 * 
 * @since 2.0.0 (Multi-fuel support - PR 7)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Schema(description = "Trip cost analysis with fuel/energy metrics")
public class TripCostAnalysisDTO {

    @Schema(description = "Trip ID", example = "1")
    private Long tripId;

    @Schema(description = "Vehicle ID", example = "1")
    private Long vehicleId;

    @Schema(description = "Vehicle number/registration", example = "EV001")
    private String vehicleNumber;

    @Schema(description = "Fuel type of the vehicle", example = "EV")
    private FuelType fuelType;

    @Schema(description = "Trip distance in kilometers", example = "125.5")
    private Double distance;

    @Schema(description = "Trip duration in minutes", example = "180")
    private Integer durationMinutes;

    // ===== COST METRICS =====

    @Schema(description = "Total trip cost in INR", example = "180.50")
    private Double totalCost;

    @Schema(description = "Cost per kilometer in INR", example = "1.44")
    private Double costPerKm;

    @Schema(description = "Energy cost for EV (INR)", example = "80.00")
    private Double energyCost;

    @Schema(description = "Fuel cost for ICE (INR)", example = "95.00")
    private Double fuelCost;

    // ===== CONSUMPTION METRICS =====

    @Schema(description = "Energy consumed in kWh (for EV/HYBRID)", example = "10.0")
    private Double energyConsumed;

    @Schema(description = "Fuel consumed in liters (for ICE/HYBRID)", example = "8.5")
    private Double fuelConsumed;

    // ===== EFFICIENCY METRICS =====

    @Schema(description = "Energy efficiency in kWh/100km (for EV)", example = "7.97")
    private Double energyEfficiency;

    @Schema(description = "Fuel efficiency in liters/100km (for ICE)", example = "6.77")
    private Double fuelEfficiency;

    @Schema(description = "Mileage in km/liter (for ICE)", example = "14.76")
    private Double mileage;

    // ===== ENVIRONMENTAL METRICS =====

    @Schema(description = "Carbon footprint in kg CO2", example = "22.78")
    private Double carbonFootprint;

    @Schema(description = "Carbon emissions per km in g CO2/km", example = "181.52")
    private Double carbonPerKm;

    // ===== TRIP QUALITY METRICS =====

    @Schema(description = "Trip efficiency score (0-100)", example = "87.5")
    private Double efficiencyScore;

    @Schema(description = "Average speed in km/h", example = "41.83")
    private Double averageSpeed;

    @Schema(description = "Number of harsh acceleration events", example = "2")
    private Integer harshAccelerationCount;

    @Schema(description = "Number of harsh braking events", example = "1")
    private Integer harshBrakingCount;

    @Schema(description = "Number of overspeeding incidents", example = "0")
    private Integer overspeedingCount;

    @Schema(description = "Idle time in minutes", example = "15")
    private Integer idleTimeMinutes;

    // ===== BATTERY/FUEL LEVEL (for range calculation) =====

    @Schema(description = "Starting battery SOC % (for EV)", example = "85.0")
    private Double startBatterySoc;

    @Schema(description = "Ending battery SOC % (for EV)", example = "71.67")
    private Double endBatterySoc;

    @Schema(description = "Battery consumed % (for EV)", example = "13.33")
    private Double batteryConsumed;

    // ===== METADATA =====

    @Schema(description = "Start time of the trip", example = "2024-11-10T09:00:00")
    private String startTime;

    @Schema(description = "End time of the trip", example = "2024-11-10T12:00:00")
    private String endTime;

    @Schema(description = "Driver ID", example = "5")
    private Long driverId;

    @Schema(description = "Trip status", example = "COMPLETED")
    private String status;
}
