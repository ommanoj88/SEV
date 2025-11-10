package com.evfleet.fleet.service;

import com.evfleet.fleet.dto.TelemetryRequest;
import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.model.TelemetryData;
import com.evfleet.fleet.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TelemetryProcessingService
 */
@ExtendWith(MockitoExtension.class)
class TelemetryProcessingServiceTest {

    @InjectMocks
    private TelemetryProcessingService telemetryProcessingService;

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

        // Setup ICE vehicle
        iceVehicle = new Vehicle();
        iceVehicle.setId(2L);
        iceVehicle.setFuelType(FuelType.ICE);

        // Setup HYBRID vehicle
        hybridVehicle = new Vehicle();
        hybridVehicle.setId(3L);
        hybridVehicle.setFuelType(FuelType.HYBRID);

        // Setup telemetry request with all fields
        telemetryRequest = new TelemetryRequest();
        telemetryRequest.setVehicleId(1L);
        telemetryRequest.setLatitude(12.9716);
        telemetryRequest.setLongitude(77.5946);
        telemetryRequest.setSpeed(60.0);
        telemetryRequest.setTimestamp(LocalDateTime.now());
        telemetryRequest.setHeading(90.0);
        telemetryRequest.setAltitude(920.0);
        telemetryRequest.setOdometer(15000.0);
        telemetryRequest.setIsIgnitionOn(true);
        telemetryRequest.setTripId(100L);
        telemetryRequest.setSignalStrength(85);

        // EV-specific fields
        telemetryRequest.setBatterySoc(75.0);
        telemetryRequest.setBatteryVoltage(380.0);
        telemetryRequest.setBatteryCurrent(50.0);
        telemetryRequest.setBatteryTemperature(35.0);
        telemetryRequest.setPowerConsumption(25.5);
        telemetryRequest.setRegenerativePower(5.2);
        telemetryRequest.setMotorTemperature(68.0);
        telemetryRequest.setControllerTemperature(42.0);
        telemetryRequest.setIsCharging(false);

