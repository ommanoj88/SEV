package com.evfleet.fleet.model;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FuelType enum
 * Tests all fuel type constants and their properties
 */
class FuelTypeTest {

    @Test
    void testFuelTypeValues() {
        // Test that all expected values exist
        FuelType[] values = FuelType.values();
        assertEquals(3, values.length, "Should have exactly 3 fuel types");
        
        assertTrue(containsValue(values, FuelType.ICE), "Should contain ICE");
        assertTrue(containsValue(values, FuelType.EV), "Should contain EV");
        assertTrue(containsValue(values, FuelType.HYBRID), "Should contain HYBRID");
    }

    @Test
    void testValueOf() {
        // Test valueOf method
        assertEquals(FuelType.ICE, FuelType.valueOf("ICE"));
        assertEquals(FuelType.EV, FuelType.valueOf("EV"));
        assertEquals(FuelType.HYBRID, FuelType.valueOf("HYBRID"));
    }

    @Test
    void testInvalidValueOf() {
        // Test that invalid values throw exception
        assertThrows(IllegalArgumentException.class, () -> {
            FuelType.valueOf("INVALID");
        });
    }

    @Test
    void testEnumEquality() {
        // Test enum equality
        FuelType ice1 = FuelType.ICE;
        FuelType ice2 = FuelType.ICE;
        assertSame(ice1, ice2, "Enum instances should be the same");
    }

    @Test
    void testEnumToString() {
        // Test toString method
        assertEquals("ICE", FuelType.ICE.toString());
        assertEquals("EV", FuelType.EV.toString());
        assertEquals("HYBRID", FuelType.HYBRID.toString());
    }

    @Test
    void testEnumName() {
        // Test name method
        assertEquals("ICE", FuelType.ICE.name());
        assertEquals("EV", FuelType.EV.name());
        assertEquals("HYBRID", FuelType.HYBRID.name());
    }

    // Helper method
    private boolean containsValue(FuelType[] values, FuelType target) {
        for (FuelType value : values) {
            if (value == target) {
                return true;
            }
        }
        return false;
    }
}
