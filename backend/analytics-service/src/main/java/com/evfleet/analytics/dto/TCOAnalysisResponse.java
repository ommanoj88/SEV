package com.evfleet.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TCOAnalysisResponse {

    private String vehicleId;
    private BigDecimal totalEnergyCost;
    private BigDecimal totalMaintenanceCost;
    private BigDecimal totalOwnershipCost;
    private BigDecimal avgCostPerKm;
    private Long analysisPeriods;
}
