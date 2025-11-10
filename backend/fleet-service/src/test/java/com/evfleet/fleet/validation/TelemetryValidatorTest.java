package com.evfleet.fleet.validation;

import com.evfleet.fleet.dto.TelemetryRequest;
import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TelemetryValidator
 */
@ExtendWith(MockitoExtension.class)
class TelemetryValidatorTest {

    @InjectMocks
    private TelemetryValidator telemetryValidator;

    private Vehicle evVehicle;
    private Vehicle iceVehicle;
    private Vehicle hybridVehicle;
    private TelemetryRequest telemetryRequest;

    @BeforeEach
    void setUp() {
        // Setup EV vehicle
        evVehicle = new Vehicle();
        evVehicle.setId(1L);
        evVehicle.setFuelType(FuelType.EV);
        evVehicle.setBatteryCapacity(60.0);

        // Setup ICE vehicle
        iceVehicle = new Vehicle();
        iceVehicle.setId(2L);
        iceVehicle.setFuelType(FuelType.ICE);
        iceVehicle.setFuelTankCapacity(50.0);

        // Setup HYBRID vehicle
        hybridVehicle = new Vehicle();
        hybridVehicle.setId(3L);
        hybridVehicle.setFuelType(FuelType.HYBRID);
        hybridVehicle.setBatteryCapacity(20.0);
        hybridVehicle.setFuelTankCapacity(40.0);

        // Setup basic telemetry request
        telemetryRequest = new TelemetryRequest();
        telemetryRequest.setVehicleId(1L);
        telemetryRequest.setLatitude(12.9716);
        telemetryRequest.setLongitude(77.5946);
        telemetryRequest.setTimestamp(LocalDateTime.now());
    }

    // ===== EV VEHICLE TESTS =====

    @Test
    void testValidateEVTelemetry_ValidBatterySoc_ShouldPass() {
        telemetryRequest.setBatterySoc(75.0);
        
        assertDoesNotThrow(() -> 
            telemetryValidator.validateTelemetryForVehicle(telemetryRequest, evVehicle)
        );
    }

    @Test
    void testValidateEVTelemetry_InvalidBatterySoc_Negative_ShouldThrowException() {
        telemetryRequest.setBatterySoc(-5.0);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            telemetryValidator.validateTelemetryForVehicle(telemetryRequest, evVehicle)
        );
        
