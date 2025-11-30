package com.evfleet.telematics.service;

import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.VehicleRepository;
import com.evfleet.notification.model.Notification;
import com.evfleet.notification.repository.NotificationRepository;
import com.evfleet.telematics.dto.VehicleTelemetryData;
import com.evfleet.telematics.model.TelemetryAlert;
import com.evfleet.telematics.model.TelemetryAlert.AlertPriority;
import com.evfleet.telematics.model.TelemetryAlert.AlertStatus;
import com.evfleet.telematics.model.TelemetryAlert.AlertType;
import com.evfleet.telematics.repository.TelemetryAlertRepository;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.annotation.PostConstruct;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Telemetry Alert Service
 * 
 * Generates alerts from telemetry data based on configurable rules.
 * Implements deduplication to prevent alert fatigue.
 * 
 * Alert Rules:
 * - LOW_BATTERY: Battery SOC < 20% (MEDIUM priority)
 * - CRITICAL_BATTERY: Battery SOC < 10% (HIGH priority)
 * - EXCESSIVE_SPEED: Speed > speed limit (MEDIUM priority)
 * - CONNECTION_LOST: No update in 1 hour (LOW priority)
 * - GEOFENCE_EXIT: Vehicle left designated area (HIGH priority)
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Service
@Slf4j
@Transactional
public class TelemetryAlertService {

    private final TelemetryAlertRepository alertRepository;
    private final VehicleRepository vehicleRepository;
    private final NotificationRepository notificationRepository;
    private final MeterRegistry meterRegistry;

    // ===== CONFIGURABLE THRESHOLDS =====

    @Value("${telematics.alerts.battery.low-threshold:20}")
    private int lowBatteryThreshold;

    @Value("${telematics.alerts.battery.critical-threshold:10}")
    private int criticalBatteryThreshold;

    @Value("${telematics.alerts.speed.max-kmh:120}")
    private int maxSpeedKmh;

    @Value("${telematics.alerts.connection.timeout-minutes:60}")
    private int connectionTimeoutMinutes;

    @Value("${telematics.alerts.fuel.low-threshold:15}")
    private int lowFuelThreshold;

    @Value("${telematics.alerts.cooldown-minutes:30}")
    private int alertCooldownMinutes;

    @Value("${telematics.alerts.enabled:true}")
    private boolean alertsEnabled;

    // ===== METRICS =====
    private Counter alertsGeneratedCounter;
    private Counter alertsDeduplicatedCounter;
    private Counter notificationsSentCounter;

    public TelemetryAlertService(
            TelemetryAlertRepository alertRepository,
            VehicleRepository vehicleRepository,
            NotificationRepository notificationRepository,
            MeterRegistry meterRegistry) {
        this.alertRepository = alertRepository;
        this.vehicleRepository = vehicleRepository;
        this.notificationRepository = notificationRepository;
        this.meterRegistry = meterRegistry;
    }

    @PostConstruct
    public void init() {
        // Initialize metrics
        alertsGeneratedCounter = Counter.builder("telematics.alerts.generated")
            .description("Number of telemetry alerts generated")
            .register(meterRegistry);

        alertsDeduplicatedCounter = Counter.builder("telematics.alerts.deduplicated")
            .description("Number of duplicate alerts suppressed")
            .register(meterRegistry);

        notificationsSentCounter = Counter.builder("telematics.alerts.notifications")
            .description("Number of alert notifications sent")
            .register(meterRegistry);

        log.info("TelemetryAlertService initialized. Alerts enabled: {}, cooldown: {} min", 
            alertsEnabled, alertCooldownMinutes);
    }

    /**
     * Process telemetry data and generate alerts if conditions are met
     * Called by TelemetrySyncScheduler after each successful sync
     */
    public List<TelemetryAlert> processAndGenerateAlerts(Vehicle vehicle, VehicleTelemetryData telemetry) {
        if (!alertsEnabled) {
            return Collections.emptyList();
        }

        List<TelemetryAlert> generatedAlerts = new ArrayList<>();

        try {
            // Check battery alerts (EV/Hybrid only)
            if (vehicle.getFuelType() != null && 
                (vehicle.getFuelType().name().contains("EV") || 
                 vehicle.getFuelType().name().contains("HYBRID"))) {
                generatedAlerts.addAll(checkBatteryAlerts(vehicle, telemetry));
            }

            // Check fuel alerts (ICE/Hybrid only)
            if (vehicle.getFuelType() != null && 
                (vehicle.getFuelType().name().contains("ICE") || 
                 vehicle.getFuelType().name().contains("HYBRID"))) {
                generatedAlerts.addAll(checkFuelAlerts(vehicle, telemetry));
            }

            // Check speed alerts (all vehicles)
            generatedAlerts.addAll(checkSpeedAlerts(vehicle, telemetry));

            // Auto-resolve CONNECTION_LOST alerts since we just got data
            autoResolveConnectionAlerts(vehicle);

        } catch (Exception e) {
            log.error("Error processing alerts for vehicle {}: {}", vehicle.getId(), e.getMessage());
        }

        return generatedAlerts;
    }

