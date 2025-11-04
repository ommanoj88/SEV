package com.evfleet.analytics.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Immutable;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "fleet_summary")
@Immutable
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FleetSummary {

    @Id
    @Column(name = "company_id", nullable = false)
    private String companyId;

    @Column(name = "total_vehicles")
    private Long totalVehicles;

    @Column(name = "avg_utilization", precision = 5, scale = 2)
    private BigDecimal avgUtilization;

    @Column(name = "total_distance", precision = 10, scale = 2)
    private BigDecimal totalDistance;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated;
}
