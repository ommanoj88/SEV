package com.evfleet.charging.service;

import com.evfleet.charging.model.ChargingSession;
import com.evfleet.charging.model.ChargingStation;
import com.evfleet.charging.repository.ChargingSessionRepository;
import com.evfleet.charging.repository.ChargingStationRepository;
import com.evfleet.common.event.EventPublisher;
import com.evfleet.common.exception.ResourceNotFoundException;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ChargingSessionService
 * 
 * PR #7: Tests for 2-wheeler charging restriction and session management
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class ChargingSessionServiceTest {

    @Mock
    private ChargingSessionRepository sessionRepository;

    @Mock
    private ChargingStationRepository stationRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private EventPublisher eventPublisher;

    private ChargingSessionService chargingSessionService;

    @BeforeEach
    void setUp() {
        chargingSessionService = new ChargingSessionService(
            sessionRepository,
            stationRepository,
            vehicleRepository,
            eventPublisher
        );
    }

    // ===== 2-Wheeler Charging Restriction Tests =====

    @Test
    void testStartSession_TwoWheeler_ThrowsException() {
        // Setup 2-wheeler vehicle
        Vehicle twoWheeler = Vehicle.builder()
            .id(1L)
            .type(Vehicle.VehicleType.TWO_WHEELER)
            .vehicleNumber("TW-001")
            .build();

        when(vehicleRepository.findById(1L)).thenReturn(Optional.of(twoWheeler));

        // Attempt to start charging session
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> chargingSessionService.startSession(1L, 100L, 1L, 50.0));

        // Verify error message mentions 2-wheelers
        assertTrue(exception.getMessage().contains("2-wheeler"));
        assertTrue(exception.getMessage().contains("GPS location only"));
        
        // Verify no session was created
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void testStartSession_ThreeWheeler_ThrowsException() {
        // Setup 3-wheeler vehicle
        Vehicle threeWheeler = Vehicle.builder()
            .id(2L)
            .type(Vehicle.VehicleType.THREE_WHEELER)
            .vehicleNumber("3W-001")
            .build();

        when(vehicleRepository.findById(2L)).thenReturn(Optional.of(threeWheeler));

        // Attempt to start charging session
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> chargingSessionService.startSession(2L, 100L, 1L, 50.0));

        // Verify error message mentions 3-wheelers
        assertTrue(exception.getMessage().contains("3-wheeler"));
        assertTrue(exception.getMessage().contains("GPS location only"));
        
        // Verify no session was created
        verify(sessionRepository, never()).save(any());
    }

    @Test
    void testStartSession_LCV_Success() {
        // Setup LCV (4-wheeler) vehicle
        Vehicle lcv = Vehicle.builder()
            .id(3L)
            .type(Vehicle.VehicleType.LCV)
            .vehicleNumber("LCV-001")
            .build();

        ChargingStation station = ChargingStation.builder()
            .id(100L)
            .availableSlots(5)
            .totalSlots(10)
            .status(ChargingStation.StationStatus.AVAILABLE)
            .build();

        when(vehicleRepository.findById(3L)).thenReturn(Optional.of(lcv));
        when(sessionRepository.findByVehicleIdAndStatus(3L, ChargingSession.SessionStatus.ACTIVE))
            .thenReturn(Optional.empty());
        when(stationRepository.findById(100L)).thenReturn(Optional.of(station));
        when(stationRepository.decrementAvailableSlots(100L)).thenReturn(1);
        when(sessionRepository.save(any())).thenAnswer(i -> {
            ChargingSession session = i.getArgument(0);
            session.setId(1L);
            return session;
        });

        // Start charging session
        ChargingSession result = chargingSessionService.startSession(3L, 100L, 1L, 50.0);

        // Verify session was created
        assertNotNull(result);
        assertEquals(3L, result.getVehicleId());
        assertEquals(100L, result.getStationId());
        assertEquals(ChargingSession.SessionStatus.ACTIVE, result.getStatus());
    }

    // ===== SOC Validation Tests =====

    @Test
    void testStartSession_InvalidSOC_TooHigh_ThrowsException() {
        Vehicle lcv = Vehicle.builder()
            .id(3L)
            .type(Vehicle.VehicleType.LCV)
            .vehicleNumber("LCV-001")
            .build();

        when(vehicleRepository.findById(3L)).thenReturn(Optional.of(lcv));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> chargingSessionService.startSession(3L, 100L, 1L, 150.0));

        assertTrue(exception.getMessage().contains("SOC"));
        assertTrue(exception.getMessage().contains("0 and 100"));
    }

    @Test
    void testStartSession_InvalidSOC_Negative_ThrowsException() {
        Vehicle lcv = Vehicle.builder()
            .id(3L)
            .type(Vehicle.VehicleType.LCV)
            .vehicleNumber("LCV-001")
            .build();

        when(vehicleRepository.findById(3L)).thenReturn(Optional.of(lcv));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> chargingSessionService.startSession(3L, 100L, 1L, -10.0));

        assertTrue(exception.getMessage().contains("SOC"));
    }

    // ===== Vehicle Not Found Tests =====

    @Test
    void testStartSession_VehicleNotFound_ThrowsException() {
        when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> chargingSessionService.startSession(999L, 100L, 1L, 50.0));

        assertTrue(exception.getMessage().contains("Vehicle"));
    }

    // ===== Active Session Tests =====

    @Test
    void testStartSession_AlreadyHasActiveSession_ThrowsException() {
        Vehicle lcv = Vehicle.builder()
            .id(3L)
            .type(Vehicle.VehicleType.LCV)
            .vehicleNumber("LCV-001")
            .build();

        ChargingSession existingSession = ChargingSession.builder()
            .id(50L)
            .vehicleId(3L)
            .status(ChargingSession.SessionStatus.ACTIVE)
            .build();

        when(vehicleRepository.findById(3L)).thenReturn(Optional.of(lcv));
        when(sessionRepository.findByVehicleIdAndStatus(3L, ChargingSession.SessionStatus.ACTIVE))
            .thenReturn(Optional.of(existingSession));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> chargingSessionService.startSession(3L, 100L, 1L, 50.0));

        assertTrue(exception.getMessage().contains("already has an active charging session"));
    }

    // ===== Station Availability Tests =====

    @Test
    void testStartSession_NoAvailableSlots_ThrowsException() {
        Vehicle lcv = Vehicle.builder()
            .id(3L)
            .type(Vehicle.VehicleType.LCV)
            .vehicleNumber("LCV-001")
            .build();

        ChargingStation station = ChargingStation.builder()
            .id(100L)
            .availableSlots(0)  // No slots available
            .totalSlots(10)
            .status(ChargingStation.StationStatus.FULL)
            .build();

        when(vehicleRepository.findById(3L)).thenReturn(Optional.of(lcv));
        when(sessionRepository.findByVehicleIdAndStatus(3L, ChargingSession.SessionStatus.ACTIVE))
            .thenReturn(Optional.empty());
        when(stationRepository.findById(100L)).thenReturn(Optional.of(station));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> chargingSessionService.startSession(3L, 100L, 1L, 50.0));

        assertTrue(exception.getMessage().contains("No available slots"));
    }

    @Test
    void testStartSession_StationNotFound_ThrowsException() {
        Vehicle lcv = Vehicle.builder()
            .id(3L)
            .type(Vehicle.VehicleType.LCV)
            .vehicleNumber("LCV-001")
            .build();

        when(vehicleRepository.findById(3L)).thenReturn(Optional.of(lcv));
        when(sessionRepository.findByVehicleIdAndStatus(3L, ChargingSession.SessionStatus.ACTIVE))
            .thenReturn(Optional.empty());
        when(stationRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> chargingSessionService.startSession(3L, 999L, 1L, 50.0));

        assertTrue(exception.getMessage().contains("ChargingStation"));
    }

    // ===== Thread-Safe Slot Reservation Tests =====

    @Test
    void testStartSession_ConcurrentSlotReservation_RaceConditionHandled() {
        Vehicle lcv = Vehicle.builder()
            .id(3L)
            .type(Vehicle.VehicleType.LCV)
            .vehicleNumber("LCV-001")
            .build();

        ChargingStation station = ChargingStation.builder()
            .id(100L)
            .availableSlots(1)  // Only 1 slot available
            .totalSlots(10)
            .status(ChargingStation.StationStatus.AVAILABLE)
            .build();

        when(vehicleRepository.findById(3L)).thenReturn(Optional.of(lcv));
        when(sessionRepository.findByVehicleIdAndStatus(3L, ChargingSession.SessionStatus.ACTIVE))
            .thenReturn(Optional.empty());
        when(stationRepository.findById(100L)).thenReturn(Optional.of(station));
        // Simulate race condition: decrement returns 0 (slot taken by another thread)
        when(stationRepository.decrementAvailableSlots(100L)).thenReturn(0);

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> chargingSessionService.startSession(3L, 100L, 1L, 50.0));

        assertTrue(exception.getMessage().contains("No available slots"));
    }

    // ===== Null SOC Tests =====

    @Test
    void testStartSession_NullSOC_Allowed() {
        Vehicle lcv = Vehicle.builder()
            .id(3L)
            .type(Vehicle.VehicleType.LCV)
            .vehicleNumber("LCV-001")
            .build();

        ChargingStation station = ChargingStation.builder()
            .id(100L)
            .availableSlots(5)
            .totalSlots(10)
            .status(ChargingStation.StationStatus.AVAILABLE)
            .build();

        when(vehicleRepository.findById(3L)).thenReturn(Optional.of(lcv));
        when(sessionRepository.findByVehicleIdAndStatus(3L, ChargingSession.SessionStatus.ACTIVE))
            .thenReturn(Optional.empty());
        when(stationRepository.findById(100L)).thenReturn(Optional.of(station));
        when(stationRepository.decrementAvailableSlots(100L)).thenReturn(1);
        when(sessionRepository.save(any())).thenAnswer(i -> {
            ChargingSession session = i.getArgument(0);
            session.setId(1L);
            return session;
        });

        // Start charging session with null SOC (should be allowed)
        ChargingSession result = chargingSessionService.startSession(3L, 100L, 1L, null);

        assertNotNull(result);
        assertNull(result.getInitialSoc());
    }

    // ===== Boundary SOC Tests =====

    @Test
    void testStartSession_SOC_Zero_Allowed() {
        Vehicle lcv = Vehicle.builder()
            .id(3L)
            .type(Vehicle.VehicleType.LCV)
            .vehicleNumber("LCV-001")
            .build();

        ChargingStation station = ChargingStation.builder()
            .id(100L)
            .availableSlots(5)
            .totalSlots(10)
            .status(ChargingStation.StationStatus.AVAILABLE)
            .build();

        when(vehicleRepository.findById(3L)).thenReturn(Optional.of(lcv));
        when(sessionRepository.findByVehicleIdAndStatus(3L, ChargingSession.SessionStatus.ACTIVE))
            .thenReturn(Optional.empty());
        when(stationRepository.findById(100L)).thenReturn(Optional.of(station));
        when(stationRepository.decrementAvailableSlots(100L)).thenReturn(1);
        when(sessionRepository.save(any())).thenAnswer(i -> {
            ChargingSession session = i.getArgument(0);
            session.setId(1L);
            return session;
        });

        // Start charging session with 0% SOC
        ChargingSession result = chargingSessionService.startSession(3L, 100L, 1L, 0.0);

        assertNotNull(result);
        assertEquals(0.0, result.getInitialSoc());
    }

    @Test
    void testStartSession_SOC_Hundred_Allowed() {
        Vehicle lcv = Vehicle.builder()
            .id(3L)
            .type(Vehicle.VehicleType.LCV)
            .vehicleNumber("LCV-001")
            .build();

        ChargingStation station = ChargingStation.builder()
            .id(100L)
            .availableSlots(5)
            .totalSlots(10)
            .status(ChargingStation.StationStatus.AVAILABLE)
            .build();

        when(vehicleRepository.findById(3L)).thenReturn(Optional.of(lcv));
        when(sessionRepository.findByVehicleIdAndStatus(3L, ChargingSession.SessionStatus.ACTIVE))
            .thenReturn(Optional.empty());
        when(stationRepository.findById(100L)).thenReturn(Optional.of(station));
        when(stationRepository.decrementAvailableSlots(100L)).thenReturn(1);
        when(sessionRepository.save(any())).thenAnswer(i -> {
            ChargingSession session = i.getArgument(0);
            session.setId(1L);
            return session;
        });

        // Start charging session with 100% SOC (edge case - maybe just need to maintain?)
        ChargingSession result = chargingSessionService.startSession(3L, 100L, 1L, 100.0);

        assertNotNull(result);
        assertEquals(100.0, result.getInitialSoc());
    }
}
