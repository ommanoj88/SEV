package com.evfleet.charging.validation;

import com.evfleet.charging.client.FleetServiceClient;
import com.evfleet.charging.dto.VehicleDTO;
import com.evfleet.charging.exception.NotAnEVVehicleException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VehicleTypeValidator
 * Tests PR-9: Charging Validation
 */
@ExtendWith(MockitoExtension.class)
class VehicleTypeValidatorTest {

    @Mock
    private FleetServiceClient fleetServiceClient;

    @InjectMocks
    private VehicleTypeValidator vehicleTypeValidator;

    private VehicleDTO evVehicle;
    private VehicleDTO hybridVehicle;
    private VehicleDTO iceVehicle;

    @BeforeEach
    void setUp() {
        evVehicle = new VehicleDTO();
        evVehicle.setId(1L);
        evVehicle.setVehicleNumber("EV-001");
        evVehicle.setFuelType("EV");
        evVehicle.setBatteryCapacity(60.0);
        evVehicle.setCurrentBatterySoc(50.0);
        evVehicle.setStatus("ACTIVE");

        hybridVehicle = new VehicleDTO();
        hybridVehicle.setId(2L);
        hybridVehicle.setVehicleNumber("HYB-001");
        hybridVehicle.setFuelType("HYBRID");
        hybridVehicle.setBatteryCapacity(40.0);
        hybridVehicle.setCurrentBatterySoc(60.0);
        hybridVehicle.setStatus("ACTIVE");

        iceVehicle = new VehicleDTO();
        iceVehicle.setId(3L);
        iceVehicle.setVehicleNumber("ICE-001");
        iceVehicle.setFuelType("ICE");
        iceVehicle.setStatus("ACTIVE");
    }

    @Test
    void testValidateVehicleCanCharge_EVVehicle_Success() {
        // Given
        when(fleetServiceClient.getVehicleById(1L)).thenReturn(evVehicle);

        // When & Then
        assertDoesNotThrow(() -> vehicleTypeValidator.validateVehicleCanCharge(1L));
        verify(fleetServiceClient, times(1)).getVehicleById(1L);
    }

    @Test
    void testValidateVehicleCanCharge_HybridVehicle_Success() {
        // Given
        when(fleetServiceClient.getVehicleById(2L)).thenReturn(hybridVehicle);

        // When & Then
        assertDoesNotThrow(() -> vehicleTypeValidator.validateVehicleCanCharge(2L));
        verify(fleetServiceClient, times(1)).getVehicleById(2L);
    }

    @Test
    void testValidateVehicleCanCharge_ICEVehicle_ThrowsException() {
        // Given
        when(fleetServiceClient.getVehicleById(3L)).thenReturn(iceVehicle);

        // When & Then
        NotAnEVVehicleException exception = assertThrows(
            NotAnEVVehicleException.class,
            () -> vehicleTypeValidator.validateVehicleCanCharge(3L)
        );

        assertEquals(3L, exception.getVehicleId());
        assertEquals("ICE", exception.getFuelType());
        assertTrue(exception.getMessage().contains("does not support charging"));
        verify(fleetServiceClient, times(1)).getVehicleById(3L);
    }

    @Test
    void testValidateVehicleCanCharge_NullFuelType_ThrowsException() {
        // Given
        VehicleDTO vehicleWithNullFuelType = new VehicleDTO();
        vehicleWithNullFuelType.setId(4L);
        vehicleWithNullFuelType.setVehicleNumber("NULL-001");
        vehicleWithNullFuelType.setFuelType(null);

        when(fleetServiceClient.getVehicleById(4L)).thenReturn(vehicleWithNullFuelType);

        // When & Then
        NotAnEVVehicleException exception = assertThrows(
            NotAnEVVehicleException.class,
            () -> vehicleTypeValidator.validateVehicleCanCharge(4L)
        );

        assertEquals(4L, exception.getVehicleId());
        assertEquals("UNKNOWN", exception.getFuelType());
        verify(fleetServiceClient, times(1)).getVehicleById(4L);
    }

    @Test
    void testValidateVehicleCanCharge_NullVehicle_ThrowsException() {
        // Given
        when(fleetServiceClient.getVehicleById(999L)).thenReturn(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> vehicleTypeValidator.validateVehicleCanCharge(999L)
        );

        assertTrue(exception.getMessage().contains("Vehicle not found"));
        verify(fleetServiceClient, times(1)).getVehicleById(999L);
    }

    @Test
    void testValidateVehicleCanCharge_UnknownFuelType_ThrowsException() {
        // Given
        VehicleDTO vehicleWithUnknownType = new VehicleDTO();
        vehicleWithUnknownType.setId(5L);
        vehicleWithUnknownType.setVehicleNumber("UNK-001");
        vehicleWithUnknownType.setFuelType("DIESEL");

        when(fleetServiceClient.getVehicleById(5L)).thenReturn(vehicleWithUnknownType);

        // When & Then
        NotAnEVVehicleException exception = assertThrows(
            NotAnEVVehicleException.class,
            () -> vehicleTypeValidator.validateVehicleCanCharge(5L)
        );

        assertEquals(5L, exception.getVehicleId());
        assertEquals("DIESEL", exception.getFuelType());
        verify(fleetServiceClient, times(1)).getVehicleById(5L);
    }
}