    /**
     * Check for battery-related alerts
     */
    private List<TelemetryAlert> checkBatteryAlerts(Vehicle vehicle, VehicleTelemetryData telemetry) {
        List<TelemetryAlert> alerts = new ArrayList<>();

        if (telemetry.getBatterySoc() == null) {
            return alerts;
        }

        double soc = telemetry.getBatterySoc();

        // Critical battery (< 10%)
        if (soc < criticalBatteryThreshold) {
            TelemetryAlert alert = createAlertIfNotDuplicate(
                vehicle,
                AlertType.CRITICAL_BATTERY,
                AlertPriority.HIGH,
                "Critical Battery Level",
                String.format("Vehicle %s battery is critically low at %.1f%%. Immediate charging required.",
                    vehicle.getVehicleNumber(), soc),
                soc,
                (double) criticalBatteryThreshold,
                telemetry
            );
            if (alert != null) alerts.add(alert);
        }
        // Low battery (< 20%)
        else if (soc < lowBatteryThreshold) {
            TelemetryAlert alert = createAlertIfNotDuplicate(
                vehicle,
                AlertType.LOW_BATTERY,
                AlertPriority.MEDIUM,
                "Low Battery Level",
                String.format("Vehicle %s battery is low at %.1f%%. Consider charging soon.",
                    vehicle.getVehicleNumber(), soc),
                soc,
                (double) lowBatteryThreshold,
                telemetry
            );
            if (alert != null) alerts.add(alert);
        }

        return alerts;
    }

    /**
     * Check for fuel-related alerts
     */
    private List<TelemetryAlert> checkFuelAlerts(Vehicle vehicle, VehicleTelemetryData telemetry) {
        List<TelemetryAlert> alerts = new ArrayList<>();

        if (telemetry.getFuelLevel() == null || vehicle.getFuelTankCapacity() == null) {
            return alerts;
        }

        // Calculate fuel percentage
        double fuelPercent = (telemetry.getFuelLevel() / vehicle.getFuelTankCapacity()) * 100;

        if (fuelPercent < lowFuelThreshold) {
            TelemetryAlert alert = createAlertIfNotDuplicate(
                vehicle,
                AlertType.LOW_FUEL,
                AlertPriority.MEDIUM,
                "Low Fuel Level",
                String.format("Vehicle %s fuel is low at %.1f%%. Refueling required.",
                    vehicle.getVehicleNumber(), fuelPercent),
                fuelPercent,
                (double) lowFuelThreshold,
                telemetry
            );
            if (alert != null) alerts.add(alert);
        }

        return alerts;
    }

    /**
     * Check for speed-related alerts
     */
    private List<TelemetryAlert> checkSpeedAlerts(Vehicle vehicle, VehicleTelemetryData telemetry) {
        List<TelemetryAlert> alerts = new ArrayList<>();

        if (telemetry.getSpeed() == null) {
            return alerts;
        }

        double speed = telemetry.getSpeed();

        if (speed > maxSpeedKmh) {
            TelemetryAlert alert = createAlertIfNotDuplicate(
                vehicle,
                AlertType.EXCESSIVE_SPEED,
                AlertPriority.MEDIUM,
                "Excessive Speed",
                String.format("Vehicle %s traveling at %.0f km/h, exceeding limit of %d km/h.",
                    vehicle.getVehicleNumber(), speed, maxSpeedKmh),
                speed,
                (double) maxSpeedKmh,
                telemetry
            );
            if (alert != null) alerts.add(alert);
        }

        return alerts;
    }

