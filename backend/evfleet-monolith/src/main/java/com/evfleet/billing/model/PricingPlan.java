package com.evfleet.billing.model;

import com.evfleet.common.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Pricing Plan Entity
 * Represents available subscription pricing plans
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Entity
@Table(name = "pricing_plans", indexes = {
    @Index(name = "idx_name", columnList = "name"),
    @Index(name = "idx_active", columnList = "is_active")
})
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingPlan extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String name;  // BASIC, PRO, ENTERPRISE

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "price_per_vehicle", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerVehicle;

    @Column(name = "min_vehicles")
    @Builder.Default
    private Integer minVehicles = 1;

    @Column(name = "max_vehicles")
    private Integer maxVehicles;

    @Enumerated(EnumType.STRING)
    @Column(name = "billing_cycle", nullable = false)
    private BillingCycle billingCycle;

    @Column(name = "is_active")
    @Builder.Default
    private Boolean isActive = true;

    // Feature flags
    @Column(name = "has_analytics")
    @Builder.Default
    private Boolean hasAnalytics = true;

    @Column(name = "has_maintenance_tracking")
    @Builder.Default
    private Boolean hasMaintenanceTracking = true;

    @Column(name = "has_driver_management")
    @Builder.Default
    private Boolean hasDriverManagement = true;

    @Column(name = "has_charging_management")
    @Builder.Default
    private Boolean hasChargingManagement = true;

    @Column(name = "has_priority_support")
    @Builder.Default
    private Boolean hasPrioritySupport = false;

    @Column(name = "has_api_access")
    @Builder.Default
    private Boolean hasApiAccess = false;

    public enum BillingCycle {
        MONTHLY,
        QUARTERLY,
        YEARLY
    }

    @PrePersist
    protected void onCreate() {
        if (isActive == null) {
            isActive = true;
        }
    }

    public BigDecimal calculatePrice(int vehicleCount) {
        if (maxVehicles != null && vehicleCount > maxVehicles) {
            throw new IllegalArgumentException("Vehicle count exceeds max limit for this plan");
        }
        if (vehicleCount < minVehicles) {
            throw new IllegalArgumentException("Vehicle count below minimum for this plan");
        }
        return pricePerVehicle.multiply(BigDecimal.valueOf(vehicleCount));
    }

    public boolean supportsVehicleCount(int count) {
        return count >= minVehicles && (maxVehicles == null || count <= maxVehicles);
    }
}
