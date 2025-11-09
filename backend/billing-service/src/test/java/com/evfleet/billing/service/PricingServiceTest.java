package com.evfleet.billing.service;

import com.evfleet.billing.dto.PricingCalculationRequest;
import com.evfleet.billing.dto.PricingCalculationResponse;
import com.evfleet.billing.dto.PricingTierDto;
import com.evfleet.billing.entity.PricingTier;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for PricingService
 */
class PricingServiceTest {
    
    private PricingService pricingService;
    
    @BeforeEach
    void setUp() {
        pricingService = new PricingService();
    }
    
    @Test
    void testGetAllPricingTiers_ShouldReturnThreeTiers() {
        // When
        List<PricingTierDto> tiers = pricingService.getAllPricingTiers();
        
        // Then
        assertNotNull(tiers);
        assertEquals(3, tiers.size());
        
        // Verify tier names
        assertTrue(tiers.stream().anyMatch(t -> "BASIC".equals(t.getTierName())));
        assertTrue(tiers.stream().anyMatch(t -> "EV_PREMIUM".equals(t.getTierName())));
        assertTrue(tiers.stream().anyMatch(t -> "ENTERPRISE".equals(t.getTierName())));
    }
    
    @Test
    void testGetAllPricingTiers_ShouldHaveCorrectPricing() {
        // When
        List<PricingTierDto> tiers = pricingService.getAllPricingTiers();
        
        // Then
        PricingTierDto basic = tiers.stream()
            .filter(t -> "BASIC".equals(t.getTierName()))
            .findFirst()
            .orElse(null);
        assertNotNull(basic);
        assertEquals(299.0, basic.getPricePerVehiclePerMonth());
        
        PricingTierDto evPremium = tiers.stream()
            .filter(t -> "EV_PREMIUM".equals(t.getTierName()))
            .findFirst()
            .orElse(null);
        assertNotNull(evPremium);
        assertEquals(699.0, evPremium.getPricePerVehiclePerMonth());
        assertTrue(evPremium.getRecommended());
        
        PricingTierDto enterprise = tiers.stream()
            .filter(t -> "ENTERPRISE".equals(t.getTierName()))
            .findFirst()
            .orElse(null);
        assertNotNull(enterprise);
        assertEquals(999.0, enterprise.getPricePerVehiclePerMonth());
    }
    
    @Test
    void testGetPricingTier_ValidTierName_ShouldReturnTier() {
        // When
        PricingTierDto tier = pricingService.getPricingTier("BASIC");
        
        // Then
        assertNotNull(tier);
        assertEquals("BASIC", tier.getTierName());
        assertEquals(299.0, tier.getPricePerVehiclePerMonth());
    }
    
    @Test
    void testGetPricingTier_InvalidTierName_ShouldReturnNull() {
        // When
        PricingTierDto tier = pricingService.getPricingTier("INVALID");
        
        // Then
        assertNull(tier);
    }
    
    @Test
    void testCalculatePricing_MonthlyBasic_ShouldCalculateCorrectly() {
        // Given
        PricingCalculationRequest request = new PricingCalculationRequest();
        request.setTier("BASIC");
        request.setVehicleCount(10);
        request.setBillingCycle("MONTHLY");
        
        // When
        PricingCalculationResponse response = pricingService.calculatePricing(request);
        
        // Then
        assertNotNull(response);
        assertEquals("BASIC", response.getTier());
        assertEquals(10, response.getVehicleCount());
        assertEquals("MONTHLY", response.getBillingCycle());
        assertEquals(new BigDecimal("299.00"), response.getBasePrice());
        assertEquals(new BigDecimal("2990.00"), response.getMonthlyCost());
        assertEquals(new BigDecimal("2990.00"), response.getTotalCost());
        assertEquals(0.0, response.getDiscountPercentage());
        assertEquals(new BigDecimal("0.00"), response.getDiscountAmount());
    }
    
