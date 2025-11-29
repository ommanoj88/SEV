package com.evfleet.telematics.service;

import com.evfleet.driver.model.Driver;
import com.evfleet.driver.repository.DriverRepository;
import com.evfleet.telematics.dto.DrivingEventRequest;
import com.evfleet.telematics.dto.DrivingEventResponse;
import com.evfleet.telematics.model.DrivingEvent;
import com.evfleet.telematics.repository.DrivingEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for DrivingEventService
 * 
 * PR #10: Driver Behavior Analysis Service
 * Tests cover:
 * - Recording driving events
 * - Retrieving events by trip/driver/vehicle
 * - Safety score calculation algorithm
 * - Severity auto-detection
 * - Input validation
 */
@ExtendWith(MockitoExtension.class)
class DrivingEventServiceTest {

    @Mock
    private DrivingEventRepository drivingEventRepository;

    @Mock
    private DriverRepository driverRepository;

    @InjectMocks
    private DrivingEventService drivingEventService;

    private static final Long COMPANY_ID = 1L;
    private static final Long DRIVER_ID = 1L;
    private static final Long VEHICLE_ID = 10L;
    private static final Long TRIP_ID = 100L;

    private Driver testDriver;
    private DrivingEvent harshBrakingEvent;
    private DrivingEvent speedingEvent;
    private DrivingEvent idlingEvent;

    @BeforeEach
    void setUp() {
        testDriver = Driver.builder()
                .id(DRIVER_ID)
                .companyId(COMPANY_ID)
                .name("Test Driver")
                .phone("1234567890")
                .safetyScore(100.0)
                .build();

        harshBrakingEvent = DrivingEvent.builder()
                .id(1L)
                .driverId(DRIVER_ID)
                .vehicleId(VEHICLE_ID)
                .tripId(TRIP_ID)
                .companyId(COMPANY_ID)
                .eventType(DrivingEvent.EventType.HARSH_BRAKING)
                .severity(DrivingEvent.Severity.HIGH)
                .eventTime(LocalDateTime.now().minusHours(1))
                .gForce(new BigDecimal("0.8"))
                .build();

        speedingEvent = DrivingEvent.builder()
                .id(2L)
                .driverId(DRIVER_ID)
                .vehicleId(VEHICLE_ID)
                .tripId(TRIP_ID)
                .companyId(COMPANY_ID)
                .eventType(DrivingEvent.EventType.SPEEDING)
                .severity(DrivingEvent.Severity.MEDIUM)
                .eventTime(LocalDateTime.now().minusHours(2))
                .speed(80.0)
                .speedLimit(60.0)
                .build();

        idlingEvent = DrivingEvent.builder()
                .id(3L)
                .driverId(DRIVER_ID)
                .vehicleId(VEHICLE_ID)
                .tripId(TRIP_ID)
                .companyId(COMPANY_ID)
                .eventType(DrivingEvent.EventType.IDLING)
                .severity(DrivingEvent.Severity.LOW)
                .eventTime(LocalDateTime.now().minusHours(3))
                .duration(600) // 10 minutes
                .build();
    }

    @Nested
    @DisplayName("Record Event Tests")
    class RecordEventTests {

        @Test
        @DisplayName("Should successfully record a driving event")
        void recordEvent_Success() {
            // Arrange
            DrivingEventRequest request = DrivingEventRequest.builder()
                    .driverId(DRIVER_ID)
                    .vehicleId(VEHICLE_ID)
                    .tripId(TRIP_ID)
                    .companyId(COMPANY_ID)
                    .eventType(DrivingEvent.EventType.HARSH_BRAKING)
                    .gForce(new BigDecimal("0.6"))
                    .latitude(12.9716)
                    .longitude(77.5946)
                    .build();

            when(driverRepository.existsById(DRIVER_ID)).thenReturn(true);
            when(drivingEventRepository.save(any(DrivingEvent.class))).thenAnswer(inv -> {
                DrivingEvent event = inv.getArgument(0);
                event.setId(1L);
                return event;
            });
            when(driverRepository.findById(DRIVER_ID)).thenReturn(Optional.of(testDriver));
            when(drivingEventRepository.findByDriverIdAndEventTimeBetween(eq(DRIVER_ID), any(), any()))
                    .thenReturn(Collections.emptyList());

            // Act
            DrivingEventResponse response = drivingEventService.recordEvent(request);

            // Assert
            assertNotNull(response);
            assertEquals(DrivingEvent.EventType.HARSH_BRAKING.name(), response.getEventType());
            verify(drivingEventRepository).save(any(DrivingEvent.class));
        }

