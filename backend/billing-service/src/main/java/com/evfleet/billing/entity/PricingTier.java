package com.evfleet.billing.entity;

/**
 * Enum representing the three-tier pricing model for the EV Fleet Management System
 * 
 * BASIC: General fleet management (₹299/vehicle/month)
 * - Suitable for all vehicle types
 * - Core fleet management features
 * - ICE focus when ICE vehicles are added
 * 
 * EV_PREMIUM: All features + EV-specific optimization (₹699/vehicle/month)
 * - All BASIC features plus EV-specific optimization
 * - Advanced battery management
 * - Charging optimization
 * - EV-specific analytics
 * 
 * ENTERPRISE: Multi-depot, custom integrations, dedicated support (₹999/vehicle/month)
 * - All EV_PREMIUM features
 * - Multi-depot management
 * - Custom integrations
 * - Dedicated support
 * - SLA guarantees
 */
public enum PricingTier {
    /**
     * BASIC tier - ₹299/vehicle/month
     * General fleet management suitable for all vehicles
     */
    BASIC("BASIC", 299.0, "General fleet management"),
    
    /**
     * EV_PREMIUM tier - ₹699/vehicle/month
     * All features plus EV-specific optimization
     */
    EV_PREMIUM("EV_PREMIUM", 699.0, "All features + EV-specific optimization"),
    
    /**
     * ENTERPRISE tier - ₹999/vehicle/month
     * Multi-depot, custom integrations, dedicated support
     */
    ENTERPRISE("ENTERPRISE", 999.0, "Multi-depot with custom integrations");
    
    private final String tierName;
    private final Double pricePerVehiclePerMonth;
    private final String description;
    
    PricingTier(String tierName, Double pricePerVehiclePerMonth, String description) {
        this.tierName = tierName;
        this.pricePerVehiclePerMonth = pricePerVehiclePerMonth;
        this.description = description;
    }
    
    public String getTierName() {
        return tierName;
    }
    
    public Double getPricePerVehiclePerMonth() {
        return pricePerVehiclePerMonth;
    }
    
    public String getDescription() {
        return description;
    }
    
    /**
     * Get pricing tier from string name
     * @param tierName the tier name
     * @return PricingTier enum value
     * @throws IllegalArgumentException if tier name is invalid
     */
    public static PricingTier fromString(String tierName) {
        for (PricingTier tier : PricingTier.values()) {
            if (tier.tierName.equalsIgnoreCase(tierName)) {
                return tier;
            }
        }
        throw new IllegalArgumentException("Invalid pricing tier: " + tierName);
    }
}