        // ICE-specific fields
        telemetryRequest.setFuelLevel(30.0);
        telemetryRequest.setEngineRpm(2500);
        telemetryRequest.setEngineTemperature(95.0);
        telemetryRequest.setEngineLoad(65.0);
        telemetryRequest.setEngineHours(1250.5);
    }

    // ===== EV VEHICLE TESTS =====

    @Test
    void testProcessEVTelemetry_ShouldSetEVFields() {
        TelemetryData result = telemetryProcessingService.processTelemetryData(telemetryRequest, evVehicle);

        // Common fields
        assertEquals(telemetryRequest.getVehicleId(), result.getVehicleId());
        assertEquals(telemetryRequest.getLatitude(), result.getLatitude());
        assertEquals(telemetryRequest.getLongitude(), result.getLongitude());
        assertEquals(telemetryRequest.getSpeed(), result.getSpeed());

        // EV-specific fields should be set
        assertEquals(telemetryRequest.getBatterySoc(), result.getBatterySoc());
        assertEquals(telemetryRequest.getBatteryVoltage(), result.getBatteryVoltage());
        assertEquals(telemetryRequest.getBatteryCurrent(), result.getBatteryCurrent());
        assertEquals(telemetryRequest.getBatteryTemperature(), result.getBatteryTemperature());
        assertEquals(telemetryRequest.getPowerConsumption(), result.getPowerConsumption());
        assertEquals(telemetryRequest.getRegenerativePower(), result.getRegenerativePower());
        assertEquals(telemetryRequest.getMotorTemperature(), result.getMotorTemperature());
        assertEquals(telemetryRequest.getControllerTemperature(), result.getControllerTemperature());
        assertEquals(telemetryRequest.getIsCharging(), result.getIsCharging());
    }

    @Test
    void testProcessEVTelemetry_ShouldNullifyICEFields() {
        TelemetryData result = telemetryProcessingService.processTelemetryData(telemetryRequest, evVehicle);

        // ICE-specific fields should be null for EV
        assertNull(result.getFuelLevel());
        assertNull(result.getEngineRpm());
        assertNull(result.getEngineTemperature());
        assertNull(result.getEngineLoad());
        assertNull(result.getEngineHours());
    }

    @Test
    void testProcessEVTelemetry_NullFuelType_ShouldDefaultToEV() {
        Vehicle vehicleWithNullFuelType = new Vehicle();
        vehicleWithNullFuelType.setId(4L);
        vehicleWithNullFuelType.setFuelType(null);

        TelemetryData result = telemetryProcessingService.processTelemetryData(telemetryRequest, vehicleWithNullFuelType);

        // Should process as EV (default)
        assertEquals(telemetryRequest.getBatterySoc(), result.getBatterySoc());
        assertNull(result.getFuelLevel());
    }

    // ===== ICE VEHICLE TESTS =====

    @Test
    void testProcessICETelemetry_ShouldSetICEFields() {
        TelemetryData result = telemetryProcessingService.processTelemetryData(telemetryRequest, iceVehicle);

        // Common fields
        assertEquals(telemetryRequest.getVehicleId(), result.getVehicleId());
        assertEquals(telemetryRequest.getLatitude(), result.getLatitude());
        assertEquals(telemetryRequest.getLongitude(), result.getLongitude());

        // ICE-specific fields should be set
        assertEquals(telemetryRequest.getFuelLevel(), result.getFuelLevel());
        assertEquals(telemetryRequest.getEngineRpm(), result.getEngineRpm());
        assertEquals(telemetryRequest.getEngineTemperature(), result.getEngineTemperature());
        assertEquals(telemetryRequest.getEngineLoad(), result.getEngineLoad());
        assertEquals(telemetryRequest.getEngineHours(), result.getEngineHours());
    }

    @Test
    void testProcessICETelemetry_ShouldNullifyEVFields() {
        TelemetryData result = telemetryProcessingService.processTelemetryData(telemetryRequest, iceVehicle);

        // Battery-related fields should be null for ICE
        assertNull(result.getBatterySoc());
        assertNull(result.getBatteryVoltage());
        assertNull(result.getBatteryCurrent());
        assertNull(result.getBatteryTemperature());
        assertNull(result.getPowerConsumption());
        assertNull(result.getRegenerativePower());
        assertNull(result.getMotorTemperature());
        assertNull(result.getControllerTemperature());
        assertNull(result.getIsCharging());
    }

    // ===== HYBRID VEHICLE TESTS =====

    @Test
    void testProcessHybridTelemetry_ShouldSetBothEVAndICEFields() {
        TelemetryData result = telemetryProcessingService.processTelemetryData(telemetryRequest, hybridVehicle);

        // Common fields
        assertEquals(telemetryRequest.getVehicleId(), result.getVehicleId());
        assertEquals(telemetryRequest.getLatitude(), result.getLatitude());

        // EV-specific fields should be set
        assertEquals(telemetryRequest.getBatterySoc(), result.getBatterySoc());
        assertEquals(telemetryRequest.getBatteryVoltage(), result.getBatteryVoltage());
        assertEquals(telemetryRequest.getPowerConsumption(), result.getPowerConsumption());
        assertEquals(telemetryRequest.getIsCharging(), result.getIsCharging());

        // ICE-specific fields should be set
        assertEquals(telemetryRequest.getFuelLevel(), result.getFuelLevel());
        assertEquals(telemetryRequest.getEngineRpm(), result.getEngineRpm());
        assertEquals(telemetryRequest.getEngineTemperature(), result.getEngineTemperature());
        assertEquals(telemetryRequest.getEngineLoad(), result.getEngineLoad());
    }

    @Test
    void testProcessHybridTelemetry_OnlyBatteryMetrics_ShouldSetOnlyBatteryFields() {
        // Simulate hybrid in EV mode
        TelemetryRequest evModeRequest = new TelemetryRequest();
        evModeRequest.setVehicleId(3L);
        evModeRequest.setLatitude(12.9716);
        evModeRequest.setLongitude(77.5946);
        evModeRequest.setTimestamp(LocalDateTime.now());
        evModeRequest.setBatterySoc(80.0);
        evModeRequest.setBatteryVoltage(400.0);
        // No engine metrics

        TelemetryData result = telemetryProcessingService.processTelemetryData(evModeRequest, hybridVehicle);

        assertEquals(80.0, result.getBatterySoc());
        assertEquals(400.0, result.getBatteryVoltage());
        assertNull(result.getFuelLevel());
        assertNull(result.getEngineRpm());
    }

    @Test
    void testProcessHybridTelemetry_OnlyEngineMetrics_ShouldSetOnlyEngineFields() {
        // Simulate hybrid in ICE mode
        TelemetryRequest iceModeRequest = new TelemetryRequest();
        iceModeRequest.setVehicleId(3L);
        iceModeRequest.setLatitude(12.9716);
        iceModeRequest.setLongitude(77.5946);
        iceModeRequest.setTimestamp(LocalDateTime.now());
        iceModeRequest.setFuelLevel(25.0);
        iceModeRequest.setEngineRpm(2200);
        // No battery metrics

        TelemetryData result = telemetryProcessingService.processTelemetryData(iceModeRequest, hybridVehicle);

        assertEquals(25.0, result.getFuelLevel());
        assertEquals(2200, result.getEngineRpm());
        assertNull(result.getBatterySoc());
        assertNull(result.getBatteryVoltage());
    }

    // ===== COMMON FIELDS TESTS =====

    @Test
    void testProcessTelemetry_CommonFieldsSetForAllTypes() {
        // Test EV
        TelemetryData evResult = telemetryProcessingService.processTelemetryData(telemetryRequest, evVehicle);
        verifyCommonFields(evResult);

        // Test ICE
        TelemetryData iceResult = telemetryProcessingService.processTelemetryData(telemetryRequest, iceVehicle);
        verifyCommonFields(iceResult);

        // Test HYBRID
        TelemetryData hybridResult = telemetryProcessingService.processTelemetryData(telemetryRequest, hybridVehicle);
        verifyCommonFields(hybridResult);
    }

    private void verifyCommonFields(TelemetryData result) {
        assertEquals(telemetryRequest.getVehicleId(), result.getVehicleId());
        assertEquals(telemetryRequest.getLatitude(), result.getLatitude());
        assertEquals(telemetryRequest.getLongitude(), result.getLongitude());
        assertEquals(telemetryRequest.getSpeed(), result.getSpeed());
        assertEquals(telemetryRequest.getOdometer(), result.getOdometer());
        assertEquals(telemetryRequest.getTimestamp(), result.getTimestamp());
        assertEquals(telemetryRequest.getHeading(), result.getHeading());
        assertEquals(telemetryRequest.getAltitude(), result.getAltitude());
        assertEquals(telemetryRequest.getIsIgnitionOn(), result.getIsIgnitionOn());
        assertEquals(telemetryRequest.getTripId(), result.getTripId());
        assertEquals(telemetryRequest.getSignalStrength(), result.getSignalStrength());
    }

    // ===== EDGE CASES =====

    @Test
    void testProcessTelemetry_NullOptionalFields_ShouldNotThrowException() {
        TelemetryRequest minimalRequest = new TelemetryRequest();
        minimalRequest.setVehicleId(1L);
        minimalRequest.setLatitude(12.9716);
        minimalRequest.setLongitude(77.5946);
        minimalRequest.setTimestamp(LocalDateTime.now());

        assertDoesNotThrow(() -> {
            telemetryProcessingService.processTelemetryData(minimalRequest, evVehicle);
            telemetryProcessingService.processTelemetryData(minimalRequest, iceVehicle);
            telemetryProcessingService.processTelemetryData(minimalRequest, hybridVehicle);
        });
    }

    @Test
    void testProcessTelemetry_ZeroValues_ShouldBePreserved() {
        telemetryRequest.setSpeed(0.0);
        telemetryRequest.setBatterySoc(0.0);
        telemetryRequest.setFuelLevel(0.0);

        TelemetryData evResult = telemetryProcessingService.processTelemetryData(telemetryRequest, evVehicle);
        assertEquals(0.0, evResult.getSpeed());
        assertEquals(0.0, evResult.getBatterySoc());

        TelemetryData iceResult = telemetryProcessingService.processTelemetryData(telemetryRequest, iceVehicle);
        assertEquals(0.0, iceResult.getSpeed());
        assertEquals(0.0, iceResult.getFuelLevel());
    }
}
