package com.evfleet.analytics.dto;

import com.evfleet.analytics.model.FleetSummary;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Fleet Summary Response DTO
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FleetSummaryResponse {

    private Long id;
    private Long companyId;
    private LocalDate summaryDate;
    private Integer totalVehicles;
    private Integer activeVehicles;
    private Integer totalTrips;
    private Double totalDistance;
    private BigDecimal totalEnergyConsumed;
    private BigDecimal totalCost;
    private BigDecimal maintenanceCost;
    private BigDecimal fuelCost;
    private BigDecimal energyCost;
    private Double avgDistancePerVehicle;
    private BigDecimal avgCostPerKm;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static FleetSummaryResponse fromEntity(FleetSummary summary) {
        Double avgDistancePerVehicle = null;
        if (summary.getTotalVehicles() != null && summary.getTotalVehicles() > 0 && summary.getTotalDistance() != null) {
            avgDistancePerVehicle = summary.getTotalDistance() / summary.getTotalVehicles();
        }

        BigDecimal avgCostPerKm = null;
        if (summary.getTotalDistance() != null && summary.getTotalDistance() > 0 && summary.getTotalCost() != null) {
            avgCostPerKm = summary.getTotalCost().divide(
                    BigDecimal.valueOf(summary.getTotalDistance()),
                    2,
                    java.math.RoundingMode.HALF_UP
            );
        }

        return FleetSummaryResponse.builder()
                .id(summary.getId())
                .companyId(summary.getCompanyId())
                .summaryDate(summary.getSummaryDate())
                .totalVehicles(summary.getTotalVehicles())
                .activeVehicles(summary.getActiveVehicles())
                .totalTrips(summary.getTotalTrips())
                .totalDistance(summary.getTotalDistance())
                .totalEnergyConsumed(summary.getTotalEnergyConsumed())
                .totalCost(summary.getTotalCost())
                .maintenanceCost(summary.getMaintenanceCost())
                .fuelCost(summary.getFuelCost())
                .energyCost(summary.getEnergyCost())
                .avgDistancePerVehicle(avgDistancePerVehicle)
                .avgCostPerKm(avgCostPerKm)
                .createdAt(summary.getCreatedAt())
                .updatedAt(summary.getUpdatedAt())
                .build();
    }
}
