package com.evfleet.charging.service;

import com.evfleet.charging.client.FleetServiceClient;
import com.evfleet.charging.dto.ChargingSessionRequest;
import com.evfleet.charging.dto.ChargingSessionResponse;
import com.evfleet.charging.dto.VehicleDTO;
import com.evfleet.charging.entity.ChargingSession;
import com.evfleet.charging.entity.ChargingStation;
import com.evfleet.charging.event.EventPublisher;
import com.evfleet.charging.exception.NotAnEVVehicleException;
import com.evfleet.charging.repository.ChargingSessionRepository;
import com.evfleet.charging.validation.VehicleTypeValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ChargingSessionServiceImpl
 * Tests PR-9: Charging Validation integration
 */
@ExtendWith(MockitoExtension.class)
class ChargingSessionServiceImplTest {

    @Mock
    private ChargingSessionRepository sessionRepository;

    @Mock
    private ChargingStationService stationService;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private VehicleTypeValidator vehicleTypeValidator;

    @InjectMocks
    private ChargingSessionServiceImpl chargingSessionService;

    private ChargingSessionRequest validRequest;
    private ChargingStation mockStation;

    @BeforeEach
    void setUp() {
        validRequest = new ChargingSessionRequest();
        validRequest.setVehicleId(1L);
        validRequest.setStationId(1L);
        validRequest.setStartBatteryLevel(BigDecimal.valueOf(50.0));
        validRequest.setPaymentMethod("CREDIT_CARD");

        mockStation = new ChargingStation();
        mockStation.setId(1L);
        mockStation.setName("Test Station");
    }

    @Test
    void testStartSession_EVVehicle_Success() {
        // Given
        doNothing().when(vehicleTypeValidator).validateVehicleCanCharge(1L);
        when(sessionRepository.findActiveSessionByVehicleId(1L)).thenReturn(Optional.empty());
        when(stationService.reserveSlot(1L)).thenReturn(mockStation);
        when(sessionRepository.save(any(ChargingSession.class))).thenAnswer(invocation -> {
            ChargingSession session = invocation.getArgument(0);
            session.setId(1L);
            return session;
        });

        // When
        ChargingSessionResponse response = chargingSessionService.startSession(validRequest);

        // Then
        assertNotNull(response);
        verify(vehicleTypeValidator, times(1)).validateVehicleCanCharge(1L);
        verify(sessionRepository, times(1)).save(any(ChargingSession.class));
        verify(eventPublisher, times(1)).publishChargingSessionStarted(any(ChargingSession.class));
    }

    @Test
    void testStartSession_ICEVehicle_ThrowsException() {
        // Given
        doThrow(new NotAnEVVehicleException(1L, "ICE"))
            .when(vehicleTypeValidator).validateVehicleCanCharge(1L);

        // When & Then
        assertThrows(NotAnEVVehicleException.class, () -> {
            chargingSessionService.startSession(validRequest);
        });

        verify(vehicleTypeValidator, times(1)).validateVehicleCanCharge(1L);
        verify(sessionRepository, never()).save(any(ChargingSession.class));
        verify(eventPublisher, never()).publishChargingSessionStarted(any(ChargingSession.class));
    }

    @Test
    void testStartSession_VehicleAlreadyCharging_ThrowsException() {
        // Given
        ChargingSession existingSession = new ChargingSession();
        existingSession.setId(1L);
        existingSession.setStatus(ChargingSession.SessionStatus.CHARGING);

        doNothing().when(vehicleTypeValidator).validateVehicleCanCharge(1L);
        when(sessionRepository.findActiveSessionByVehicleId(1L)).thenReturn(Optional.of(existingSession));

        // When & Then
        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            chargingSessionService.startSession(validRequest);
        });

        assertTrue(exception.getMessage().contains("already has an active charging session"));
        verify(vehicleTypeValidator, times(1)).validateVehicleCanCharge(1L);
        verify(sessionRepository, never()).save(any(ChargingSession.class));
    }

    @Test
    void testStartSession_HybridVehicle_Success() {
        // Given
        validRequest.setVehicleId(2L);

        doNothing().when(vehicleTypeValidator).validateVehicleCanCharge(2L);
        when(sessionRepository.findActiveSessionByVehicleId(2L)).thenReturn(Optional.empty());
        when(stationService.reserveSlot(1L)).thenReturn(mockStation);
        when(sessionRepository.save(any(ChargingSession.class))).thenAnswer(invocation -> {
            ChargingSession session = invocation.getArgument(0);
            session.setId(2L);
            return session;
        });

        // When
        ChargingSessionResponse response = chargingSessionService.startSession(validRequest);

        // Then
        assertNotNull(response);
        verify(vehicleTypeValidator, times(1)).validateVehicleCanCharge(2L);
        verify(sessionRepository, times(1)).save(any(ChargingSession.class));
    }
}
