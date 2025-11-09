package com.evfleet.fleet.controller;

import com.evfleet.fleet.model.FeatureToggle;
import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.service.FeatureToggleService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST Controller for Feature Toggle Management.
 * Provides endpoints to query and manage feature availability based on fuel types.
 * 
 * @since 2.0.0 (Feature flag system)
 */
@RestController
@RequestMapping("/api/features")
@Tag(name = "Feature Toggle", description = "Feature toggle management APIs")
@Slf4j
public class FeatureToggleController {

    private final FeatureToggleService featureToggleService;

    @Autowired
    public FeatureToggleController(FeatureToggleService featureToggleService) {
        this.featureToggleService = featureToggleService;
    }

    /**
     * Check if a feature is available for a specific fuel type
     */
    @Operation(summary = "Check feature availability for fuel type", 
               description = "Check if a specific feature is available for a given fuel type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Feature availability checked successfully",
                    content = @Content(schema = @Schema(implementation = Boolean.class))),
        @ApiResponse(responseCode = "400", description = "Invalid fuel type"),
        @ApiResponse(responseCode = "404", description = "Feature not found")
    })
    @GetMapping("/check")
    public ResponseEntity<Boolean> checkFeatureAvailability(
            @Parameter(description = "Name of the feature", required = true)
            @RequestParam String featureName,
            @Parameter(description = "Fuel type (EV, ICE, HYBRID)", required = true)
            @RequestParam FuelType fuelType) {
        
        log.info("Checking if feature '{}' is available for fuel type '{}'", featureName, fuelType);
        boolean available = featureToggleService.isFeatureAvailable(featureName, fuelType);
        return ResponseEntity.ok(available);
    }

    /**
     * Check if a feature is available for a specific vehicle
     */
    @Operation(summary = "Check feature availability for vehicle", 
               description = "Check if a specific feature is available for a given vehicle")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Feature availability checked successfully",
                    content = @Content(schema = @Schema(implementation = Boolean.class))),
        @ApiResponse(responseCode = "404", description = "Vehicle or feature not found")
    })
    @GetMapping("/check/vehicle/{vehicleId}")
    public ResponseEntity<Boolean> checkFeatureAvailabilityForVehicle(
            @Parameter(description = "ID of the vehicle", required = true)
            @PathVariable Long vehicleId,
            @Parameter(description = "Name of the feature", required = true)
            @RequestParam String featureName) {
        
        log.info("Checking if feature '{}' is available for vehicle ID {}", featureName, vehicleId);
        boolean available = featureToggleService.isFeatureAvailableForVehicle(featureName, vehicleId);
        return ResponseEntity.ok(available);
    }

    /**
     * Get all available features for a specific fuel type
     */
    @Operation(summary = "Get available features for fuel type", 
               description = "Get list of all features available for a specific fuel type")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Features retrieved successfully",
                    content = @Content(schema = @Schema(implementation = List.class))),
        @ApiResponse(responseCode = "400", description = "Invalid fuel type")
    })
    @GetMapping("/available")
    public ResponseEntity<List<String>> getAvailableFeatures(
            @Parameter(description = "Fuel type (EV, ICE, HYBRID)", required = true)
            @RequestParam FuelType fuelType) {
        
        log.info("Getting available features for fuel type '{}'", fuelType);
        List<String> features = featureToggleService.getAvailableFeatures(fuelType);
        return ResponseEntity.ok(features);
    }

    /**
     * Get all available features for a specific vehicle
     */
    @Operation(summary = "Get available features for vehicle", 
               description = "Get list of all features available for a specific vehicle")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Features retrieved successfully",
                    content = @Content(schema = @Schema(implementation = List.class))),
        @ApiResponse(responseCode = "404", description = "Vehicle not found")
    })
    @GetMapping("/available/vehicle/{vehicleId}")
    public ResponseEntity<List<String>> getAvailableFeaturesForVehicle(
            @Parameter(description = "ID of the vehicle", required = true)
            @PathVariable Long vehicleId) {
        
        log.info("Getting available features for vehicle ID {}", vehicleId);
        List<String> features = featureToggleService.getAvailableFeaturesForVehicle(vehicleId);
        return ResponseEntity.ok(features);
    }

    /**
     * Get all feature toggles
     */
    @Operation(summary = "Get all feature toggles", 
               description = "Get complete list of all feature toggles in the system")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Feature toggles retrieved successfully",
                    content = @Content(schema = @Schema(implementation = List.class)))
    })
    @GetMapping
    public ResponseEntity<List<FeatureToggle>> getAllFeatureToggles() {
        log.info("Getting all feature toggles");
        List<FeatureToggle> features = featureToggleService.getAllFeatureToggles();
        return ResponseEntity.ok(features);
    }

    /**
     * Get only enabled feature toggles
     */
    @Operation(summary = "Get enabled feature toggles", 
               description = "Get list of all enabled feature toggles")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Enabled feature toggles retrieved successfully",
                    content = @Content(schema = @Schema(implementation = List.class)))
    })
    @GetMapping("/enabled")
    public ResponseEntity<List<FeatureToggle>> getEnabledFeatureToggles() {
        log.info("Getting enabled feature toggles");
        List<FeatureToggle> features = featureToggleService.getEnabledFeatureToggles();
        return ResponseEntity.ok(features);
    }

    /**
     * Get a specific feature toggle by name
     */
    @Operation(summary = "Get feature toggle by name", 
               description = "Get details of a specific feature toggle")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Feature toggle retrieved successfully",
                    content = @Content(schema = @Schema(implementation = FeatureToggle.class))),
        @ApiResponse(responseCode = "404", description = "Feature toggle not found")
    })
    @GetMapping("/{featureName}")
    public ResponseEntity<FeatureToggle> getFeatureToggle(
            @Parameter(description = "Name of the feature", required = true)
            @PathVariable String featureName) {
        
        log.info("Getting feature toggle: {}", featureName);
        FeatureToggle feature = featureToggleService.getFeatureToggle(featureName);
        return ResponseEntity.ok(feature);
    }

    /**
     * Enable a feature toggle
     */
    @Operation(summary = "Enable feature", 
               description = "Enable a specific feature toggle")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Feature enabled successfully"),
        @ApiResponse(responseCode = "404", description = "Feature toggle not found")
    })
    @PutMapping("/{featureName}/enable")
    public ResponseEntity<Void> enableFeature(
            @Parameter(description = "Name of the feature to enable", required = true)
            @PathVariable String featureName) {
        
        log.info("Enabling feature: {}", featureName);
        featureToggleService.enableFeature(featureName);
        return ResponseEntity.ok().build();
    }

    /**
     * Disable a feature toggle
     */
    @Operation(summary = "Disable feature", 
               description = "Disable a specific feature toggle")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Feature disabled successfully"),
        @ApiResponse(responseCode = "404", description = "Feature toggle not found")
    })
    @PutMapping("/{featureName}/disable")
    public ResponseEntity<Void> disableFeature(
            @Parameter(description = "Name of the feature to disable", required = true)
            @PathVariable String featureName) {
        
        log.info("Disabling feature: {}", featureName);
        featureToggleService.disableFeature(featureName);
        return ResponseEntity.ok().build();
    }

    /**
     * Create or update a feature toggle
     */
    @Operation(summary = "Save feature toggle", 
               description = "Create or update a feature toggle")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Feature toggle updated successfully",
                    content = @Content(schema = @Schema(implementation = FeatureToggle.class))),
        @ApiResponse(responseCode = "201", description = "Feature toggle created successfully",
                    content = @Content(schema = @Schema(implementation = FeatureToggle.class))),
        @ApiResponse(responseCode = "400", description = "Invalid feature toggle data")
    })
    @PostMapping
    public ResponseEntity<FeatureToggle> saveFeatureToggle(
            @Parameter(description = "Feature toggle to save", required = true)
            @RequestBody FeatureToggle featureToggle) {
        
        log.info("Saving feature toggle: {}", featureToggle.getFeatureName());
        FeatureToggle saved = featureToggleService.saveFeatureToggle(featureToggle);
        
        // Return 201 if new, 200 if updated
        HttpStatus status = featureToggle.getId() == null ? HttpStatus.CREATED : HttpStatus.OK;
        return ResponseEntity.status(status).body(saved);
    }
}
