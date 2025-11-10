package com.evfleet.fleet.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for TelemetryType enum
 * 
 * Tests the telemetry type categorization for multi-fuel fleet management
 */
class TelemetryTypeTest {

    @Test
    void testTelemetryTypeValues() {
        // Verify all expected telemetry types exist
        TelemetryType[] types = TelemetryType.values();
        
        assertEquals(6, types.length, "Should have 6 telemetry types");
        
        // Verify each type exists
        assertNotNull(TelemetryType.valueOf("COMMON"));
        assertNotNull(TelemetryType.valueOf("BATTERY"));
        assertNotNull(TelemetryType.valueOf("MOTOR"));
        assertNotNull(TelemetryType.valueOf("ENGINE"));
        assertNotNull(TelemetryType.valueOf("FUEL"));
        assertNotNull(TelemetryType.valueOf("CHARGING"));
    }

    @Test
    void testTelemetryTypeOrdering() {
        // Verify enum ordering for consistency
        TelemetryType[] types = TelemetryType.values();
        
        assertEquals(TelemetryType.COMMON, types[0]);
        assertEquals(TelemetryType.BATTERY, types[1]);
        assertEquals(TelemetryType.MOTOR, types[2]);
        assertEquals(TelemetryType.ENGINE, types[3]);
        assertEquals(TelemetryType.FUEL, types[4]);
        assertEquals(TelemetryType.CHARGING, types[5]);
    }

    @Test
    void testTelemetryTypeName() {
        assertEquals("COMMON", TelemetryType.COMMON.name());
        assertEquals("ENGINE", TelemetryType.ENGINE.name());
        assertEquals("FUEL", TelemetryType.FUEL.name());
    }

    @Test
    void testInvalidTelemetryType() {
        assertThrows(IllegalArgumentException.class, () -> {
            TelemetryType.valueOf("INVALID_TYPE");
        });
    }
}
