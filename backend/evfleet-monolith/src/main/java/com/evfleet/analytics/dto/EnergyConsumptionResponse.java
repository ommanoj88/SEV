package com.evfleet.analytics.dto;

import com.evfleet.analytics.model.EnergyConsumptionAnalytics;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Energy Consumption Response DTO
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EnergyConsumptionResponse {

    private Long id;
    private String date;
    private Long vehicleId;
    private String vehicleName;
    
    // Energy metrics
    private BigDecimal energyConsumed;  // kWh
    private BigDecimal distance;  // km
    private Integer chargingSessions;
    
    // Efficiency metrics
    private BigDecimal efficiency;  // kWh per 100km
    private BigDecimal bestEfficiency;
    private BigDecimal worstEfficiency;
    
    // Cost metrics
    private BigDecimal chargingCost;
    private BigDecimal costPerKwh;
    private BigDecimal costPerKm;
    
    // Advanced metrics
    private BigDecimal regenEnergy;  // kWh
    private BigDecimal regenPercentage;  // %
    private BigDecimal idleEnergyLoss;
    
    // Environmental metrics
    private BigDecimal co2Saved;  // kg

    public static EnergyConsumptionResponse fromEntity(EnergyConsumptionAnalytics analytics) {
        if (analytics == null) {
            return null;
        }

        return EnergyConsumptionResponse.builder()
                .id(analytics.getId())
                .date(analytics.getAnalysisDate() != null ? analytics.getAnalysisDate().toString() : null)
                .vehicleId(analytics.getVehicleId())
                .energyConsumed(analytics.getTotalEnergyConsumed())
                .distance(analytics.getTotalDistance())
                .chargingSessions(analytics.getTotalChargingSessions())
                .efficiency(analytics.getAverageEfficiency())
                .bestEfficiency(analytics.getBestEfficiency())
                .worstEfficiency(analytics.getWorstEfficiency())
                .chargingCost(analytics.getTotalChargingCost())
                .costPerKwh(analytics.getAverageCostPerKwh())
                .costPerKm(analytics.getCostPerKm())
                .regenEnergy(analytics.getRegenerativeEnergy())
                .regenPercentage(analytics.getRegenPercentage())
                .idleEnergyLoss(analytics.getIdleEnergyLoss())
                .co2Saved(analytics.getCo2Saved())
                .build();
    }
}
