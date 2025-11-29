package com.evfleet.driver.scheduler;

import com.evfleet.driver.model.Driver;
import com.evfleet.driver.repository.DriverRepository;
import com.evfleet.notification.model.Notification;
import com.evfleet.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * License Expiry Scheduler
 * 
 * Runs daily to check for driver licenses that are:
 * - Expiring within 30 days (LOW priority alert)
 * - Expiring within 7 days (MEDIUM priority alert)
 * - Already expired (HIGH priority alert)
 * 
 * Creates notifications for fleet managers to take action.
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LicenseExpiryScheduler {

    private final DriverRepository driverRepository;
    private final NotificationRepository notificationRepository;

    @Value("${evfleet.license.alert.days-30:30}")
    private int lowPriorityDays;

    @Value("${evfleet.license.alert.days-7:7}")
    private int mediumPriorityDays;

    @Value("${evfleet.license.alert.enabled:true}")
    private boolean alertsEnabled;

    /**
     * Run daily at 6 AM to check license expiry.
     * Cron: second minute hour day-of-month month day-of-week
     */
    @Scheduled(cron = "${evfleet.license.alert.cron:0 0 6 * * ?}")
    @Transactional
    public void checkLicenseExpiry() {
        if (!alertsEnabled) {
            log.info("License expiry alerts are disabled");
            return;
        }

        log.info("Running license expiry check at 6:00 AM");

        LocalDate today = LocalDate.now();
        LocalDate in30Days = today.plusDays(lowPriorityDays);
        LocalDate in7Days = today.plusDays(mediumPriorityDays);

        // Check for expired licenses (HIGH priority)
        checkExpiredLicenses(today);

        // Check for licenses expiring within 7 days (MEDIUM priority)
        checkExpiringWithin7Days(today, in7Days);

        // Check for licenses expiring within 30 days (LOW priority)
        checkExpiringWithin30Days(in7Days, in30Days);

        log.info("License expiry check completed");
    }

    /**
     * Check for already expired licenses.
     */
    private void checkExpiredLicenses(LocalDate today) {
        log.info("Checking for expired licenses...");

        List<Driver> expiredDrivers = driverRepository.findByLicenseExpiryBefore(today);
        
        for (Driver driver : expiredDrivers) {
            // Skip if driver is already inactive
            if (driver.getStatus() == Driver.DriverStatus.INACTIVE) {
                continue;
            }

            // Check if we already created an alert today for this driver
            if (hasRecentAlert(driver.getId(), "LICENSE_EXPIRED")) {
                continue;
            }

            createLicenseAlert(
                driver,
                Notification.NotificationType.ALERT,
                "EXPIRED License Alert",
                String.format(
                    "URGENT: Driver %s (ID: %d) has an EXPIRED license. " +
                    "License expired on %s. Driver should be suspended immediately until license is renewed.",
                    driver.getName(), driver.getId(), driver.getLicenseExpiry()
                ),
                "LICENSE_EXPIRED"
            );

            log.warn("ALERT: Driver {} has expired license (expired: {})", 
                driver.getName(), driver.getLicenseExpiry());
        }

        log.info("Found {} drivers with expired licenses", expiredDrivers.size());
    }

    /**
     * Check for licenses expiring within 7 days.
     */
    private void checkExpiringWithin7Days(LocalDate today, LocalDate in7Days) {
        log.info("Checking for licenses expiring within 7 days...");

        List<Driver> expiringDrivers = driverRepository.findByLicenseExpiryBetween(today, in7Days);

        for (Driver driver : expiringDrivers) {
            if (hasRecentAlert(driver.getId(), "LICENSE_EXPIRING_SOON")) {
                continue;
            }

            long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(today, driver.getLicenseExpiry());

            createLicenseAlert(
                driver,
                Notification.NotificationType.WARNING,
                "License Expiring SOON",
                String.format(
                    "Warning: Driver %s (ID: %d) license expires in %d days on %s. " +
                    "Please ensure license renewal is in progress.",
                    driver.getName(), driver.getId(), daysRemaining, driver.getLicenseExpiry()
                ),
                "LICENSE_EXPIRING_SOON"
            );

            log.warn("WARNING: Driver {} license expires in {} days", driver.getName(), daysRemaining);
        }

        log.info("Found {} drivers with licenses expiring within 7 days", expiringDrivers.size());
    }

    /**
     * Check for licenses expiring within 30 days (but more than 7 days out).
     */
    private void checkExpiringWithin30Days(LocalDate after7Days, LocalDate in30Days) {
        log.info("Checking for licenses expiring within 30 days...");

        List<Driver> expiringDrivers = driverRepository.findByLicenseExpiryBetween(after7Days, in30Days);

        for (Driver driver : expiringDrivers) {
            if (hasRecentAlert(driver.getId(), "LICENSE_EXPIRING_30_DAYS")) {
                continue;
            }

            long daysRemaining = java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), driver.getLicenseExpiry());

            createLicenseAlert(
                driver,
                Notification.NotificationType.INFO,
                "License Expiring in 30 Days",
                String.format(
                    "Notice: Driver %s (ID: %d) license expires in %d days on %s. " +
                    "Consider initiating license renewal process.",
                    driver.getName(), driver.getId(), daysRemaining, driver.getLicenseExpiry()
                ),
                "LICENSE_EXPIRING_30_DAYS"
            );

            log.info("NOTICE: Driver {} license expires in {} days", driver.getName(), daysRemaining);
        }

        log.info("Found {} drivers with licenses expiring within 30 days", expiringDrivers.size());
    }

    /**
     * Create a license alert notification.
     */
    private void createLicenseAlert(Driver driver, Notification.NotificationType type,
                                    String title, String message, String referenceType) {
        // Create notification for company admin (userId = companyId for system notifications)
        Notification notification = Notification.builder()
                .userId(driver.getCompanyId()) // Notify company admin
                .title(title)
                .message(message)
                .type(type)
                .referenceId(referenceType + "_" + driver.getId())
                .isRead(false)
                .build();

        notificationRepository.save(notification);
        log.info("Created {} notification for driver {} license expiry", type, driver.getId());
    }

    /**
     * Check if we already created an alert recently for this driver.
     * Prevents duplicate alerts.
     */
    private boolean hasRecentAlert(Long driverId, String referenceType) {
        String referenceId = referenceType + "_" + driverId;
        
        // Check if an alert was created in the last 24 hours
        LocalDate yesterday = LocalDate.now().minusDays(1);
        
        List<Notification> recentAlerts = notificationRepository.findByReferenceIdAndCreatedAtAfter(
            referenceId, 
            yesterday.atStartOfDay()
        );
        
        return !recentAlerts.isEmpty();
    }

    /**
     * Manual trigger for testing or immediate check.
     */
    public void runManualCheck() {
        log.info("Manual license expiry check triggered");
        checkLicenseExpiry();
    }
}
