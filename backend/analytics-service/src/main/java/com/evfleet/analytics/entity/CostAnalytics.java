package com.evfleet.analytics.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "cost_analytics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CostAnalytics {

    @Id
    @Column(name = "id", nullable = false)
    private String id;

    @Column(name = "company_id", nullable = false)
    private String companyId;

    @Column(name = "vehicle_id")
    private String vehicleId;

    @Column(name = "period_start", nullable = false)
    private LocalDateTime periodStart;

    @Column(name = "period_end", nullable = false)
    private LocalDateTime periodEnd;

    @Column(name = "energy_cost", precision = 12, scale = 2)
    private BigDecimal energyCost;

    @Column(name = "maintenance_cost", precision = 12, scale = 2)
    private BigDecimal maintenanceCost;

    @Column(name = "total_cost", precision = 12, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "cost_per_km", precision = 10, scale = 4)
    private BigDecimal costPerKm;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
