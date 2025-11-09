package com.evfleet.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

/**
 * DTO for pricing calculation response
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PricingCalculationResponse {
    
    /**
     * Pricing tier
     */
    private String tier;
    
    /**
     * Number of vehicles
     */
    private Integer vehicleCount;
    
    /**
     * Billing cycle
     */
    private String billingCycle;
    
    /**
     * Base price per vehicle per month
     */
    private BigDecimal basePrice;
    
    /**
     * Total monthly cost
     */
    private BigDecimal monthlyCost;
    
    /**
     * Total cost for the billing cycle
     */
    private BigDecimal totalCost;
    
    /**
     * Discount percentage applied (if any)
     */
    private Double discountPercentage;
    
    /**
     * Discount amount
     */
    private BigDecimal discountAmount;
}