        @Test
        @DisplayName("Should reject event with invalid latitude")
        void recordEvent_InvalidLatitude_ThrowsException() {
            // Arrange
            DrivingEventRequest request = DrivingEventRequest.builder()
                    .driverId(DRIVER_ID)
                    .vehicleId(VEHICLE_ID)
                    .companyId(COMPANY_ID)
                    .eventType(DrivingEvent.EventType.SPEEDING)
                    .latitude(100.0) // Invalid - must be -90 to 90
                    .longitude(77.0)
                    .build();

            when(driverRepository.existsById(DRIVER_ID)).thenReturn(true);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> drivingEventService.recordEvent(request)
            );

            assertTrue(exception.getMessage().contains("Latitude"));
            verify(drivingEventRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject event with invalid longitude")
        void recordEvent_InvalidLongitude_ThrowsException() {
            // Arrange
            DrivingEventRequest request = DrivingEventRequest.builder()
                    .driverId(DRIVER_ID)
                    .vehicleId(VEHICLE_ID)
                    .companyId(COMPANY_ID)
                    .eventType(DrivingEvent.EventType.SPEEDING)
                    .latitude(12.0)
                    .longitude(200.0) // Invalid - must be -180 to 180
                    .build();

            when(driverRepository.existsById(DRIVER_ID)).thenReturn(true);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> drivingEventService.recordEvent(request)
            );

            assertTrue(exception.getMessage().contains("Longitude"));
            verify(drivingEventRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject event with negative speed")
        void recordEvent_NegativeSpeed_ThrowsException() {
            // Arrange
            DrivingEventRequest request = DrivingEventRequest.builder()
                    .driverId(DRIVER_ID)
                    .vehicleId(VEHICLE_ID)
                    .companyId(COMPANY_ID)
                    .eventType(DrivingEvent.EventType.SPEEDING)
                    .speed(-10.0)
                    .build();

            when(driverRepository.existsById(DRIVER_ID)).thenReturn(true);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> drivingEventService.recordEvent(request)
            );

            assertTrue(exception.getMessage().contains("Speed"));
        }

        @Test
        @DisplayName("Should reject event with negative g-force")
        void recordEvent_NegativeGForce_ThrowsException() {
            // Arrange
            DrivingEventRequest request = DrivingEventRequest.builder()
                    .driverId(DRIVER_ID)
                    .vehicleId(VEHICLE_ID)
                    .companyId(COMPANY_ID)
                    .eventType(DrivingEvent.EventType.HARSH_BRAKING)
                    .gForce(new BigDecimal("-0.5"))
                    .build();

            when(driverRepository.existsById(DRIVER_ID)).thenReturn(true);

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                    IllegalArgumentException.class,
                    () -> drivingEventService.recordEvent(request)
            );

            assertTrue(exception.getMessage().contains("G-force"));
        }

        @Test
        @DisplayName("Should throw exception when driver not found")
        void recordEvent_DriverNotFound_ThrowsException() {
            // Arrange
            DrivingEventRequest request = DrivingEventRequest.builder()
                    .driverId(999L)
                    .vehicleId(VEHICLE_ID)
                    .companyId(COMPANY_ID)
                    .eventType(DrivingEvent.EventType.SPEEDING)
                    .build();

            when(driverRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThrows(
                    com.evfleet.common.exception.ResourceNotFoundException.class,
                    () -> drivingEventService.recordEvent(request)
            );
        }
    }

