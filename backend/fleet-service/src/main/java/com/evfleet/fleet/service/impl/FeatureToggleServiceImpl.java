package com.evfleet.fleet.service.impl;

import com.evfleet.fleet.exception.ResourceNotFoundException;
import com.evfleet.fleet.model.FeatureToggle;
import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.FeatureToggleRepository;
import com.evfleet.fleet.repository.VehicleRepository;
import com.evfleet.fleet.service.FeatureToggleService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementation of FeatureToggleService.
 * Manages feature availability logic based on vehicle fuel types.
 * 
 * @since 2.0.0 (Feature flag system)
 */
@Service
@Slf4j
@Transactional
public class FeatureToggleServiceImpl implements FeatureToggleService {

    private final FeatureToggleRepository featureToggleRepository;
    private final VehicleRepository vehicleRepository;

    @Autowired
    public FeatureToggleServiceImpl(
            FeatureToggleRepository featureToggleRepository,
            VehicleRepository vehicleRepository) {
        this.featureToggleRepository = featureToggleRepository;
        this.vehicleRepository = vehicleRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFeatureAvailable(String featureName, FuelType fuelType) {
        log.debug("Checking if feature '{}' is available for fuel type '{}'", featureName, fuelType);
        return featureToggleRepository.isFeatureAvailableForFuelType(featureName, fuelType);
    }

    @Override
    @Transactional(readOnly = true)
    public boolean isFeatureAvailableForVehicle(String featureName, Long vehicleId) {
        log.debug("Checking if feature '{}' is available for vehicle ID {}", featureName, vehicleId);
        
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));
        
        FuelType fuelType = vehicle.getFuelType();
        if (fuelType == null) {
            log.warn("Vehicle {} has no fuel type set, defaulting to EV", vehicleId);
            fuelType = FuelType.EV; // Default to EV for backward compatibility
        }
        
        return isFeatureAvailable(featureName, fuelType);
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAvailableFeatures(FuelType fuelType) {
        log.debug("Getting available features for fuel type '{}'", fuelType);
        
        List<FeatureToggle> features = featureToggleRepository.findByFuelType(fuelType);
        return features.stream()
                .map(FeatureToggle::getFeatureName)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<String> getAvailableFeaturesForVehicle(Long vehicleId) {
        log.debug("Getting available features for vehicle ID {}", vehicleId);
        
        Vehicle vehicle = vehicleRepository.findById(vehicleId)
                .orElseThrow(() -> new ResourceNotFoundException("Vehicle not found with id: " + vehicleId));
        
        FuelType fuelType = vehicle.getFuelType();
        if (fuelType == null) {
            log.warn("Vehicle {} has no fuel type set, defaulting to EV", vehicleId);
            fuelType = FuelType.EV;
        }
        
        return getAvailableFeatures(fuelType);
    }

    @Override
    @Transactional(readOnly = true)
    public FeatureToggle getFeatureToggle(String featureName) {
        log.debug("Getting feature toggle for '{}'", featureName);
        return featureToggleRepository.findByFeatureName(featureName)
                .orElseThrow(() -> new ResourceNotFoundException("Feature toggle not found: " + featureName));
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeatureToggle> getAllFeatureToggles() {
        log.debug("Getting all feature toggles");
        return featureToggleRepository.findAll();
    }

    @Override
    @Transactional(readOnly = true)
    public List<FeatureToggle> getEnabledFeatureToggles() {
        log.debug("Getting all enabled feature toggles");
        return featureToggleRepository.findByEnabledTrue();
    }

    @Override
    public FeatureToggle saveFeatureToggle(FeatureToggle featureToggle) {
        log.info("Saving feature toggle: {}", featureToggle.getFeatureName());
        return featureToggleRepository.save(featureToggle);
    }

    @Override
    public void enableFeature(String featureName) {
        log.info("Enabling feature: {}", featureName);
        
        FeatureToggle featureToggle = getFeatureToggle(featureName);
        featureToggle.setEnabled(true);
        featureToggleRepository.save(featureToggle);
    }

    @Override
    public void disableFeature(String featureName) {
        log.info("Disabling feature: {}", featureName);
        
        FeatureToggle featureToggle = getFeatureToggle(featureName);
        featureToggle.setEnabled(false);
        featureToggleRepository.save(featureToggle);
    }
}
