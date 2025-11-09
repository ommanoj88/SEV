package com.evfleet.fleet.service;

import com.evfleet.fleet.model.FeatureToggle;
import com.evfleet.fleet.model.FuelType;

import java.util.List;

/**
 * Service interface for Feature Toggle operations.
 * Manages feature availability based on vehicle fuel types.
 * 
 * @since 2.0.0 (Feature flag system)
 */
public interface FeatureToggleService {

    /**
     * Check if a feature is available for a specific fuel type
     * 
     * @param featureName the name of the feature to check
     * @param fuelType the fuel type
     * @return true if the feature is enabled and supports the fuel type
     */
    boolean isFeatureAvailable(String featureName, FuelType fuelType);

    /**
     * Check if a feature is available for a specific vehicle
     * 
     * @param featureName the name of the feature to check
     * @param vehicleId the vehicle ID
     * @return true if the feature is available for the vehicle's fuel type
     */
    boolean isFeatureAvailableForVehicle(String featureName, Long vehicleId);

    /**
     * Get all features available for a specific fuel type
     * 
     * @param fuelType the fuel type
     * @return list of available feature names
     */
    List<String> getAvailableFeatures(FuelType fuelType);

    /**
     * Get all features available for a specific vehicle
     * 
     * @param vehicleId the vehicle ID
     * @return list of available feature names
     */
    List<String> getAvailableFeaturesForVehicle(Long vehicleId);

    /**
     * Get a feature toggle by name
     * 
     * @param featureName the name of the feature
     * @return the feature toggle
     */
    FeatureToggle getFeatureToggle(String featureName);

    /**
     * Get all feature toggles
     * 
     * @return list of all feature toggles
     */
    List<FeatureToggle> getAllFeatureToggles();

    /**
     * Get all enabled feature toggles
     * 
     * @return list of enabled feature toggles
     */
    List<FeatureToggle> getEnabledFeatureToggles();

    /**
     * Create or update a feature toggle
     * 
     * @param featureToggle the feature toggle to save
     * @return the saved feature toggle
     */
    FeatureToggle saveFeatureToggle(FeatureToggle featureToggle);

    /**
     * Enable a feature toggle
     * 
     * @param featureName the name of the feature to enable
     */
    void enableFeature(String featureName);

    /**
     * Disable a feature toggle
     * 
     * @param featureName the name of the feature to disable
     */
    void disableFeature(String featureName);
}
