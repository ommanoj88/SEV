package com.evfleet.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for pricing calculation request
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PricingCalculationRequest {
    
    /**
     * Pricing tier (BASIC, EV_PREMIUM, ENTERPRISE)
     */
    private String tier;
    
    /**
     * Number of vehicles
     */
    private Integer vehicleCount;
    
    /**
     * Billing cycle (MONTHLY, QUARTERLY, ANNUAL)
     */
    private String billingCycle;
}