    @Nested
    @DisplayName("Safety Score Calculation Tests")
    class SafetyScoreTests {

        @Test
        @DisplayName("Should return 100 for driver with no events")
        void calculateSafetyScore_NoEvents_Returns100() {
            // Arrange
            when(drivingEventRepository.findByDriverIdAndEventTimeBetween(eq(DRIVER_ID), any(), any()))
                    .thenReturn(Collections.emptyList());

            // Act
            double score = drivingEventService.calculateDriverSafetyScore(DRIVER_ID);

            // Assert
            assertEquals(100.0, score);
        }

        @Test
        @DisplayName("Should deduct points for harsh braking event")
        void calculateSafetyScore_HarshBraking_DeductsPoints() {
            // Arrange
            when(drivingEventRepository.findByDriverIdAndEventTimeBetween(eq(DRIVER_ID), any(), any()))
                    .thenReturn(Arrays.asList(harshBrakingEvent));

            // Act
            double score = drivingEventService.calculateDriverSafetyScore(DRIVER_ID);

            // Assert
            assertTrue(score < 100);
            // HIGH severity harsh braking = -10 points
            assertEquals(90.0, score);
        }

        @Test
        @DisplayName("Should deduct points for speeding event")
        void calculateSafetyScore_Speeding_DeductsPoints() {
            // Arrange
            when(drivingEventRepository.findByDriverIdAndEventTimeBetween(eq(DRIVER_ID), any(), any()))
                    .thenReturn(Arrays.asList(speedingEvent));

            // Act
            double score = drivingEventService.calculateDriverSafetyScore(DRIVER_ID);

            // Assert
            assertTrue(score < 100);
            // MEDIUM severity speeding = -10 points
            assertEquals(90.0, score);
        }

        @Test
        @DisplayName("Should deduct points for idling based on duration")
        void calculateSafetyScore_Idling_DeductsBasedOnDuration() {
            // Arrange
            when(drivingEventRepository.findByDriverIdAndEventTimeBetween(eq(DRIVER_ID), any(), any()))
                    .thenReturn(Arrays.asList(idlingEvent));

            // Act
            double score = drivingEventService.calculateDriverSafetyScore(DRIVER_ID);

            // Assert
            assertTrue(score < 100);
            // 10 minutes = 600 seconds / 300 = 2 penalty points
            assertEquals(98.0, score);
        }

        @Test
        @DisplayName("Should accumulate deductions for multiple events")
        void calculateSafetyScore_MultipleEvents_AccumulatesDeductions() {
            // Arrange
            when(drivingEventRepository.findByDriverIdAndEventTimeBetween(eq(DRIVER_ID), any(), any()))
                    .thenReturn(Arrays.asList(harshBrakingEvent, speedingEvent, idlingEvent));

            // Act
            double score = drivingEventService.calculateDriverSafetyScore(DRIVER_ID);

            // Assert
            // 100 - 10 (harsh braking) - 10 (speeding) - 2 (idling) = 78
            assertEquals(78.0, score);
        }

        @Test
        @DisplayName("Should not go below 0")
        void calculateSafetyScore_ManyEvents_DoesNotGoBelowZero() {
            // Arrange
            List<DrivingEvent> manyEvents = Arrays.asList(
                    createEvent(DrivingEvent.EventType.SPEEDING, DrivingEvent.Severity.CRITICAL), // -20
                    createEvent(DrivingEvent.EventType.SPEEDING, DrivingEvent.Severity.CRITICAL), // -20
                    createEvent(DrivingEvent.EventType.SPEEDING, DrivingEvent.Severity.CRITICAL), // -20
                    createEvent(DrivingEvent.EventType.SPEEDING, DrivingEvent.Severity.CRITICAL), // -20
                    createEvent(DrivingEvent.EventType.SPEEDING, DrivingEvent.Severity.CRITICAL), // -20
                    createEvent(DrivingEvent.EventType.SPEEDING, DrivingEvent.Severity.CRITICAL)  // -20 = -120 total
            );

            when(drivingEventRepository.findByDriverIdAndEventTimeBetween(eq(DRIVER_ID), any(), any()))
                    .thenReturn(manyEvents);

            // Act
            double score = drivingEventService.calculateDriverSafetyScore(DRIVER_ID);

            // Assert
            assertEquals(0.0, score);
        }

