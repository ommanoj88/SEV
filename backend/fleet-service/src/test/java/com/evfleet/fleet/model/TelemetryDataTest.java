package com.evfleet.fleet.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TelemetryData entity
 * 
 * Tests the multi-fuel telemetry data model including EV and ICE specific fields
 */
class TelemetryDataTest {

    private TelemetryData telemetryData;

    @BeforeEach
    void setUp() {
        telemetryData = new TelemetryData();
    }

    @Test
    void testCommonTelemetryFields() {
        // Common fields applicable to all vehicle types
        telemetryData.setVehicleId(1L);
        telemetryData.setLatitude(12.9716);
        telemetryData.setLongitude(77.5946);
        telemetryData.setSpeed(60.0);
        telemetryData.setTimestamp(LocalDateTime.now());
        telemetryData.setOdometer(15000.0);
        telemetryData.setHeading(180.0);
        telemetryData.setAltitude(920.0);
        telemetryData.setSignalStrength(85);

        assertEquals(1L, telemetryData.getVehicleId());
        assertEquals(12.9716, telemetryData.getLatitude());
        assertEquals(77.5946, telemetryData.getLongitude());
        assertEquals(60.0, telemetryData.getSpeed());
        assertNotNull(telemetryData.getTimestamp());
        assertEquals(15000.0, telemetryData.getOdometer());
        assertEquals(180.0, telemetryData.getHeading());
        assertEquals(920.0, telemetryData.getAltitude());
        assertEquals(85, telemetryData.getSignalStrength());
    }

    @Test
    void testEVSpecificTelemetryFields() {
        // EV-specific battery and motor fields
        telemetryData.setBatterySoc(75.5);
        telemetryData.setBatteryVoltage(72.0);
        telemetryData.setBatteryCurrent(25.5);
        telemetryData.setBatteryTemperature(28.5);
        telemetryData.setMotorTemperature(65.0);
        telemetryData.setControllerTemperature(55.0);
        telemetryData.setPowerConsumption(15.5);
        telemetryData.setRegenerativePower(3.2);
        telemetryData.setIsCharging(false);

        assertEquals(75.5, telemetryData.getBatterySoc());
        assertEquals(72.0, telemetryData.getBatteryVoltage());
        assertEquals(25.5, telemetryData.getBatteryCurrent());
        assertEquals(28.5, telemetryData.getBatteryTemperature());
        assertEquals(65.0, telemetryData.getMotorTemperature());
        assertEquals(55.0, telemetryData.getControllerTemperature());
        assertEquals(15.5, telemetryData.getPowerConsumption());
        assertEquals(3.2, telemetryData.getRegenerativePower());
        assertFalse(telemetryData.getIsCharging());
    }

    @Test
    void testICESpecificTelemetryFields() {
        // ICE-specific engine and fuel fields
        telemetryData.setFuelLevel(45.5);
        telemetryData.setEngineRpm(2500);
        telemetryData.setEngineTemperature(92.0);
        telemetryData.setEngineLoad(65.5);
        telemetryData.setEngineHours(1250.5);

        assertEquals(45.5, telemetryData.getFuelLevel());
        assertEquals(2500, telemetryData.getEngineRpm());
        assertEquals(92.0, telemetryData.getEngineTemperature());
        assertEquals(65.5, telemetryData.getEngineLoad());
        assertEquals(1250.5, telemetryData.getEngineHours());
    }

    @Test
    void testHybridVehicleTelemetry() {
        // HYBRID vehicles should have both EV and ICE fields
        
        // EV fields
        telemetryData.setBatterySoc(60.0);
        telemetryData.setBatteryVoltage(68.0);
        telemetryData.setMotorTemperature(70.0);
        
        // ICE fields
        telemetryData.setFuelLevel(30.0);
        telemetryData.setEngineRpm(1800);
        telemetryData.setEngineTemperature(88.0);
        telemetryData.setEngineLoad(45.0);

        // Verify both sets of fields are set
        assertEquals(60.0, telemetryData.getBatterySoc());
        assertEquals(68.0, telemetryData.getBatteryVoltage());
        assertEquals(70.0, telemetryData.getMotorTemperature());
        
        assertEquals(30.0, telemetryData.getFuelLevel());
        assertEquals(1800, telemetryData.getEngineRpm());
        assertEquals(88.0, telemetryData.getEngineTemperature());
        assertEquals(45.0, telemetryData.getEngineLoad());
    }