        assertTrue(exception.getMessage().contains("Invalid battery SOC"));
        assertTrue(exception.getMessage().contains("Must be between 0 and 100"));
    }

    @Test
    void testValidateEVTelemetry_InvalidBatterySoc_OverHundred_ShouldThrowException() {
        telemetryRequest.setBatterySoc(105.0);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            telemetryValidator.validateTelemetryForVehicle(telemetryRequest, evVehicle)
        );
        
        assertTrue(exception.getMessage().contains("Invalid battery SOC"));
        assertTrue(exception.getMessage().contains("Must be between 0 and 100"));
    }

    @Test
    void testValidateEVTelemetry_WithICEFields_ShouldPassWithWarning() {
        // EV telemetry with ICE fields should pass but log warning
        telemetryRequest.setBatterySoc(80.0);
        telemetryRequest.setFuelLevel(25.0); // ICE field
        telemetryRequest.setEngineRpm(2000); // ICE field
        
        assertDoesNotThrow(() -> 
            telemetryValidator.validateTelemetryForVehicle(telemetryRequest, evVehicle)
        );
    }

    @Test
    void testValidateEVTelemetry_NullFuelType_ShouldDefaultToEV() {
        Vehicle vehicleWithNullFuelType = new Vehicle();
        vehicleWithNullFuelType.setId(4L);
        vehicleWithNullFuelType.setFuelType(null); // null fuel type
        
        telemetryRequest.setBatterySoc(50.0);
        
        assertDoesNotThrow(() -> 
            telemetryValidator.validateTelemetryForVehicle(telemetryRequest, vehicleWithNullFuelType)
        );
    }

    // ===== ICE VEHICLE TESTS =====

    @Test
    void testValidateICETelemetry_ValidFuelLevel_ShouldPass() {
        telemetryRequest.setFuelLevel(30.0);
        telemetryRequest.setEngineRpm(2500);
        
        assertDoesNotThrow(() -> 
            telemetryValidator.validateTelemetryForVehicle(telemetryRequest, iceVehicle)
        );
    }

    @Test
    void testValidateICETelemetry_InvalidFuelLevel_Negative_ShouldThrowException() {
        telemetryRequest.setFuelLevel(-10.0);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            telemetryValidator.validateTelemetryForVehicle(telemetryRequest, iceVehicle)
        );
        
        assertTrue(exception.getMessage().contains("Invalid fuel level"));
        assertTrue(exception.getMessage().contains("Must be non-negative"));
    }

    @Test
    void testValidateICETelemetry_InvalidEngineRpm_Negative_ShouldThrowException() {
        telemetryRequest.setFuelLevel(25.0);
        telemetryRequest.setEngineRpm(-100);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            telemetryValidator.validateTelemetryForVehicle(telemetryRequest, iceVehicle)
        );
        
        assertTrue(exception.getMessage().contains("Invalid engine RPM"));
        assertTrue(exception.getMessage().contains("Must be between 0 and 10000"));
    }

    @Test
    void testValidateICETelemetry_InvalidEngineRpm_TooHigh_ShouldThrowException() {
        telemetryRequest.setFuelLevel(25.0);
        telemetryRequest.setEngineRpm(15000);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            telemetryValidator.validateTelemetryForVehicle(telemetryRequest, iceVehicle)
        );
        
        assertTrue(exception.getMessage().contains("Invalid engine RPM"));
        assertTrue(exception.getMessage().contains("Must be between 0 and 10000"));
    }

    @Test
    void testValidateICETelemetry_InvalidEngineLoad_Negative_ShouldThrowException() {
        telemetryRequest.setFuelLevel(25.0);
        telemetryRequest.setEngineLoad(-5.0);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            telemetryValidator.validateTelemetryForVehicle(telemetryRequest, iceVehicle)
        );
        
        assertTrue(exception.getMessage().contains("Invalid engine load"));
        assertTrue(exception.getMessage().contains("Must be between 0 and 100"));
    }

    @Test
    void testValidateICETelemetry_InvalidEngineLoad_TooHigh_ShouldThrowException() {
        telemetryRequest.setFuelLevel(25.0);
        telemetryRequest.setEngineLoad(150.0);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            telemetryValidator.validateTelemetryForVehicle(telemetryRequest, iceVehicle)
        );
        
        assertTrue(exception.getMessage().contains("Invalid engine load"));
        assertTrue(exception.getMessage().contains("Must be between 0 and 100"));
    }

    @Test
    void testValidateICETelemetry_InvalidEngineHours_Negative_ShouldThrowException() {
        telemetryRequest.setFuelLevel(25.0);
        telemetryRequest.setEngineHours(-100.0);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            telemetryValidator.validateTelemetryForVehicle(telemetryRequest, iceVehicle)
        );
        
        assertTrue(exception.getMessage().contains("Invalid engine hours"));
        assertTrue(exception.getMessage().contains("Must be non-negative"));
    }

    @Test
    void testValidateICETelemetry_WithChargingFlag_ShouldPassWithWarning() {
        // ICE vehicle with charging flag should pass but log warning
        telemetryRequest.setFuelLevel(30.0);
        telemetryRequest.setIsCharging(true); // EV field
        
        assertDoesNotThrow(() -> 
            telemetryValidator.validateTelemetryForVehicle(telemetryRequest, iceVehicle)
        );
    }

    // ===== HYBRID VEHICLE TESTS =====

    @Test
    void testValidateHybridTelemetry_ValidBothMetrics_ShouldPass() {
        telemetryRequest.setBatterySoc(60.0);
        telemetryRequest.setFuelLevel(20.0);
        telemetryRequest.setEngineRpm(1800);
        
        assertDoesNotThrow(() -> 
            telemetryValidator.validateTelemetryForVehicle(telemetryRequest, hybridVehicle)
        );
    }

    @Test
    void testValidateHybridTelemetry_InvalidBatterySoc_ShouldThrowException() {
        telemetryRequest.setBatterySoc(120.0);
        telemetryRequest.setFuelLevel(20.0);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            telemetryValidator.validateTelemetryForVehicle(telemetryRequest, hybridVehicle)
        );
        
        assertTrue(exception.getMessage().contains("Invalid battery SOC"));
    }

    @Test
    void testValidateHybridTelemetry_InvalidFuelLevel_ShouldThrowException() {
        telemetryRequest.setBatterySoc(60.0);
        telemetryRequest.setFuelLevel(-5.0);
        
        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class, () ->
            telemetryValidator.validateTelemetryForVehicle(telemetryRequest, hybridVehicle)
        );
        
        assertTrue(exception.getMessage().contains("Invalid fuel level"));
    }

    @Test
    void testValidateHybridTelemetry_OnlyBatteryMetrics_ShouldPass() {
        // Hybrid running in EV mode - only battery metrics
        telemetryRequest.setBatterySoc(70.0);
        telemetryRequest.setBatteryVoltage(400.0);
        
        assertDoesNotThrow(() -> 
            telemetryValidator.validateTelemetryForVehicle(telemetryRequest, hybridVehicle)
        );
    }

    @Test
    void testValidateHybridTelemetry_OnlyEngineMetrics_ShouldPass() {
        // Hybrid running in ICE mode - only engine metrics
        telemetryRequest.setFuelLevel(25.0);
        telemetryRequest.setEngineRpm(2200);
        
        assertDoesNotThrow(() -> 
            telemetryValidator.validateTelemetryForVehicle(telemetryRequest, hybridVehicle)
        );
    }

    // ===== EDGE CASES =====

    @Test
    void testValidateTelemetry_NullBatterySoc_ShouldPass() {
        telemetryRequest.setBatterySoc(null);
        
        assertDoesNotThrow(() -> 
            telemetryValidator.validateTelemetryForVehicle(telemetryRequest, evVehicle)
        );
    }

    @Test
    void testValidateTelemetry_ZeroBatterySoc_ShouldPass() {
        telemetryRequest.setBatterySoc(0.0);
        
        assertDoesNotThrow(() -> 
            telemetryValidator.validateTelemetryForVehicle(telemetryRequest, evVehicle)
        );
    }

    @Test
    void testValidateTelemetry_HundredBatterySoc_ShouldPass() {
        telemetryRequest.setBatterySoc(100.0);
        
        assertDoesNotThrow(() -> 
            telemetryValidator.validateTelemetryForVehicle(telemetryRequest, evVehicle)
        );
    }

    @Test
    void testValidateTelemetry_ZeroFuelLevel_ShouldPass() {
        telemetryRequest.setFuelLevel(0.0);
        
        assertDoesNotThrow(() -> 
            telemetryValidator.validateTelemetryForVehicle(telemetryRequest, iceVehicle)
        );
    }

    @Test
    void testValidateTelemetry_ValidEngineRpm_Boundary_ShouldPass() {
        telemetryRequest.setFuelLevel(25.0);
        telemetryRequest.setEngineRpm(0);
        
        assertDoesNotThrow(() -> 
            telemetryValidator.validateTelemetryForVehicle(telemetryRequest, iceVehicle)
        );
        
        telemetryRequest.setEngineRpm(10000);
        
        assertDoesNotThrow(() -> 
            telemetryValidator.validateTelemetryForVehicle(telemetryRequest, iceVehicle)
        );
    }

    @Test
    void testValidateTelemetry_ValidEngineLoad_Boundary_ShouldPass() {
        telemetryRequest.setFuelLevel(25.0);
        telemetryRequest.setEngineLoad(0.0);
        
        assertDoesNotThrow(() -> 
            telemetryValidator.validateTelemetryForVehicle(telemetryRequest, iceVehicle)
        );
        
        telemetryRequest.setEngineLoad(100.0);
        
        assertDoesNotThrow(() -> 
            telemetryValidator.validateTelemetryForVehicle(telemetryRequest, iceVehicle)
        );
    }
}