    @Test
    void testCalculatePricing_QuarterlyWithDiscount_ShouldApply5PercentDiscount() {
        // Given
        PricingCalculationRequest request = new PricingCalculationRequest();
        request.setTier("EV_PREMIUM");
        request.setVehicleCount(5);
        request.setBillingCycle("QUARTERLY");
        
        // When
        PricingCalculationResponse response = pricingService.calculatePricing(request);
        
        // Then
        assertNotNull(response);
        assertEquals("EV_PREMIUM", response.getTier());
        assertEquals(5.0, response.getDiscountPercentage());
        
        // Monthly cost: 699 * 5 = 3495
        // Quarterly before discount: 3495 * 3 = 10485
        // Discount: 10485 * 0.05 = 524.25
        // Total: 10485 - 524.25 = 9960.75
        assertEquals(new BigDecimal("3495.00"), response.getMonthlyCost());
        assertEquals(new BigDecimal("524.25"), response.getDiscountAmount());
        assertEquals(new BigDecimal("9960.75"), response.getTotalCost());
    }
    
    @Test
    void testCalculatePricing_AnnualWithDiscount_ShouldApply10PercentDiscount() {
        // Given
        PricingCalculationRequest request = new PricingCalculationRequest();
        request.setTier("ENTERPRISE");
        request.setVehicleCount(100);
        request.setBillingCycle("ANNUAL");
        
        // When
        PricingCalculationResponse response = pricingService.calculatePricing(request);
        
        // Then
        assertNotNull(response);
        assertEquals("ENTERPRISE", response.getTier());
        assertEquals(10.0, response.getDiscountPercentage());
        
        // Monthly cost: 999 * 100 = 99900
        // Annual before discount: 99900 * 12 = 1198800
        // Discount: 1198800 * 0.10 = 119880
        // Total: 1198800 - 119880 = 1078920
        assertEquals(new BigDecimal("99900.00"), response.getMonthlyCost());
        assertEquals(new BigDecimal("119880.00"), response.getDiscountAmount());
        assertEquals(new BigDecimal("1078920.00"), response.getTotalCost());
    }
    
    @Test
    void testCalculatePricing_InvalidTier_ShouldThrowException() {
        // Given
        PricingCalculationRequest request = new PricingCalculationRequest();
        request.setTier("INVALID_TIER");
        request.setVehicleCount(10);
        request.setBillingCycle("MONTHLY");
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            pricingService.calculatePricing(request);
        });
    }
    
    @Test
    void testCalculatePricing_InvalidVehicleCount_ShouldThrowException() {
        // Given
        PricingCalculationRequest request = new PricingCalculationRequest();
        request.setTier("BASIC");
        request.setVehicleCount(0);
        request.setBillingCycle("MONTHLY");
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            pricingService.calculatePricing(request);
        });
    }
    
    @Test
    void testCalculatePricing_InvalidBillingCycle_ShouldThrowException() {
        // Given
        PricingCalculationRequest request = new PricingCalculationRequest();
        request.setTier("BASIC");
        request.setVehicleCount(10);
        request.setBillingCycle("INVALID_CYCLE");
        
        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            pricingService.calculatePricing(request);
        });
    }
    
    @Test
    void testGetRecommendedTier_SmallFleet_ShouldRecommendBasic() {
        // When
        String tier = pricingService.getRecommendedTier(3, true);
        
        // Then
        assertEquals("BASIC", tier);
    }
    
    @Test
    void testGetRecommendedTier_MediumFleetWithEV_ShouldRecommendEVPremium() {
        // When
        String tier = pricingService.getRecommendedTier(10, true);
        
        // Then
        assertEquals("EV_PREMIUM", tier);
    }
    
    @Test
    void testGetRecommendedTier_LargeFleet_ShouldRecommendEnterprise() {
        // When
        String tier = pricingService.getRecommendedTier(60, true);
        
        // Then
        assertEquals("ENTERPRISE", tier);
    }
    
    @Test
    void testGetRecommendedTier_MediumFleetNoEV_ShouldRecommendBasic() {
        // When
        String tier = pricingService.getRecommendedTier(10, false);
        
        // Then
        assertEquals("BASIC", tier);
    }
}
