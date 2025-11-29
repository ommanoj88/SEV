package com.evfleet.driver.scheduler;

import com.evfleet.driver.model.Driver;
import com.evfleet.driver.repository.DriverRepository;
import com.evfleet.notification.model.Notification;
import com.evfleet.notification.repository.NotificationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for LicenseExpiryScheduler
 * 
 * PR #12: License Expiry Alerts
 * Tests cover:
 * - Expired license detection
 * - 7-day expiry warning
 * - 30-day expiry notice
 * - Duplicate alert prevention
 * - Disabled alerts config
 */
@ExtendWith(MockitoExtension.class)
class LicenseExpirySchedulerTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @InjectMocks
    private LicenseExpiryScheduler scheduler;

    private static final Long COMPANY_ID = 1L;

    @BeforeEach
    void setUp() {
        // Set default config values
        ReflectionTestUtils.setField(scheduler, "lowPriorityDays", 30);
        ReflectionTestUtils.setField(scheduler, "mediumPriorityDays", 7);
        ReflectionTestUtils.setField(scheduler, "alertsEnabled", true);
    }

    @Test
    @DisplayName("Should create HIGH priority alert for expired license")
    void checkLicenseExpiry_ExpiredLicense_CreatesAlert() {
        // Arrange
        Driver expiredDriver = createDriver(1L, "Expired Driver", LocalDate.now().minusDays(1));
        
        when(driverRepository.findByLicenseExpiryBefore(any(LocalDate.class)))
                .thenReturn(Arrays.asList(expiredDriver));
        when(driverRepository.findByLicenseExpiryBetween(any(), any()))
                .thenReturn(Collections.emptyList());
        when(notificationRepository.findByReferenceIdAndCreatedAtAfter(anyString(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        scheduler.checkLicenseExpiry();

        // Assert
        verify(notificationRepository).save(argThat(notification -> 
                notification.getType() == Notification.NotificationType.ALERT &&
                notification.getTitle().contains("EXPIRED")
        ));
    }

    @Test
    @DisplayName("Should create MEDIUM priority alert for license expiring in 7 days")
    void checkLicenseExpiry_ExpiringIn7Days_CreatesWarning() {
        // Arrange
        Driver expiringDriver = createDriver(2L, "Expiring Soon", LocalDate.now().plusDays(5));
        
        when(driverRepository.findByLicenseExpiryBefore(any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(driverRepository.findByLicenseExpiryBetween(any(), any()))
                .thenAnswer(invocation -> {
                    LocalDate start = invocation.getArgument(0);
                    LocalDate end = invocation.getArgument(1);
                    // Return driver only for 0-7 day range
                    if (start.equals(LocalDate.now())) {
                        return Arrays.asList(expiringDriver);
                    }
                    return Collections.emptyList();
                });
        when(notificationRepository.findByReferenceIdAndCreatedAtAfter(anyString(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        scheduler.checkLicenseExpiry();

        // Assert
        verify(notificationRepository).save(argThat(notification -> 
                notification.getType() == Notification.NotificationType.WARNING &&
                notification.getTitle().contains("Expiring SOON")
        ));
    }

    @Test
    @DisplayName("Should create LOW priority alert for license expiring in 30 days")
    void checkLicenseExpiry_ExpiringIn30Days_CreatesInfo() {
        // Arrange
        Driver expiringDriver = createDriver(3L, "Expiring Later", LocalDate.now().plusDays(20));
        
        when(driverRepository.findByLicenseExpiryBefore(any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(driverRepository.findByLicenseExpiryBetween(any(), any()))
                .thenAnswer(invocation -> {
                    LocalDate start = invocation.getArgument(0);
                    LocalDate end = invocation.getArgument(1);
                    // Return driver only for 7-30 day range
                    if (!start.equals(LocalDate.now())) {
                        return Arrays.asList(expiringDriver);
                    }
                    return Collections.emptyList();
                });
        when(notificationRepository.findByReferenceIdAndCreatedAtAfter(anyString(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        scheduler.checkLicenseExpiry();

        // Assert
        verify(notificationRepository).save(argThat(notification -> 
                notification.getType() == Notification.NotificationType.INFO &&
                notification.getTitle().contains("30 Days")
        ));
    }

    @Test
    @DisplayName("Should NOT create alert if recent alert exists")
    void checkLicenseExpiry_RecentAlertExists_SkipsCreation() {
        // Arrange
        Driver expiredDriver = createDriver(1L, "Expired Driver", LocalDate.now().minusDays(1));
        Notification existingAlert = Notification.builder()
                .id(100L)
                .referenceId("LICENSE_EXPIRED_1")
                .build();
        
        when(driverRepository.findByLicenseExpiryBefore(any(LocalDate.class)))
                .thenReturn(Arrays.asList(expiredDriver));
        when(driverRepository.findByLicenseExpiryBetween(any(), any()))
                .thenReturn(Collections.emptyList());
        when(notificationRepository.findByReferenceIdAndCreatedAtAfter(eq("LICENSE_EXPIRED_1"), any()))
                .thenReturn(Arrays.asList(existingAlert));

        // Act
        scheduler.checkLicenseExpiry();

        // Assert - No new alert should be created
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should skip INACTIVE drivers")
    void checkLicenseExpiry_InactiveDriver_SkipsAlert() {
        // Arrange
        Driver inactiveDriver = createDriver(4L, "Inactive Driver", LocalDate.now().minusDays(1));
        inactiveDriver.setStatus(Driver.DriverStatus.INACTIVE);
        
        when(driverRepository.findByLicenseExpiryBefore(any(LocalDate.class)))
                .thenReturn(Arrays.asList(inactiveDriver));
        when(driverRepository.findByLicenseExpiryBetween(any(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        scheduler.checkLicenseExpiry();

        // Assert - No alert for inactive driver
        verify(notificationRepository, never()).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should NOT run when alerts are disabled")
    void checkLicenseExpiry_AlertsDisabled_DoesNothing() {
        // Arrange
        ReflectionTestUtils.setField(scheduler, "alertsEnabled", false);

        // Act
        scheduler.checkLicenseExpiry();

        // Assert
        verify(driverRepository, never()).findByLicenseExpiryBefore(any());
        verify(driverRepository, never()).findByLicenseExpiryBetween(any(), any());
        verify(notificationRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should handle multiple drivers with different expiry statuses")
    void checkLicenseExpiry_MultipleDrivers_CreatesAppropriateAlerts() {
        // Arrange
        Driver expiredDriver = createDriver(1L, "Expired", LocalDate.now().minusDays(1));
        Driver soong7Driver = createDriver(2L, "Soon 7", LocalDate.now().plusDays(3));
        Driver soon30Driver = createDriver(3L, "Soon 30", LocalDate.now().plusDays(15));
        
        when(driverRepository.findByLicenseExpiryBefore(any(LocalDate.class)))
                .thenReturn(Arrays.asList(expiredDriver));
        when(driverRepository.findByLicenseExpiryBetween(any(), any()))
                .thenAnswer(invocation -> {
                    LocalDate start = invocation.getArgument(0);
                    if (start.equals(LocalDate.now())) {
                        return Arrays.asList(soong7Driver);
                    }
                    return Arrays.asList(soon30Driver);
                });
        when(notificationRepository.findByReferenceIdAndCreatedAtAfter(anyString(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        scheduler.checkLicenseExpiry();

        // Assert - 3 notifications should be created
        verify(notificationRepository, times(3)).save(any(Notification.class));
    }

    @Test
    @DisplayName("Should use correct reference ID format")
    void checkLicenseExpiry_CreatesCorrectReferenceId() {
        // Arrange
        Driver expiredDriver = createDriver(42L, "Test Driver", LocalDate.now().minusDays(1));
        
        when(driverRepository.findByLicenseExpiryBefore(any(LocalDate.class)))
                .thenReturn(Arrays.asList(expiredDriver));
        when(driverRepository.findByLicenseExpiryBetween(any(), any()))
                .thenReturn(Collections.emptyList());
        when(notificationRepository.findByReferenceIdAndCreatedAtAfter(anyString(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        scheduler.checkLicenseExpiry();

        // Assert
        verify(notificationRepository).save(argThat(notification -> 
                notification.getReferenceId().equals("LICENSE_EXPIRED_42")
        ));
    }

    @Test
    @DisplayName("Manual trigger should work correctly")
    void runManualCheck_InvokesCheckLicenseExpiry() {
        // Arrange
        when(driverRepository.findByLicenseExpiryBefore(any(LocalDate.class)))
                .thenReturn(Collections.emptyList());
        when(driverRepository.findByLicenseExpiryBetween(any(), any()))
                .thenReturn(Collections.emptyList());

        // Act
        scheduler.runManualCheck();

        // Assert
        verify(driverRepository).findByLicenseExpiryBefore(any(LocalDate.class));
    }

    private Driver createDriver(Long id, String name, LocalDate licenseExpiry) {
        return Driver.builder()
                .id(id)
                .companyId(COMPANY_ID)
                .name(name)
                .phone("123456789" + id)
                .licenseNumber("DL-" + id)
                .licenseExpiry(licenseExpiry)
                .status(Driver.DriverStatus.ACTIVE)
                .build();
    }
}