        private DrivingEvent createEvent(DrivingEvent.EventType type, DrivingEvent.Severity severity) {
            return DrivingEvent.builder()
                    .id((long) (Math.random() * 1000))
                    .driverId(DRIVER_ID)
                    .vehicleId(VEHICLE_ID)
                    .companyId(COMPANY_ID)
                    .eventType(type)
                    .severity(severity)
                    .eventTime(LocalDateTime.now().minusHours(1))
                    .build();
        }
    }

    @Nested
    @DisplayName("Get Events Tests")
    class GetEventsTests {

        @Test
        @DisplayName("Should get events by trip ID")
        void getEventsByTrip_Success() {
            // Arrange
            when(drivingEventRepository.findByTripId(TRIP_ID))
                    .thenReturn(Arrays.asList(harshBrakingEvent, speedingEvent));

            // Act
            List<DrivingEventResponse> events = drivingEventService.getEventsByTrip(TRIP_ID);

            // Assert
            assertNotNull(events);
            assertEquals(2, events.size());
            verify(drivingEventRepository).findByTripId(TRIP_ID);
        }

        @Test
        @DisplayName("Should get events by driver ID with date range")
        void getEventsByDriver_WithDateRange_Success() {
            // Arrange
            LocalDateTime start = LocalDateTime.now().minusDays(7);
            LocalDateTime end = LocalDateTime.now();

            when(drivingEventRepository.findByDriverIdAndEventTimeBetween(eq(DRIVER_ID), any(), any()))
                    .thenReturn(Arrays.asList(harshBrakingEvent));

            // Act
            List<DrivingEventResponse> events = drivingEventService.getEventsByDriver(DRIVER_ID, start, end);

            // Assert
            assertNotNull(events);
            assertEquals(1, events.size());
        }

        @Test
        @DisplayName("Should use default date range when null")
        void getEventsByDriver_NullDates_UsesDefaults() {
            // Arrange
            when(drivingEventRepository.findByDriverIdAndEventTimeBetween(eq(DRIVER_ID), any(), any()))
                    .thenReturn(Collections.emptyList());

            // Act
            List<DrivingEventResponse> events = drivingEventService.getEventsByDriver(DRIVER_ID, null, null);

            // Assert
            assertNotNull(events);
            verify(drivingEventRepository).findByDriverIdAndEventTimeBetween(eq(DRIVER_ID), any(), any());
        }

        @Test
        @DisplayName("Should get events by vehicle ID")
        void getEventsByVehicle_Success() {
            // Arrange
            when(drivingEventRepository.findByVehicleId(VEHICLE_ID))
                    .thenReturn(Arrays.asList(harshBrakingEvent, speedingEvent, idlingEvent));

            // Act
            List<DrivingEventResponse> events = drivingEventService.getEventsByVehicle(VEHICLE_ID);

            // Assert
            assertNotNull(events);
            assertEquals(3, events.size());
            verify(drivingEventRepository).findByVehicleId(VEHICLE_ID);
        }
    }

    @Nested
    @DisplayName("Severity Auto-Detection Tests")
    class SeverityDetectionTests {

