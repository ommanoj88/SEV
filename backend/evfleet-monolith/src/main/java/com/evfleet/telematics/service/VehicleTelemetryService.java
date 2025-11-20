package com.evfleet.telematics.service;

import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.VehicleRepository;
import com.evfleet.telematics.dto.VehicleTelemetryData;
import com.evfleet.telematics.provider.TelemetryProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Central service for managing multi-vendor vehicle telemetry
 * Orchestrates all telemetry providers (OEM APIs, devices, manual entry)
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Service
@Slf4j
public class VehicleTelemetryService {

    private final VehicleRepository vehicleRepository;
    private final List<TelemetryProvider> providers;

    public VehicleTelemetryService(VehicleRepository vehicleRepository, List<TelemetryProvider> providers) {
        this.vehicleRepository = vehicleRepository;
        this.providers = providers != null ? providers : new ArrayList<>();
        log.info("VehicleTelemetryService initialized with {} providers", this.providers.size());

        // Log all registered providers
        this.providers.forEach(provider ->
            log.info("  - Provider: {} ({}) - Source: {}",
                provider.getProviderName(),
                provider.getProviderId(),
                provider.getSourceType())
        );
    }

    /**
     * Fetch the latest telemetry data for a vehicle using the best available source
     * Priority: OEM API > Device > Mobile App > Manual
     */
    @Transactional(readOnly = true)
    public Optional<VehicleTelemetryData> getLatestTelemetry(Long vehicleId) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId).orElse(null);
        if (vehicle == null) {
            log.warn("Vehicle not found: {}", vehicleId);
            return Optional.empty();
        }

        return getLatestTelemetry(vehicle);
    }

    /**
     * Fetch the latest telemetry data for a vehicle using the best available source
     */
    @Transactional(readOnly = true)
    public Optional<VehicleTelemetryData> getLatestTelemetry(Vehicle vehicle) {
        log.debug("Fetching latest telemetry for vehicle: {} ({})", vehicle.getId(), vehicle.getVehicleNumber());

        // Find the appropriate provider for this vehicle
        TelemetryProvider provider = findBestProvider(vehicle);

        if (provider == null) {
            log.warn("No telemetry provider found for vehicle: {} (Source: {})",
                vehicle.getId(), vehicle.getTelemetrySource());
            return Optional.empty();
        }

        log.debug("Using provider: {} for vehicle: {}", provider.getProviderId(), vehicle.getId());

        try {
            Optional<VehicleTelemetryData> data = provider.fetchLatestData(vehicle);

            if (data.isPresent()) {
                log.info("Telemetry data fetched successfully for vehicle: {} via {}",
                    vehicle.getId(), provider.getProviderId());
            } else {
                log.warn("No telemetry data available from provider: {} for vehicle: {}",
                    provider.getProviderId(), vehicle.getId());
            }

            return data;
        } catch (Exception e) {
            log.error("Error fetching telemetry data for vehicle: {} from provider: {}",
                vehicle.getId(), provider.getProviderId(), e);
            return Optional.empty();
        }
    }

    /**
     * Update vehicle with latest telemetry data
     */
    @Transactional
    public boolean updateVehicleFromTelemetry(Long vehicleId) {
        Optional<VehicleTelemetryData> telemetryOpt = getLatestTelemetry(vehicleId);

        if (telemetryOpt.isEmpty()) {
            return false;
        }

        VehicleTelemetryData telemetry = telemetryOpt.get();
        Vehicle vehicle = vehicleRepository.findById(vehicleId).orElse(null);

        if (vehicle == null) {
            return false;
        }

        // Update vehicle fields from telemetry
        if (telemetry.getLatitude() != null && telemetry.getLongitude() != null) {
            vehicle.setLatitude(telemetry.getLatitude());
            vehicle.setLongitude(telemetry.getLongitude());
        }

        if (telemetry.getOdometer() != null) {
            vehicle.setOdometer(telemetry.getOdometer());
        }

        if (telemetry.getBatterySoc() != null) {
            vehicle.setCurrentBatterySoc(telemetry.getBatterySoc());
        }

        if (telemetry.getFuelLevel() != null) {
            vehicle.setFuelLevel(telemetry.getFuelLevel());
        }

        vehicle.setLastTelemetryUpdate(telemetry.getTimestamp());
        vehicle.setTelemetryDataQuality(telemetry.getDataQuality());

        vehicleRepository.save(vehicle);
        log.info("Vehicle {} updated with telemetry data from {}",
            vehicleId, telemetry.getProviderName());

        return true;
    }

    /**
     * Fetch historical telemetry data
     */
    @Transactional(readOnly = true)
    public List<VehicleTelemetryData> getHistoricalTelemetry(Long vehicleId, LocalDateTime start, LocalDateTime end) {
        Vehicle vehicle = vehicleRepository.findById(vehicleId).orElse(null);
        if (vehicle == null) {
            return List.of();
        }

        TelemetryProvider provider = findBestProvider(vehicle);
        if (provider == null) {
            return List.of();
        }

        try {
            return provider.fetchHistoricalData(vehicle, start, end);
        } catch (Exception e) {
            log.error("Error fetching historical telemetry for vehicle: {}", vehicleId, e);
            return List.of();
        }
    }

    /**
     * Get all supported providers
     */
    public List<TelemetryProvider> getAllProviders() {
        return List.copyOf(providers);
    }

    /**
     * Get a specific provider by ID
     */
    public Optional<TelemetryProvider> getProvider(String providerId) {
        return providers.stream()
            .filter(p -> p.getProviderId().equals(providerId))
            .findFirst();
    }

    /**
     * Find the best provider for a vehicle based on its configuration
     * Priority: OEM API > Device > Mobile App > Manual
     */
    private TelemetryProvider findBestProvider(Vehicle vehicle) {
        // If vehicle has a specific telemetry source configured
        if (vehicle.getTelemetrySource() != null) {
            switch (vehicle.getTelemetrySource()) {
                case OEM_API:
                    // Find provider matching the configured OEM API
                    if (vehicle.getOemApiProvider() != null) {
                        return providers.stream()
                            .filter(p -> p.getProviderId().equals(vehicle.getOemApiProvider()))
                            .filter(p -> p.supports(vehicle))
                            .findFirst()
                            .orElse(null);
                    }
                    break;

                case DEVICE:
                    // Find provider matching the device type
                    if (vehicle.getTelematicsDeviceType() != null) {
                        return providers.stream()
                            .filter(p -> p.getProviderId().equals(vehicle.getTelematicsDeviceType()))
                            .filter(p -> p.supports(vehicle))
                            .findFirst()
                            .orElse(null);
                    }
                    break;

                case MOBILE_APP:
                case MANUAL:
                    // Find generic mobile/manual provider
                    return providers.stream()
                        .filter(p -> p.getSourceType() == vehicle.getTelemetrySource())
                        .findFirst()
                        .orElse(null);

                default:
                    break;
            }
        }

        // Fallback: Find any provider that supports this vehicle (best priority)
        return providers.stream()
            .filter(p -> p.supports(vehicle))
            .min((p1, p2) -> Integer.compare(p1.getSourceType().ordinal(), p2.getSourceType().ordinal()))
            .orElse(null);
    }

    /**
     * Test connectivity with all providers
     */
    public void testAllProviders() {
        log.info("Testing connectivity with all telemetry providers...");

        providers.forEach(provider -> {
            try {
                boolean connected = provider.testConnection();
                if (connected) {
                    log.info("✓ Provider {} is connected", provider.getProviderName());
                } else {
                    log.warn("✗ Provider {} is not available", provider.getProviderName());
                }
            } catch (Exception e) {
                log.error("✗ Provider {} connection test failed", provider.getProviderName(), e);
            }
        });
    }
}