    /**
     * Check for stale connections and generate CONNECTION_LOST alerts
     * This should be called periodically (e.g., by a scheduled job)
     */
    public List<TelemetryAlert> checkConnectionLostAlerts() {
        if (!alertsEnabled) {
            return Collections.emptyList();
        }

        List<TelemetryAlert> alerts = new ArrayList<>();
        LocalDateTime threshold = LocalDateTime.now().minusMinutes(connectionTimeoutMinutes);

        // Find vehicles with telematics that haven't reported recently
        List<Vehicle> staleVehicles = vehicleRepository.findAll().stream()
            .filter(v -> v.getTelemetrySource() == Vehicle.TelemetrySource.DEVICE 
                      || v.getTelemetrySource() == Vehicle.TelemetrySource.OEM_API)
            .filter(v -> v.getLastTelemetryUpdate() != null && v.getLastTelemetryUpdate().isBefore(threshold))
            .filter(v -> v.getStatus() != Vehicle.VehicleStatus.INACTIVE)
            .toList();

        for (Vehicle vehicle : staleVehicles) {
            long minutesSinceUpdate = java.time.Duration.between(
                vehicle.getLastTelemetryUpdate(), LocalDateTime.now()
            ).toMinutes();

            TelemetryAlert alert = createAlertIfNotDuplicate(
                vehicle,
                AlertType.CONNECTION_LOST,
                AlertPriority.LOW,
                "Connection Lost",
                String.format("Vehicle %s has not reported telemetry for %d minutes.",
                    vehicle.getVehicleNumber(), minutesSinceUpdate),
                (double) minutesSinceUpdate,
                (double) connectionTimeoutMinutes,
                null
            );
            if (alert != null) alerts.add(alert);
        }

        return alerts;
    }

    /**
     * Create an alert if a similar one doesn't already exist within the cooldown period
     */
    private TelemetryAlert createAlertIfNotDuplicate(
            Vehicle vehicle,
            AlertType alertType,
            AlertPriority priority,
            String title,
            String message,
            Double currentValue,
            Double thresholdValue,
            VehicleTelemetryData telemetry) {

        // Check for recent duplicate
        LocalDateTime cooldownStart = LocalDateTime.now().minusMinutes(alertCooldownMinutes);
        boolean isDuplicate = alertRepository.existsRecentAlert(vehicle.getId(), alertType, cooldownStart);

        if (isDuplicate) {
            log.debug("Suppressing duplicate {} alert for vehicle {}", alertType, vehicle.getId());
            alertsDeduplicatedCounter.increment();
            return null;
        }

        // Create new alert
        TelemetryAlert alert = TelemetryAlert.builder()
            .vehicleId(vehicle.getId())
            .companyId(vehicle.getCompanyId())
            .driverId(vehicle.getCurrentDriverId())
            .alertType(alertType)
            .priority(priority)
            .status(AlertStatus.ACTIVE)
            .title(title)
            .message(message)
            .currentValue(currentValue)
            .thresholdValue(thresholdValue)
            .triggeredAt(LocalDateTime.now())
            .build();

        // Add location if available
        if (telemetry != null) {
            alert.setLatitude(telemetry.getLatitude());
            alert.setLongitude(telemetry.getLongitude());
            alert.setSpeed(telemetry.getSpeed());
        }

        alert = alertRepository.save(alert);
        alertsGeneratedCounter.increment();

        log.info("Generated {} alert for vehicle {} - {}", alertType, vehicle.getId(), title);

        // Send notification
        sendAlertNotification(alert, vehicle);

        return alert;
    }

    /**
     * Auto-resolve CONNECTION_LOST alerts when connection is restored
     */
    private void autoResolveConnectionAlerts(Vehicle vehicle) {
        List<TelemetryAlert> activeConnectionAlerts = alertRepository
            .findByVehicleIdAndStatusOrderByPriorityDescTriggeredAtDesc(vehicle.getId(), AlertStatus.ACTIVE)
            .stream()
            .filter(a -> a.getAlertType() == AlertType.CONNECTION_LOST)
            .toList();

        for (TelemetryAlert alert : activeConnectionAlerts) {
            alert.resolve(null, "Auto-resolved: Connection restored");
            alertRepository.save(alert);

            // Create CONNECTION_RESTORED info alert
            TelemetryAlert restoredAlert = TelemetryAlert.builder()
                .vehicleId(vehicle.getId())
                .companyId(vehicle.getCompanyId())
                .alertType(AlertType.CONNECTION_RESTORED)
                .priority(AlertPriority.LOW)
                .status(AlertStatus.RESOLVED) // Already resolved, just informational
                .title("Connection Restored")
                .message(String.format("Vehicle %s telemetry connection restored.", vehicle.getVehicleNumber()))
                .triggeredAt(LocalDateTime.now())
                .resolvedAt(LocalDateTime.now())
                .build();
            alertRepository.save(restoredAlert);

            log.info("Auto-resolved CONNECTION_LOST alert for vehicle {}", vehicle.getId());
        }
    }

