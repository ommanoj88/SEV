package com.evfleet.analytics.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "tco_analysis")
@Immutable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class TCOAnalysis {

    @Id
    @Column(name = "vehicle_id", nullable = false)
    private String vehicleId;

    @Column(name = "total_energy_cost", precision = 12, scale = 2)
    private BigDecimal totalEnergyCost;

    @Column(name = "total_maintenance_cost", precision = 12, scale = 2)
    private BigDecimal totalMaintenanceCost;

    @Column(name = "total_ownership_cost", precision = 12, scale = 2)
    private BigDecimal totalOwnershipCost;

    @Column(name = "avg_cost_per_km", precision = 10, scale = 4)
    private BigDecimal avgCostPerKm;

    @Column(name = "analysis_periods")
    private Long analysisPeriods;
}
