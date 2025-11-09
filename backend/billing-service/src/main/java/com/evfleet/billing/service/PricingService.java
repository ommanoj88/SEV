package com.evfleet.billing.service;

import com.evfleet.billing.dto.PricingCalculationRequest;
import com.evfleet.billing.dto.PricingCalculationResponse;
import com.evfleet.billing.dto.PricingTierDto;
import com.evfleet.billing.entity.PricingTier;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Service for managing pricing tiers and calculations
 */
@Service
public class PricingService {
    
    /**
     * Get all available pricing tiers with their details
     * 
     * @return List of PricingTierDto
     */
    public List<PricingTierDto> getAllPricingTiers() {
        List<PricingTierDto> tiers = new ArrayList<>();
        
        // BASIC tier
        PricingTierDto basic = new PricingTierDto();
        basic.setTierName("BASIC");
        basic.setDisplayName("Basic");
        basic.setPricePerVehiclePerMonth(299.0);
        basic.setDescription("General fleet management - Perfect for all vehicle types");
        basic.setFeatures(Arrays.asList(
            "Real-time GPS tracking",
            "Fleet monitoring dashboard",
            "Driver management",
            "Trip history and reports",
            "Basic analytics",
            "Email support"
        ));
        basic.setRecommended(false);
        basic.setBillingCycles(Arrays.asList("MONTHLY", "QUARTERLY", "ANNUAL"));
        tiers.add(basic);
        
        // EV_PREMIUM tier
        PricingTierDto evPremium = new PricingTierDto();
        evPremium.setTierName("EV_PREMIUM");
        evPremium.setDisplayName("EV Premium");
        evPremium.setPricePerVehiclePerMonth(699.0);
        evPremium.setDescription("All features + EV-specific optimization");
        evPremium.setFeatures(Arrays.asList(
            "Everything in Basic",
            "Advanced battery health monitoring",
            "Smart charging optimization",
            "Charging station management",
            "Predictive maintenance",
            "Advanced EV analytics",
            "Carbon footprint tracking",
            "Priority support"
        ));
        evPremium.setRecommended(true);
        evPremium.setBillingCycles(Arrays.asList("MONTHLY", "QUARTERLY", "ANNUAL"));
        tiers.add(evPremium);
        
        // ENTERPRISE tier
        PricingTierDto enterprise = new PricingTierDto();
        enterprise.setTierName("ENTERPRISE");
        enterprise.setDisplayName("Enterprise");
        enterprise.setPricePerVehiclePerMonth(999.0);
        enterprise.setDescription("Multi-depot, custom integrations, dedicated support");
        enterprise.setFeatures(Arrays.asList(
            "Everything in EV Premium",
            "Multi-depot management",
            "Custom API integrations",
            "White-label options",
            "Advanced role-based access control",
            "Custom reports and dashboards",
            "Dedicated account manager",
            "24/7 priority support",
            "SLA guarantee (99.9% uptime)"
        ));
        enterprise.setRecommended(false);
        enterprise.setBillingCycles(Arrays.asList("MONTHLY", "QUARTERLY", "ANNUAL"));
        tiers.add(enterprise);
        
        return tiers;
    }
    
    /**
     * Get a specific pricing tier by name
     * 
     * @param tierName the tier name
     * @return PricingTierDto or null if not found
     */
    public PricingTierDto getPricingTier(String tierName) {
        return getAllPricingTiers().stream()
            .filter(t -> t.getTierName().equalsIgnoreCase(tierName))
            .findFirst()
            .orElse(null);
    }
    
    /**
     * Calculate pricing based on tier, vehicle count, and billing cycle
     * 
     * @param request the pricing calculation request
     * @return PricingCalculationResponse with calculated costs
     */
    public PricingCalculationResponse calculatePricing(PricingCalculationRequest request) {
        // Validate tier
        PricingTier tier;
        try {
            tier = PricingTier.fromString(request.getTier());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid pricing tier: " + request.getTier());
        }
        
        // Validate vehicle count
        if (request.getVehicleCount() == null || request.getVehicleCount() < 1) {
            throw new IllegalArgumentException("Vehicle count must be at least 1");
        }
        
        // Validate billing cycle
        String billingCycle = request.getBillingCycle() != null ? 
            request.getBillingCycle().toUpperCase() : "MONTHLY";
        if (!Arrays.asList("MONTHLY", "QUARTERLY", "ANNUAL").contains(billingCycle)) {
            throw new IllegalArgumentException("Invalid billing cycle: " + request.getBillingCycle());
        }
        
        // Calculate base monthly cost
        BigDecimal basePrice = BigDecimal.valueOf(tier.getPricePerVehiclePerMonth());
        BigDecimal monthlyCost = basePrice.multiply(BigDecimal.valueOf(request.getVehicleCount()));
        
        // Calculate discount based on billing cycle
        double discountPercentage = 0.0;
        int months = 1;
        
        switch (billingCycle) {
            case "QUARTERLY":
                discountPercentage = 5.0; // 5% discount for quarterly
                months = 3;
                break;
            case "ANNUAL":
                discountPercentage = 10.0; // 10% discount for annual
                months = 12;
                break;
            default:
                months = 1;
                break;
        }
        
        // Calculate total cost
        BigDecimal totalBeforeDiscount = monthlyCost.multiply(BigDecimal.valueOf(months));
        BigDecimal discountAmount = totalBeforeDiscount
            .multiply(BigDecimal.valueOf(discountPercentage))
            .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal totalCost = totalBeforeDiscount.subtract(discountAmount);
        
        // Build response
        PricingCalculationResponse response = new PricingCalculationResponse();
        response.setTier(tier.getTierName());
        response.setVehicleCount(request.getVehicleCount());
        response.setBillingCycle(billingCycle);
        response.setBasePrice(basePrice.setScale(2, RoundingMode.HALF_UP));
        response.setMonthlyCost(monthlyCost.setScale(2, RoundingMode.HALF_UP));
        response.setTotalCost(totalCost.setScale(2, RoundingMode.HALF_UP));
        response.setDiscountPercentage(discountPercentage);
        response.setDiscountAmount(discountAmount.setScale(2, RoundingMode.HALF_UP));
        
        return response;
    }
    
    /**
     * Get recommended tier for a company based on their fleet characteristics
     * In future, this can be enhanced to consider fuel types when available
     * 
     * @param vehicleCount number of vehicles in fleet
     * @param hasEVVehicles whether fleet has EV vehicles
     * @return recommended tier name
     */
    public String getRecommendedTier(Integer vehicleCount, Boolean hasEVVehicles) {
        // For now, recommend EV_PREMIUM if they have EV vehicles and more than 5 vehicles
        // BASIC for smaller fleets
        // ENTERPRISE for large fleets (50+ vehicles)
        
        if (vehicleCount >= 50) {
            return PricingTier.ENTERPRISE.getTierName();
        } else if (hasEVVehicles && vehicleCount >= 5) {
            return PricingTier.EV_PREMIUM.getTierName();
        } else {
            return PricingTier.BASIC.getTierName();
        }
    }
}
