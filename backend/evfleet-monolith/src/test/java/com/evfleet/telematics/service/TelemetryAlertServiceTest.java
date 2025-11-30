package com.evfleet.telematics.service;

import com.evfleet.fleet.model.FuelType;
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
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TelemetryAlertService
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class TelemetryAlertServiceTest {

    @Mock
    private TelemetryAlertRepository alertRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private NotificationRepository notificationRepository;

    private TelemetryAlertService alertService;

    @BeforeEach
    void setUp() {
        alertService = new TelemetryAlertService(
            alertRepository,
            vehicleRepository,
            notificationRepository,
            new SimpleMeterRegistry()
        );
        
        // Set configuration values
        ReflectionTestUtils.setField(alertService, "lowBatteryThreshold", 20);
        ReflectionTestUtils.setField(alertService, "criticalBatteryThreshold", 10);
        ReflectionTestUtils.setField(alertService, "maxSpeedKmh", 120);
        ReflectionTestUtils.setField(alertService, "connectionTimeoutMinutes", 60);
        ReflectionTestUtils.setField(alertService, "lowFuelThreshold", 15);
        ReflectionTestUtils.setField(alertService, "alertCooldownMinutes", 30);
        ReflectionTestUtils.setField(alertService, "alertsEnabled", true);
        
        // Initialize the service
        alertService.init();
    }

    @Test
    @DisplayName("Should generate CRITICAL_BATTERY alert when SOC < 10%")
    void shouldGenerateCriticalBatteryAlert() {
        // Given
        Vehicle vehicle = createTestEVVehicle();
        VehicleTelemetryData telemetry = createTelemetryWithSOC(5.0);
        
        when(alertRepository.existsRecentAlert(any(), any(), any())).thenReturn(false);
        when(alertRepository.save(any(TelemetryAlert.class))).thenAnswer(i -> {
            TelemetryAlert alert = i.getArgument(0);
            alert.setId(1L);
            return alert;
        });
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // When
        List<TelemetryAlert> alerts = alertService.processAndGenerateAlerts(vehicle, telemetry);
        
        // Then
        assertThat(alerts).hasSize(1);
        TelemetryAlert alert = alerts.get(0);
        assertThat(alert.getAlertType()).isEqualTo(AlertType.CRITICAL_BATTERY);
        assertThat(alert.getPriority()).isEqualTo(AlertPriority.HIGH);
        assertThat(alert.getCurrentValue()).isEqualTo(5.0);
        assertThat(alert.getThresholdValue()).isEqualTo(10.0);
    }

    @Test
    @DisplayName("Should generate LOW_BATTERY alert when SOC between 10-20%")
    void shouldGenerateLowBatteryAlert() {
        // Given
        Vehicle vehicle = createTestEVVehicle();
        VehicleTelemetryData telemetry = createTelemetryWithSOC(15.0);
        
        when(alertRepository.existsRecentAlert(any(), any(), any())).thenReturn(false);
        when(alertRepository.save(any(TelemetryAlert.class))).thenAnswer(i -> {
            TelemetryAlert alert = i.getArgument(0);
            alert.setId(1L);
            return alert;
        });
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // When
        List<TelemetryAlert> alerts = alertService.processAndGenerateAlerts(vehicle, telemetry);
        
        // Then
        assertThat(alerts).hasSize(1);
        TelemetryAlert alert = alerts.get(0);
        assertThat(alert.getAlertType()).isEqualTo(AlertType.LOW_BATTERY);
        assertThat(alert.getPriority()).isEqualTo(AlertPriority.MEDIUM);
    }

    @Test
    @DisplayName("Should not generate battery alert when SOC > 20%")
    void shouldNotGenerateBatteryAlertWhenSOCNormal() {
        // Given
        Vehicle vehicle = createTestEVVehicle();
        VehicleTelemetryData telemetry = createTelemetryWithSOC(75.0);
        
        // When
        List<TelemetryAlert> alerts = alertService.processAndGenerateAlerts(vehicle, telemetry);
        
        // Then
        assertThat(alerts).isEmpty();
        verify(alertRepository, never()).save(any(TelemetryAlert.class));
    }

    @Test
    @DisplayName("Should generate EXCESSIVE_SPEED alert when speed > limit")
    void shouldGenerateExcessiveSpeedAlert() {
        // Given
        Vehicle vehicle = createTestEVVehicle();
        VehicleTelemetryData telemetry = createTelemetryWithSpeed(150.0);
        
        when(alertRepository.existsRecentAlert(any(), any(), any())).thenReturn(false);
        when(alertRepository.save(any(TelemetryAlert.class))).thenAnswer(i -> {
            TelemetryAlert alert = i.getArgument(0);
            alert.setId(1L);
            return alert;
        });
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // When
        List<TelemetryAlert> alerts = alertService.processAndGenerateAlerts(vehicle, telemetry);
        
        // Then
        assertThat(alerts).hasSize(1);
        TelemetryAlert alert = alerts.get(0);
        assertThat(alert.getAlertType()).isEqualTo(AlertType.EXCESSIVE_SPEED);
        assertThat(alert.getPriority()).isEqualTo(AlertPriority.MEDIUM);
        assertThat(alert.getCurrentValue()).isEqualTo(150.0);
        assertThat(alert.getThresholdValue()).isEqualTo(120.0);
    }

    @Test
    @DisplayName("Should deduplicate alerts within cooldown period")
    void shouldDeduplicateAlertsWithinCooldown() {
        // Given
        Vehicle vehicle = createTestEVVehicle();
        VehicleTelemetryData telemetry = createTelemetryWithSOC(5.0);
        
        // Simulate existing recent alert
        when(alertRepository.existsRecentAlert(
            eq(vehicle.getId()), 
            eq(AlertType.CRITICAL_BATTERY), 
            any(LocalDateTime.class))
        ).thenReturn(true);
        
        // When
        List<TelemetryAlert> alerts = alertService.processAndGenerateAlerts(vehicle, telemetry);
        
        // Then
        assertThat(alerts).isEmpty();
        verify(alertRepository, never()).save(any(TelemetryAlert.class));
    }

    @Test
    @DisplayName("Should generate LOW_FUEL alert for ICE vehicle")
    void shouldGenerateLowFuelAlertForICE() {
        // Given
        Vehicle vehicle = createTestICEVehicle();
        vehicle.setFuelTankCapacity(50.0);
        VehicleTelemetryData telemetry = createTelemetryWithFuel(5.0); // 10% fuel
        
        when(alertRepository.existsRecentAlert(any(), any(), any())).thenReturn(false);
        when(alertRepository.save(any(TelemetryAlert.class))).thenAnswer(i -> {
            TelemetryAlert alert = i.getArgument(0);
            alert.setId(1L);
            return alert;
        });
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // When
        List<TelemetryAlert> alerts = alertService.processAndGenerateAlerts(vehicle, telemetry);
        
        // Then
        assertThat(alerts).hasSize(1);
        TelemetryAlert alert = alerts.get(0);
        assertThat(alert.getAlertType()).isEqualTo(AlertType.LOW_FUEL);
    }

    @Test
    @DisplayName("Should not generate alerts when disabled")
    void shouldNotGenerateAlertsWhenDisabled() {
        // Given
        ReflectionTestUtils.setField(alertService, "alertsEnabled", false);
        Vehicle vehicle = createTestEVVehicle();
        VehicleTelemetryData telemetry = createTelemetryWithSOC(5.0);
        
        // When
        List<TelemetryAlert> alerts = alertService.processAndGenerateAlerts(vehicle, telemetry);
        
        // Then
        assertThat(alerts).isEmpty();
    }

    @Test
    @DisplayName("Should acknowledge alert")
    void shouldAcknowledgeAlert() {
        // Given
        TelemetryAlert alert = createTestAlert(AlertStatus.ACTIVE);
        when(alertRepository.findById(1L)).thenReturn(Optional.of(alert));
        when(alertRepository.save(any(TelemetryAlert.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // When
        TelemetryAlert result = alertService.acknowledgeAlert(1L, 100L);
        
        // Then
        assertThat(result.getStatus()).isEqualTo(AlertStatus.ACKNOWLEDGED);
        assertThat(result.getAcknowledgedBy()).isEqualTo(100L);
        assertThat(result.getAcknowledgedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should resolve alert with notes")
    void shouldResolveAlertWithNotes() {
        // Given
        TelemetryAlert alert = createTestAlert(AlertStatus.ACTIVE);
        when(alertRepository.findById(1L)).thenReturn(Optional.of(alert));
        when(alertRepository.save(any(TelemetryAlert.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // When
        TelemetryAlert result = alertService.resolveAlert(1L, 100L, "Issue resolved by charging");
        
        // Then
        assertThat(result.getStatus()).isEqualTo(AlertStatus.RESOLVED);
        assertThat(result.getResolvedBy()).isEqualTo(100L);
        assertThat(result.getResolvedAt()).isNotNull();
        assertThat(result.getResolutionNotes()).isEqualTo("Issue resolved by charging");
    }

    @Test
    @DisplayName("Should return alert statistics")
    void shouldReturnAlertStatistics() {
        // Given
        Long companyId = 100L;
        when(alertRepository.countByCompanyIdAndStatus(companyId, AlertStatus.ACTIVE)).thenReturn(10L);
        when(alertRepository.countByCompanyIdAndStatusAndPriority(companyId, AlertStatus.ACTIVE, AlertPriority.CRITICAL)).thenReturn(2L);
        when(alertRepository.countByCompanyIdAndStatusAndPriority(companyId, AlertStatus.ACTIVE, AlertPriority.HIGH)).thenReturn(3L);
        when(alertRepository.countByCompanyIdAndStatusAndPriority(companyId, AlertStatus.ACTIVE, AlertPriority.MEDIUM)).thenReturn(3L);
        when(alertRepository.countByCompanyIdAndStatusAndPriority(companyId, AlertStatus.ACTIVE, AlertPriority.LOW)).thenReturn(2L);
        
        // When
        TelemetryAlertService.AlertStatistics stats = alertService.getAlertStatistics(companyId);
        
        // Then
        assertThat(stats.totalActive()).isEqualTo(10);
        assertThat(stats.critical()).isEqualTo(2);
        assertThat(stats.high()).isEqualTo(3);
        assertThat(stats.medium()).isEqualTo(3);
        assertThat(stats.low()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should send notification when alert is generated")
    void shouldSendNotificationWhenAlertGenerated() {
        // Given
        Vehicle vehicle = createTestEVVehicle();
        VehicleTelemetryData telemetry = createTelemetryWithSOC(5.0);
        
        when(alertRepository.existsRecentAlert(any(), any(), any())).thenReturn(false);
        when(alertRepository.save(any(TelemetryAlert.class))).thenAnswer(i -> {
            TelemetryAlert alert = i.getArgument(0);
            alert.setId(1L);
            return alert;
        });
        
        ArgumentCaptor<Notification> notificationCaptor = ArgumentCaptor.forClass(Notification.class);
        when(notificationRepository.save(notificationCaptor.capture())).thenAnswer(i -> i.getArguments()[0]);
        
        // When
        alertService.processAndGenerateAlerts(vehicle, telemetry);
        
        // Then
        verify(notificationRepository).save(any(Notification.class));
        Notification notification = notificationCaptor.getValue();
        assertThat(notification.getTitle()).isEqualTo("Critical Battery Level");
        assertThat(notification.getType()).isEqualTo(Notification.NotificationType.ALERT);
    }

    @Test
    @DisplayName("Should generate multiple alerts for multiple conditions")
    void shouldGenerateMultipleAlertsForMultipleConditions() {
        // Given
        Vehicle vehicle = createTestEVVehicle();
        VehicleTelemetryData telemetry = new VehicleTelemetryData();
        telemetry.setBatterySoc(5.0); // Critical battery
        telemetry.setSpeed(150.0);    // Excessive speed
        telemetry.setTimestamp(LocalDateTime.now());
        
        when(alertRepository.existsRecentAlert(any(), any(), any())).thenReturn(false);
        when(alertRepository.save(any(TelemetryAlert.class))).thenAnswer(i -> {
            TelemetryAlert alert = i.getArgument(0);
            alert.setId(1L);
            return alert;
        });
        when(notificationRepository.save(any(Notification.class))).thenAnswer(i -> i.getArguments()[0]);
        
        // When
        List<TelemetryAlert> alerts = alertService.processAndGenerateAlerts(vehicle, telemetry);
        
        // Then
        assertThat(alerts).hasSize(2);
        assertThat(alerts).extracting(TelemetryAlert::getAlertType)
            .containsExactlyInAnyOrder(AlertType.CRITICAL_BATTERY, AlertType.EXCESSIVE_SPEED);
    }

    // ===== HELPER METHODS =====

    private Vehicle createTestEVVehicle() {
        return Vehicle.builder()
            .id(1L)
            .companyId(100L)
            .vehicleNumber("KA-01-EV-0001")
            .make("Tata")
            .model("Tigor EV")
            .fuelType(FuelType.EV)
            .type(Vehicle.VehicleType.LCV)
            .status(Vehicle.VehicleStatus.ACTIVE)
            .telemetrySource(Vehicle.TelemetrySource.DEVICE)
            .build();
    }

    private Vehicle createTestICEVehicle() {
        return Vehicle.builder()
            .id(2L)
            .companyId(100L)
            .vehicleNumber("KA-01-IC-0001")
            .make("Maruti")
            .model("Eeco")
            .fuelType(FuelType.ICE)
            .type(Vehicle.VehicleType.LCV)
            .status(Vehicle.VehicleStatus.ACTIVE)
            .telemetrySource(Vehicle.TelemetrySource.DEVICE)
            .build();
    }

    private VehicleTelemetryData createTelemetryWithSOC(Double soc) {
        VehicleTelemetryData data = new VehicleTelemetryData();
        data.setBatterySoc(soc);
        data.setSpeed(60.0);
        data.setTimestamp(LocalDateTime.now());
        return data;
    }

    private VehicleTelemetryData createTelemetryWithSpeed(Double speed) {
        VehicleTelemetryData data = new VehicleTelemetryData();
        data.setBatterySoc(75.0);
        data.setSpeed(speed);
        data.setTimestamp(LocalDateTime.now());
        return data;
    }

    private VehicleTelemetryData createTelemetryWithFuel(Double fuelLevel) {
        VehicleTelemetryData data = new VehicleTelemetryData();
        data.setFuelLevel(fuelLevel);
        data.setSpeed(60.0);
        data.setTimestamp(LocalDateTime.now());
        return data;
    }

    private TelemetryAlert createTestAlert(AlertStatus status) {
        return TelemetryAlert.builder()
            .id(1L)
            .vehicleId(1L)
            .companyId(100L)
            .alertType(AlertType.CRITICAL_BATTERY)
            .priority(AlertPriority.HIGH)
            .status(status)
            .title("Test Alert")
            .message("Test alert message")
            .triggeredAt(LocalDateTime.now())
            .build();
    }
}
