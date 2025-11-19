package com.evfleet.driver.scheduler;

import com.evfleet.auth.model.User;
import com.evfleet.auth.repository.UserRepository;
import com.evfleet.driver.model.Driver;
import com.evfleet.driver.repository.DriverRepository;
import com.evfleet.notification.model.Notification;
import com.evfleet.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * License Expiry Job
 * 
 * Runs daily at 9 AM to check for expiring driver licenses
 * and sends notifications to fleet managers.
 * 
 * @author SEV Platform Team
 * @version 1.0.0
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class LicenseExpiryJob {

    private final DriverRepository driverRepository;
    private final UserRepository userRepository;
    private final NotificationRepository notificationRepository;

    /**
     * Check for expiring licenses daily at 9 AM
     * Sends alerts for licenses expiring in 30, 15, and 7 days
     */
    @Scheduled(cron = "0 0 9 * * *")
    @Transactional
    public void checkExpiringLicenses() {
        log.info("Starting daily license expiry check...");

        try {
            // Check for licenses expiring in 30 days
            checkAndNotify(30, "License Expiring in 30 Days");
            
            // Check for licenses expiring in 15 days
            checkAndNotify(15, "License Expiring in 15 Days - Urgent");
            
            // Check for licenses expiring in 7 days
            checkAndNotify(7, "License Expiring in 7 Days - Critical");
            
            // Check for expired licenses (already expired)
            checkAndNotifyExpired();

            log.info("Daily license expiry check completed successfully");
        } catch (Exception e) {
            log.error("Error during license expiry check", e);
        }
    }

    private void checkAndNotify(int daysAhead, String titlePrefix) {
        LocalDate targetDate = LocalDate.now().plusDays(daysAhead);
        
        // Find all drivers whose license expires on the target date
        List<Driver> drivers = driverRepository.findByLicenseExpiry(targetDate);
        
        log.info("Found {} drivers with licenses expiring in {} days", drivers.size(), daysAhead);
        
        for (Driver driver : drivers) {
            // Find fleet managers for this driver's company
            List<User> managers = userRepository.findFleetManagersByCompanyId(driver.getCompanyId());
            
            for (User manager : managers) {
                createNotification(
                    manager.getId(),
                    titlePrefix,
                    String.format("Driver %s (License: %s) has their license expiring on %s",
                        driver.getName(),
                        driver.getLicenseNumber(),
                        driver.getLicenseExpiry()),
                    Notification.NotificationType.WARNING,
                    "DRIVER_LICENSE_" + driver.getId()
                );
            }
        }
    }

    private void checkAndNotifyExpired() {
        LocalDate today = LocalDate.now();
        
        // Find all drivers whose license has already expired but are still active
        List<Driver> expiredDrivers = driverRepository.findByLicenseExpiryBeforeAndStatus(
            today, Driver.DriverStatus.ACTIVE
        );
        
        log.info("Found {} drivers with expired licenses", expiredDrivers.size());
        
        for (Driver driver : expiredDrivers) {
            // Find fleet managers for this driver's company
            List<User> managers = userRepository.findFleetManagersByCompanyId(driver.getCompanyId());
            
            for (User manager : managers) {
                createNotification(
                    manager.getId(),
                    "EXPIRED License - Immediate Action Required",
                    String.format("Driver %s (License: %s) has an EXPIRED license (expired on %s). " +
                        "This driver should not be assigned to any trips until the license is renewed.",
                        driver.getName(),
                        driver.getLicenseNumber(),
                        driver.getLicenseExpiry()),
                    Notification.NotificationType.ALERT,
                    "DRIVER_LICENSE_EXPIRED_" + driver.getId()
                );
            }
        }
    }

    private void createNotification(Long userId, String title, String message, 
                                   Notification.NotificationType type, String referenceId) {
        // Check if a notification with this reference ID already exists today
        // to avoid duplicate notifications
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        boolean alreadyNotified = notificationRepository.existsByUserIdAndReferenceIdAndCreatedAtAfter(
            userId, referenceId, startOfDay
        );
        
        if (!alreadyNotified) {
            Notification notification = Notification.builder()
                .userId(userId)
                .title(title)
                .message(message)
                .type(type)
                .isRead(false)
                .referenceId(referenceId)
                .build();
            
            notificationRepository.save(notification);
            log.debug("Created notification for user {} about {}", userId, referenceId);
        } else {
            log.debug("Notification already sent today for user {} about {}", userId, referenceId);
        }
    }
}