    @Test
    void testNullableFields() {
        // Test that optional fields can be null
        assertNull(telemetryData.getFuelLevel());
        assertNull(telemetryData.getEngineRpm());
        assertNull(telemetryData.getEngineTemperature());
        assertNull(telemetryData.getEngineLoad());
        assertNull(telemetryData.getEngineHours());
        assertNull(telemetryData.getBatterySoc());
        assertNull(telemetryData.getBatteryVoltage());
    }

    @Test
    void testTripAssociation() {
        telemetryData.setTripId(101L);
        assertEquals(101L, telemetryData.getTripId());
    }

    @Test
    void testErrorCodes() {
        String errorCodes = "P0001,P0002,P0003";
        telemetryData.setErrorCodes(errorCodes);
        assertEquals(errorCodes, telemetryData.getErrorCodes());
    }

    @Test
    void testIgnitionStatus() {
        telemetryData.setIsIgnitionOn(true);
        assertTrue(telemetryData.getIsIgnitionOn());
        
        telemetryData.setIsIgnitionOn(false);
        assertFalse(telemetryData.getIsIgnitionOn());
    }

    @Test
    void testValidEngineRpmRange() {
        // Engine RPM should be positive and within reasonable limits
        telemetryData.setEngineRpm(0);
        assertEquals(0, telemetryData.getEngineRpm());
        
        telemetryData.setEngineRpm(3000);
        assertEquals(3000, telemetryData.getEngineRpm());
        
        telemetryData.setEngineRpm(8000);
        assertEquals(8000, telemetryData.getEngineRpm());
    }

    @Test
    void testValidEngineLoadRange() {
        // Engine load should be between 0 and 100
        telemetryData.setEngineLoad(0.0);
        assertEquals(0.0, telemetryData.getEngineLoad());
        
        telemetryData.setEngineLoad(50.0);
        assertEquals(50.0, telemetryData.getEngineLoad());
        
        telemetryData.setEngineLoad(100.0);
        assertEquals(100.0, telemetryData.getEngineLoad());
    }

    @Test
    void testValidFuelLevelRange() {
        // Fuel level should be positive
        telemetryData.setFuelLevel(0.0);
        assertEquals(0.0, telemetryData.getFuelLevel());
        
        telemetryData.setFuelLevel(25.5);
        assertEquals(25.5, telemetryData.getFuelLevel());
        
        telemetryData.setFuelLevel(60.0);
        assertEquals(60.0, telemetryData.getFuelLevel());
    }

    @Test
    void testEngineTemperatureRange() {
        // Engine temperature in realistic range
        telemetryData.setEngineTemperature(20.0);  // Cold start
        assertEquals(20.0, telemetryData.getEngineTemperature());
        
        telemetryData.setEngineTemperature(90.0);  // Normal operating temp
        assertEquals(90.0, telemetryData.getEngineTemperature());
        
        telemetryData.setEngineTemperature(110.0);  // High temp warning
        assertEquals(110.0, telemetryData.getEngineTemperature());
    }

    @Test
    void testEngineHoursAccumulation() {
        // Engine hours should be cumulative
        telemetryData.setEngineHours(0.0);
        assertEquals(0.0, telemetryData.getEngineHours());
        
        telemetryData.setEngineHours(500.5);
        assertEquals(500.5, telemetryData.getEngineHours());
        
        telemetryData.setEngineHours(5000.0);
        assertEquals(5000.0, telemetryData.getEngineHours());
    }
}