        @Test
        @DisplayName("Should detect CRITICAL severity for 50%+ speeding")
        void determineSeverity_Speeding50Percent_Critical() {
            // Arrange
            DrivingEventRequest request = DrivingEventRequest.builder()
                    .driverId(DRIVER_ID)
                    .vehicleId(VEHICLE_ID)
                    .companyId(COMPANY_ID)
                    .eventType(DrivingEvent.EventType.SPEEDING)
                    .speed(100.0) // 66% over limit
                    .speedLimit(60.0)
                    .build();

            when(driverRepository.existsById(DRIVER_ID)).thenReturn(true);
            when(drivingEventRepository.save(any(DrivingEvent.class))).thenAnswer(inv -> {
                DrivingEvent event = inv.getArgument(0);
                event.setId(1L);
                assertEquals(DrivingEvent.Severity.CRITICAL, event.getSeverity());
                return event;
            });
            when(driverRepository.findById(DRIVER_ID)).thenReturn(Optional.of(testDriver));
            when(drivingEventRepository.findByDriverIdAndEventTimeBetween(eq(DRIVER_ID), any(), any()))
                    .thenReturn(Collections.emptyList());

            // Act
            drivingEventService.recordEvent(request);

            // Assert
            verify(drivingEventRepository).save(argThat(event -> 
                    event.getSeverity() == DrivingEvent.Severity.CRITICAL));
        }

        @Test
        @DisplayName("Should detect HIGH severity for high g-force")
        void determineSeverity_HighGForce_High() {
            // Arrange
            DrivingEventRequest request = DrivingEventRequest.builder()
                    .driverId(DRIVER_ID)
                    .vehicleId(VEHICLE_ID)
                    .companyId(COMPANY_ID)
                    .eventType(DrivingEvent.EventType.HARSH_BRAKING)
                    .gForce(new BigDecimal("0.8"))
                    .build();

            when(driverRepository.existsById(DRIVER_ID)).thenReturn(true);
            when(drivingEventRepository.save(any(DrivingEvent.class))).thenAnswer(inv -> {
                DrivingEvent event = inv.getArgument(0);
                event.setId(1L);
                return event;
            });
            when(driverRepository.findById(DRIVER_ID)).thenReturn(Optional.of(testDriver));
            when(drivingEventRepository.findByDriverIdAndEventTimeBetween(eq(DRIVER_ID), any(), any()))
                    .thenReturn(Collections.emptyList());

            // Act
            drivingEventService.recordEvent(request);

            // Assert
            verify(drivingEventRepository).save(argThat(event -> 
                    event.getSeverity() == DrivingEvent.Severity.HIGH));
        }

        @Test
        @DisplayName("Should detect CRITICAL severity for long idling")
        void determineSeverity_LongIdling_Critical() {
            // Arrange - 20 minutes idling
            DrivingEventRequest request = DrivingEventRequest.builder()
                    .driverId(DRIVER_ID)
                    .vehicleId(VEHICLE_ID)
                    .companyId(COMPANY_ID)
                    .eventType(DrivingEvent.EventType.IDLING)
                    .duration(1200) // 20 minutes > 15 min threshold
                    .build();

            when(driverRepository.existsById(DRIVER_ID)).thenReturn(true);
            when(drivingEventRepository.save(any(DrivingEvent.class))).thenAnswer(inv -> {
                DrivingEvent event = inv.getArgument(0);
                event.setId(1L);
                return event;
            });
            when(driverRepository.findById(DRIVER_ID)).thenReturn(Optional.of(testDriver));
            when(drivingEventRepository.findByDriverIdAndEventTimeBetween(eq(DRIVER_ID), any(), any()))
                    .thenReturn(Collections.emptyList());

            // Act
            drivingEventService.recordEvent(request);

            // Assert
            verify(drivingEventRepository).save(argThat(event -> 
                    event.getSeverity() == DrivingEvent.Severity.CRITICAL));
        }
    }

    @Nested
    @DisplayName("Event Count Tests")
    class EventCountTests {

        @Test
        @DisplayName("Should get correct event count by type")
        void getEventCountByType_Success() {
            // Arrange
            when(drivingEventRepository.countByDriverIdAndEventTypeAndEventTimeBetween(
                    eq(DRIVER_ID), eq(DrivingEvent.EventType.SPEEDING), any(), any()))
                    .thenReturn(5L);

            // Act
            long count = drivingEventService.getEventCountByType(
                    DRIVER_ID, DrivingEvent.EventType.SPEEDING,
                    LocalDateTime.now().minusDays(30), LocalDateTime.now());

            // Assert
            assertEquals(5L, count);
        }
    }
}
