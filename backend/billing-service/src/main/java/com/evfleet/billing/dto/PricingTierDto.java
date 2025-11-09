package com.evfleet.billing.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * DTO for Pricing Tier information
 * Used in API responses to provide tier details to frontend
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PricingTierDto {
    
    /**
     * Tier name (BASIC, EV_PREMIUM, ENTERPRISE)
     */
    private String tierName;
    
    /**
     * Display name for the tier
     */
    private String displayName;
    
    /**
     * Price per vehicle per month in INR
     */
    private Double pricePerVehiclePerMonth;
    
    /**
     * Description of the tier
     */
    private String description;
    
    /**
     * List of features included in this tier
     */
    private List<String> features;
    
    /**
     * Whether this is the recommended tier
     */
    private Boolean recommended;
    
    /**
     * Billing cycle options (MONTHLY, QUARTERLY, ANNUAL)
     */
    private List<String> billingCycles;
}
