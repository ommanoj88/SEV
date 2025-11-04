package com.evfleet.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class FleetSummaryResponse {

    private String companyId;
    private Long totalVehicles;
    private BigDecimal avgUtilization;
    private BigDecimal totalDistance;
    private LocalDateTime lastUpdated;
}
