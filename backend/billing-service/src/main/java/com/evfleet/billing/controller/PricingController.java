package com.evfleet.billing.controller;

import com.evfleet.billing.dto.PricingCalculationRequest;
import com.evfleet.billing.dto.PricingCalculationResponse;
import com.evfleet.billing.dto.PricingTierDto;
import com.evfleet.billing.service.PricingService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * REST Controller for Pricing Tier management and calculations
 */
@RestController
@RequestMapping("/api/billing/pricing")
@Tag(name = "Pricing", description = "Pricing tier management and calculation APIs")
public class PricingController {
    
    private final PricingService pricingService;
    
    @Autowired
    public PricingController(PricingService pricingService) {
        this.pricingService = pricingService;
    }
    
    /**
     * Get all available pricing tiers
     * 
     * @return List of pricing tiers with details
     */
    @GetMapping("/tiers")
    @Operation(summary = "Get all pricing tiers", 
               description = "Retrieve all available pricing tiers with features and pricing information")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved pricing tiers"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<List<PricingTierDto>> getAllPricingTiers() {
        try {
            List<PricingTierDto> tiers = pricingService.getAllPricingTiers();
            return ResponseEntity.ok(tiers);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Get a specific pricing tier by name
     * 
     * @param tierName the tier name (BASIC, EV_PREMIUM, ENTERPRISE)
     * @return Pricing tier details
     */
    @GetMapping("/tiers/{tierName}")
    @Operation(summary = "Get pricing tier by name", 
               description = "Retrieve details of a specific pricing tier")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved pricing tier"),
        @ApiResponse(responseCode = "404", description = "Pricing tier not found"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<PricingTierDto> getPricingTier(
            @Parameter(description = "Tier name (BASIC, EV_PREMIUM, ENTERPRISE)")
            @PathVariable String tierName) {
        try {
            PricingTierDto tier = pricingService.getPricingTier(tierName);
            if (tier == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok(tier);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
    
    /**
     * Calculate pricing based on tier, vehicle count, and billing cycle
     * 
     * @param request the pricing calculation request
     * @return Calculated pricing details
     */
    @PostMapping("/calculate")
    @Operation(summary = "Calculate pricing", 
               description = "Calculate total cost based on tier, vehicle count, and billing cycle")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully calculated pricing"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> calculatePricing(@RequestBody PricingCalculationRequest request) {
        try {
            PricingCalculationResponse response = pricingService.calculatePricing(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to calculate pricing"));
        }
    }
    
    /**
     * Get recommended pricing tier for a company
     * 
     * @param vehicleCount number of vehicles in fleet
     * @param hasEVVehicles whether fleet has EV vehicles
     * @return Recommended tier name
     */
    @GetMapping("/recommend")
    @Operation(summary = "Get recommended pricing tier", 
               description = "Get the recommended pricing tier based on fleet characteristics")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved recommendation"),
        @ApiResponse(responseCode = "400", description = "Invalid request parameters"),
        @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getRecommendedTier(
            @Parameter(description = "Number of vehicles in fleet")
            @RequestParam Integer vehicleCount,
            @Parameter(description = "Whether fleet has EV vehicles", required = false)
            @RequestParam(required = false, defaultValue = "true") Boolean hasEVVehicles) {
        try {
            if (vehicleCount == null || vehicleCount < 1) {
                return ResponseEntity.badRequest()
                    .body(Map.of("error", "Vehicle count must be at least 1"));
            }
            
            String recommendedTier = pricingService.getRecommendedTier(vehicleCount, hasEVVehicles);
            return ResponseEntity.ok(Map.of(
                "recommendedTier", recommendedTier,
                "vehicleCount", vehicleCount,
                "hasEVVehicles", hasEVVehicles
            ));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(Map.of("error", "Failed to get recommendation"));
        }
    }
}
