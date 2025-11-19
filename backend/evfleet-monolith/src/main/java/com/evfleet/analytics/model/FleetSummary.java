package com.evfleet.analytics.model;

import com.evfleet.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;

@Entity
@Table(name = "fleet_summaries")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FleetSummary extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "company_id", nullable = false)
    private Long companyId;

    @Column(name = "summary_date", nullable = false)
    private LocalDate summaryDate;

    @Column(name = "total_vehicles")
    private Integer totalVehicles;

    @Column(name = "active_vehicles")
    private Integer activeVehicles;

    @Column(name = "total_trips")
    private Integer totalTrips;

    @Column(name = "total_distance")
    private Double totalDistance;

    @Column(name = "total_energy_consumed")
    private BigDecimal totalEnergyConsumed;

    @Column(name = "total_cost")
    private BigDecimal totalCost;

    // New fields for maintenance cost tracking
    @Column(name = "maintenance_cost")
    @Builder.Default
    private BigDecimal maintenanceCost = BigDecimal.ZERO;

    @Column(name = "fuel_cost")
    @Builder.Default
    private BigDecimal fuelCost = BigDecimal.ZERO;

    @Column(name = "energy_cost")
    @Builder.Default
    private BigDecimal energyCost = BigDecimal.ZERO;
}
