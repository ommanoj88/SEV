package com.evfleet.telematics.scheduler;

import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.VehicleRepository;
import com.evfleet.telematics.dto.VehicleTelemetryData;
import com.evfleet.telematics.model.TelemetrySnapshot;
import com.evfleet.telematics.provider.TelemetryProvider;
import com.evfleet.telematics.repository.TelemetrySnapshotRepository;
import com.evfleet.telematics.service.TelemetryAlertService;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Background scheduler for syncing telemetry data from all connected vehicles.
 * Fetches data from OEM APIs and telematics devices at configurable intervals.
 * 
 * Features:
 * - Per-vehicle error isolation (one failure doesn't affect others)
 * - Exponential backoff for failing vehicles
 * - Metrics tracking for monitoring
 * - Configurable sync interval
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Component
@Slf4j
public class TelemetrySyncScheduler {

    private final VehicleRepository vehicleRepository;
    private final TelemetrySnapshotRepository snapshotRepository;
    private final List<TelemetryProvider> telemetryProviders;
    private final MeterRegistry meterRegistry;
    private final TelemetryAlertService alertService;

    // Configuration
    @Value("${telematics.sync.enabled:true}")
    private boolean syncEnabled;

    @Value("${telematics.sync.batch-size:50}")
    private int batchSize;

    @Value("${telematics.sync.max-retries:3}")
    private int maxRetries;

    @Value("${telematics.sync.backoff-multiplier:2}")
    private int backoffMultiplier;

    // Metrics
    private Counter syncSuccessCounter;
    private Counter syncFailureCounter;
    private Counter snapshotsSavedCounter;
    private Timer syncDurationTimer;

    // Per-vehicle tracking for error isolation and backoff
    private final Map<Long, VehicleSyncState> vehicleSyncStates = new ConcurrentHashMap<>();

    // Overall sync statistics
    private final AtomicLong totalSyncRuns = new AtomicLong(0);
    private final AtomicLong totalVehiclesSynced = new AtomicLong(0);
    private final AtomicLong totalVehiclesFailed = new AtomicLong(0);

    public TelemetrySyncScheduler(
            VehicleRepository vehicleRepository,
            TelemetrySnapshotRepository snapshotRepository,
            List<TelemetryProvider> telemetryProviders,
            MeterRegistry meterRegistry,
            TelemetryAlertService alertService) {
        this.vehicleRepository = vehicleRepository;
        this.snapshotRepository = snapshotRepository;
        this.telemetryProviders = telemetryProviders;
        this.meterRegistry = meterRegistry;
        this.alertService = alertService;
    }

    @PostConstruct
    public void init() {
        // Initialize metrics
        syncSuccessCounter = Counter.builder("telematics.sync.success")
            .description("Number of successful vehicle telemetry syncs")
            .register(meterRegistry);

        syncFailureCounter = Counter.builder("telematics.sync.failure")
            .description("Number of failed vehicle telemetry syncs")
            .register(meterRegistry);

        snapshotsSavedCounter = Counter.builder("telematics.snapshots.saved")
            .description("Total number of telemetry snapshots saved")
            .register(meterRegistry);

        syncDurationTimer = Timer.builder("telematics.sync.duration")
            .description("Duration of telemetry sync operations")
            .register(meterRegistry);

        log.info("TelemetrySyncScheduler initialized. Sync enabled: {}, batch size: {}", 
            syncEnabled, batchSize);
    }

    /**
     * Main sync job - runs every 60 seconds by default
     * Fetches telemetry for all vehicles with DEVICE or OEM_API source
     */
    @Scheduled(fixedRateString = "${telematics.sync.interval-ms:60000}")
    @Transactional
    public void syncAllVehicleTelemetry() {
        if (!syncEnabled) {
            log.debug("Telemetry sync is disabled");
            return;
        }

        long runNumber = totalSyncRuns.incrementAndGet();
        log.info("Starting telemetry sync run #{}", runNumber);

        long startTime = System.currentTimeMillis();
        int successCount = 0;
        int failureCount = 0;
        int skippedCount = 0;

        try {
            // Get all vehicles with telematics integration
            List<Vehicle> vehicles = getVehiclesForSync();
            log.info("Found {} vehicles with telematics integration", vehicles.size());

            for (Vehicle vehicle : vehicles) {
                try {
                    // Check if vehicle should be skipped (backoff)
                    VehicleSyncState state = vehicleSyncStates.computeIfAbsent(
                        vehicle.getId(), 
                        id -> new VehicleSyncState()
                    );

                    if (state.shouldSkip()) {
                        skippedCount++;
                        log.debug("Skipping vehicle {} due to backoff (retry in {} seconds)", 
                            vehicle.getId(), state.getSecondsUntilNextRetry());
                        continue;
                    }

                    // Sync this vehicle
                    boolean success = syncVehicle(vehicle);

                    if (success) {
                        successCount++;
                        state.recordSuccess();
                        syncSuccessCounter.increment();
                        totalVehiclesSynced.incrementAndGet();
                    } else {
                        failureCount++;
                        state.recordFailure(maxRetries, backoffMultiplier);
                        syncFailureCounter.increment();
                        totalVehiclesFailed.incrementAndGet();
                    }

                } catch (Exception e) {
                    // Per-vehicle error isolation - log and continue
                    failureCount++;
                    log.error("Error syncing vehicle {}: {}", vehicle.getId(), e.getMessage());
                    
                    VehicleSyncState state = vehicleSyncStates.get(vehicle.getId());
                    if (state != null) {
                        state.recordFailure(maxRetries, backoffMultiplier);
                    }
                    syncFailureCounter.increment();
                }
            }

        } catch (Exception e) {
            log.error("Critical error in telemetry sync run #{}: {}", runNumber, e.getMessage(), e);
        } finally {
            long duration = System.currentTimeMillis() - startTime;
            syncDurationTimer.record(duration, TimeUnit.MILLISECONDS);

            log.info("Telemetry sync run #{} completed in {}ms. Success: {}, Failed: {}, Skipped: {}",
                runNumber, duration, successCount, failureCount, skippedCount);
        }
    }

    /**
     * Sync telemetry for a single vehicle
     * @return true if sync was successful
     */
    private boolean syncVehicle(Vehicle vehicle) {
        log.debug("Syncing telemetry for vehicle {} ({})", vehicle.getId(), vehicle.getVehicleNumber());

        // Find a provider that supports this vehicle
        TelemetryProvider provider = findProviderForVehicle(vehicle);
        if (provider == null) {
            log.warn("No telemetry provider found for vehicle {} with source {}", 
                vehicle.getId(), vehicle.getTelemetrySource());
            return false;
        }

        // Fetch latest telemetry data
        Optional<VehicleTelemetryData> dataOpt = provider.fetchLatestData(vehicle);
        if (dataOpt.isEmpty()) {
            log.warn("No telemetry data returned for vehicle {} from provider {}", 
                vehicle.getId(), provider.getProviderId());
            return false;
        }

        VehicleTelemetryData data = dataOpt.get();

        // Create and save snapshot
        TelemetrySnapshot snapshot = createSnapshot(vehicle, data, provider);
        snapshotRepository.save(snapshot);
        snapshotsSavedCounter.increment();

        // Update vehicle with latest telemetry
        updateVehicleFromTelemetry(vehicle, data);

        // Process telemetry for alerts (battery, speed, etc.)
        alertService.processAndGenerateAlerts(vehicle, data);

        log.debug("Successfully synced telemetry for vehicle {} - SOC: {}%, Speed: {} km/h", 
            vehicle.getId(), data.getBatterySoc(), data.getSpeed());

        return true;
    }

    /**
     * Find a provider that supports the given vehicle
     */
    private TelemetryProvider findProviderForVehicle(Vehicle vehicle) {
        for (TelemetryProvider provider : telemetryProviders) {
            if (provider.supports(vehicle)) {
                return provider;
            }
        }
        return null;
    }

    /**
     * Create a TelemetrySnapshot from provider data
     */
    private TelemetrySnapshot createSnapshot(Vehicle vehicle, VehicleTelemetryData data, TelemetryProvider provider) {
        return TelemetrySnapshot.builder()
            .vehicleId(vehicle.getId())
            .companyId(vehicle.getCompanyId())
            .deviceId(getDeviceId(vehicle))
            .source(vehicle.getTelemetrySource())
            .providerName(provider.getProviderName())
            .timestamp(data.getTimestamp() != null ? data.getTimestamp() : LocalDateTime.now())
            .dataQuality(determineDataQuality(data))
            // Location
            .latitude(data.getLatitude())
            .longitude(data.getLongitude())
            .altitude(data.getAltitude())
            .heading(data.getHeading())
            .speed(data.getSpeed())
            .satellites(data.getSatellites())
            // Odometer
            .odometer(data.getOdometer())
            .tripDistance(data.getTripDistance())
            // EV Battery
            .batterySoc(data.getBatterySoc())
            .batterySoh(data.getBatterySoh())
            .batteryVoltage(data.getBatteryVoltage())
            .batteryCurrent(data.getBatteryCurrent())
            .batteryTemperature(data.getBatteryTemperature())
            .isCharging(data.getIsCharging())
            .estimatedRange(data.getEstimatedRange())
            .chargingStatus(data.getChargingStatus())
            // Fuel (for hybrid/ICE)
            .fuelLevel(data.getFuelLevel())
            .fuelPercentage(data.getFuelPercentage())
            // Engine & Diagnostics
            .ignitionOn(data.getIgnitionOn())
            .isMoving(data.getIsMoving())
            .engineRpm(data.getEngineRpm())
            .vehicleStatus(data.getVehicleStatus())
            .checkEngineLight(data.getCheckEngineLight())
            // Driver Behavior
            .accelerationX(data.getAccelerationX())
            .accelerationY(data.getAccelerationY())
            .accelerationZ(data.getAccelerationZ())
            // Connectivity
            .signalStrength(data.getSignalStrength())
            .isEstimated(data.getIsEstimated())
            .build();
    }

    /**
     * Update Vehicle entity with latest telemetry data
     */
    @Transactional
    private void updateVehicleFromTelemetry(Vehicle vehicle, VehicleTelemetryData data) {
        // Update location
        if (data.getLatitude() != null && data.getLongitude() != null) {
            vehicle.setLatitude(data.getLatitude());
            vehicle.setLongitude(data.getLongitude());
        }

        // Update battery/fuel levels
        if (data.getBatterySoc() != null) {
            vehicle.setCurrentBatterySoc(data.getBatterySoc());
        }
        if (data.getFuelLevel() != null) {
            vehicle.setFuelLevel(data.getFuelLevel());
        }

        // Update odometer
        if (data.getOdometer() != null) {
            vehicle.setOdometer(data.getOdometer());
        }

        // Update timestamps
        vehicle.setLastTelemetryUpdate(LocalDateTime.now());
        vehicle.setLastUpdated(LocalDateTime.now());
        vehicle.setTelemetryDataQuality(determineDataQuality(data));

        vehicleRepository.save(vehicle);
    }

    /**
     * Get device identifier for the vehicle
     */
    private String getDeviceId(Vehicle vehicle) {
        if (vehicle.getTelemetrySource() == Vehicle.TelemetrySource.DEVICE) {
            return vehicle.getTelematicsDeviceImei();
        } else if (vehicle.getTelemetrySource() == Vehicle.TelemetrySource.OEM_API) {
            return vehicle.getOemVehicleId();
        }
        return null;
    }

    /**
     * Determine data quality based on telemetry data freshness
     */
    private Vehicle.TelemetryDataQuality determineDataQuality(VehicleTelemetryData data) {
        if (data.getTimestamp() == null) {
            return Vehicle.TelemetryDataQuality.UNKNOWN;
        }

        long ageMinutes = java.time.Duration.between(data.getTimestamp(), LocalDateTime.now()).toMinutes();

        if (ageMinutes < 5) {
            return Vehicle.TelemetryDataQuality.REAL_TIME;
        } else if (ageMinutes < 30) {
            return Vehicle.TelemetryDataQuality.RECENT;
        } else {
            return Vehicle.TelemetryDataQuality.STALE;
        }
    }

    /**
     * Get all vehicles that have telematics integration enabled
     */
    private List<Vehicle> getVehiclesForSync() {
        return vehicleRepository.findAll().stream()
            .filter(v -> v.getTelemetrySource() == Vehicle.TelemetrySource.DEVICE 
                      || v.getTelemetrySource() == Vehicle.TelemetrySource.OEM_API)
            .filter(v -> v.getStatus() != Vehicle.VehicleStatus.INACTIVE)
            .toList();
    }

    // ===== MANUAL SYNC METHODS =====

    /**
     * Manually trigger sync for a specific vehicle
     */
    @Transactional
    public boolean syncVehicleById(Long vehicleId) {
        Optional<Vehicle> vehicleOpt = vehicleRepository.findById(vehicleId);
        if (vehicleOpt.isEmpty()) {
            log.warn("Vehicle {} not found for manual sync", vehicleId);
            return false;
        }

        Vehicle vehicle = vehicleOpt.get();
        if (vehicle.getTelemetrySource() == null || 
            vehicle.getTelemetrySource() == Vehicle.TelemetrySource.NONE ||
            vehicle.getTelemetrySource() == Vehicle.TelemetrySource.MANUAL) {
            log.warn("Vehicle {} does not have telematics integration", vehicleId);
            return false;
        }

        return syncVehicle(vehicle);
    }

    /**
     * Reset backoff state for a vehicle (e.g., after fixing connectivity issues)
     */
    public void resetVehicleBackoff(Long vehicleId) {
        vehicleSyncStates.remove(vehicleId);
        log.info("Reset backoff state for vehicle {}", vehicleId);
    }

    /**
     * Get sync statistics
     */
    public SyncStatistics getStatistics() {
        return new SyncStatistics(
            totalSyncRuns.get(),
            totalVehiclesSynced.get(),
            totalVehiclesFailed.get(),
            vehicleSyncStates.size(),
            (long) vehicleSyncStates.values().stream().filter(VehicleSyncState::isInBackoff).count()
        );
    }

    // ===== INNER CLASSES =====

    /**
     * Tracks sync state for individual vehicles (for backoff logic)
     */
    private static class VehicleSyncState {
        private int consecutiveFailures = 0;
        private LocalDateTime lastAttempt = null;
        private LocalDateTime nextAllowedAttempt = null;

        public void recordSuccess() {
            this.consecutiveFailures = 0;
            this.lastAttempt = LocalDateTime.now();
            this.nextAllowedAttempt = null;
        }

        public void recordFailure(int maxRetries, int backoffMultiplier) {
            this.consecutiveFailures++;
            this.lastAttempt = LocalDateTime.now();

            // Calculate backoff: 1 min, 2 min, 4 min, 8 min, max 30 min
            int backoffMinutes = (int) Math.min(30, Math.pow(backoffMultiplier, Math.min(consecutiveFailures, maxRetries)));
            this.nextAllowedAttempt = LocalDateTime.now().plusMinutes(backoffMinutes);
        }

        public boolean shouldSkip() {
            return nextAllowedAttempt != null && LocalDateTime.now().isBefore(nextAllowedAttempt);
        }

        public boolean isInBackoff() {
            return shouldSkip();
        }

        public long getSecondsUntilNextRetry() {
            if (nextAllowedAttempt == null) return 0;
            return java.time.Duration.between(LocalDateTime.now(), nextAllowedAttempt).getSeconds();
        }
    }

    /**
     * Statistics about sync operations
     */
    public record SyncStatistics(
        long totalRuns,
        long totalVehiclesSynced,
        long totalVehiclesFailed,
        long trackedVehicles,
        long vehiclesInBackoff
    ) {}
}
