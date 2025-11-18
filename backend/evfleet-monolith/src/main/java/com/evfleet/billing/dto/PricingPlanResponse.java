package com.evfleet.billing.dto;

import com.evfleet.billing.model.PricingPlan;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * Pricing Plan Response DTO
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class PricingPlanResponse {

    private Long id;
    private String name;
    private String description;
    private BigDecimal pricePerVehicle;
    private Integer minVehicles;
    private Integer maxVehicles;
    private String billingCycle;
    private Boolean isActive;
    private Boolean hasAnalytics;
    private Boolean hasMaintenanceTracking;
    private Boolean hasDriverManagement;
    private Boolean hasChargingManagement;
    private Boolean hasPrioritySupport;
    private Boolean hasApiAccess;

    public static PricingPlanResponse fromEntity(PricingPlan plan) {
        return PricingPlanResponse.builder()
                .id(plan.getId())
                .name(plan.getName())
                .description(plan.getDescription())
                .pricePerVehicle(plan.getPricePerVehicle())
                .minVehicles(plan.getMinVehicles())
                .maxVehicles(plan.getMaxVehicles())
                .billingCycle(plan.getBillingCycle().name())
                .isActive(plan.getIsActive())
                .hasAnalytics(plan.getHasAnalytics())
                .hasMaintenanceTracking(plan.getHasMaintenanceTracking())
                .hasDriverManagement(plan.getHasDriverManagement())
                .hasChargingManagement(plan.getHasChargingManagement())
                .hasPrioritySupport(plan.getHasPrioritySupport())
                .hasApiAccess(plan.getHasApiAccess())
                .build();
    }
}