    /**
     * Send notification for an alert
     */
    private void sendAlertNotification(TelemetryAlert alert, Vehicle vehicle) {
        try {
            // Create notification for company admins (simplified - in production would query users)
            Notification notification = Notification.builder()
                .userId(1L) // TODO: Get actual admin user IDs for the company
                .title(alert.getTitle())
                .message(alert.getMessage())
                .type(mapPriorityToNotificationType(alert.getPriority()))
                .referenceId("alert:" + alert.getId())
                .build();

            notificationRepository.save(notification);
            notificationsSentCounter.increment();

            // Mark alert as notified
            alert.markNotificationSent();
            alertRepository.save(alert);

        } catch (Exception e) {
            log.error("Failed to send notification for alert {}: {}", alert.getId(), e.getMessage());
        }
    }

    private Notification.NotificationType mapPriorityToNotificationType(AlertPriority priority) {
        return switch (priority) {
            case CRITICAL, HIGH -> Notification.NotificationType.ALERT;
            case MEDIUM -> Notification.NotificationType.WARNING;
            case LOW -> Notification.NotificationType.INFO;
        };
    }

    // ===== PUBLIC API METHODS =====

    /**
     * Get all active alerts for a company
     */
    @Transactional(readOnly = true)
    public List<TelemetryAlert> getActiveAlerts(Long companyId) {
        return alertRepository.findByCompanyIdAndStatusOrderByPriorityDescTriggeredAtDesc(
            companyId, AlertStatus.ACTIVE
        );
    }

    /**
     * Get urgent (high/critical) alerts for a company
     */
    @Transactional(readOnly = true)
    public List<TelemetryAlert> getUrgentAlerts(Long companyId) {
        return alertRepository.findUrgentAlerts(companyId);
    }

    /**
     * Get alerts for a specific vehicle
     */
    @Transactional(readOnly = true)
    public List<TelemetryAlert> getVehicleAlerts(Long vehicleId) {
        return alertRepository.findByVehicleIdOrderByTriggeredAtDesc(vehicleId);
    }

    /**
     * Acknowledge an alert
     */
    public TelemetryAlert acknowledgeAlert(Long alertId, Long userId) {
        TelemetryAlert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));

        alert.acknowledge(userId);
        return alertRepository.save(alert);
    }

    /**
     * Resolve an alert
     */
    public TelemetryAlert resolveAlert(Long alertId, Long userId, String notes) {
        TelemetryAlert alert = alertRepository.findById(alertId)
            .orElseThrow(() -> new IllegalArgumentException("Alert not found: " + alertId));

        alert.resolve(userId, notes);
        return alertRepository.save(alert);
    }

    /**
     * Acknowledge all alerts for a vehicle
     */
    public int acknowledgeAllForVehicle(Long vehicleId, Long userId) {
        return alertRepository.acknowledgeAllForVehicle(vehicleId, userId, LocalDateTime.now());
    }

    /**
     * Get alert statistics for a company
     */
    @Transactional(readOnly = true)
    public AlertStatistics getAlertStatistics(Long companyId) {
        long totalActive = alertRepository.countByCompanyIdAndStatus(companyId, AlertStatus.ACTIVE);
        long criticalCount = alertRepository.countByCompanyIdAndStatusAndPriority(
            companyId, AlertStatus.ACTIVE, AlertPriority.CRITICAL);
        long highCount = alertRepository.countByCompanyIdAndStatusAndPriority(
            companyId, AlertStatus.ACTIVE, AlertPriority.HIGH);
        long mediumCount = alertRepository.countByCompanyIdAndStatusAndPriority(
            companyId, AlertStatus.ACTIVE, AlertPriority.MEDIUM);
        long lowCount = alertRepository.countByCompanyIdAndStatusAndPriority(
            companyId, AlertStatus.ACTIVE, AlertPriority.LOW);

        return new AlertStatistics(totalActive, criticalCount, highCount, mediumCount, lowCount);
    }

    /**
     * Statistics record for alerts
     */
    public record AlertStatistics(
        long totalActive,
        long critical,
        long high,
        long medium,
        long low
    ) {}
}
