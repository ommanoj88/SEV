package com.evfleet.maintenance.model;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MaintenanceType enum
 * Tests the applicability logic for different fuel types
 */
class MaintenanceTypeTest {

    @Test
    @DisplayName("ICE maintenance types should be applicable to ICE and HYBRID vehicles")
    void testICEMaintenanceApplicability() {
        // ICE-specific maintenance types
        MaintenanceType[] iceTypes = {
            MaintenanceType.OIL_CHANGE,
            MaintenanceType.OIL_FILTER,
            MaintenanceType.AIR_FILTER,
            MaintenanceType.FUEL_FILTER,
            MaintenanceType.SPARK_PLUGS,
            MaintenanceType.TIMING_BELT
        };

        for (MaintenanceType type : iceTypes) {
            assertTrue(type.isApplicableToFuelType("ICE"),
                    type + " should be applicable to ICE vehicles");
            assertTrue(type.isApplicableToFuelType("HYBRID"),
                    type + " should be applicable to HYBRID vehicles");
            assertFalse(type.isApplicableToFuelType("EV"),
                    type + " should NOT be applicable to EV vehicles");
        }
    }

    @Test
    @DisplayName("EV maintenance types should be applicable to EV and HYBRID vehicles")
    void testEVMaintenanceApplicability() {
        // EV-specific maintenance types
        MaintenanceType[] evTypes = {
            MaintenanceType.BATTERY_CHECK,
            MaintenanceType.BATTERY_MAINTENANCE,
            MaintenanceType.BATTERY_COOLING,
            MaintenanceType.MOTOR_INSPECTION,
            MaintenanceType.HIGH_VOLTAGE_CHECK,
            MaintenanceType.CHARGING_PORT_CHECK
        };

        for (MaintenanceType type : evTypes) {
            assertTrue(type.isApplicableToFuelType("EV"),
                    type + " should be applicable to EV vehicles");
            assertTrue(type.isApplicableToFuelType("HYBRID"),
                    type + " should be applicable to HYBRID vehicles");
            assertFalse(type.isApplicableToFuelType("ICE"),
                    type + " should NOT be applicable to ICE vehicles");
        }
    }

    @Test
    @DisplayName("Common maintenance types should be applicable to all vehicle types")
    void testCommonMaintenanceApplicability() {
        // Common maintenance types
        MaintenanceType[] commonTypes = {
            MaintenanceType.TIRE_ROTATION,
            MaintenanceType.TIRE_REPLACEMENT,
            MaintenanceType.BRAKE_PADS,
            MaintenanceType.BRAKE_FLUID,
            MaintenanceType.BRAKE_INSPECTION,
            MaintenanceType.SUSPENSION_CHECK,
            MaintenanceType.WHEEL_ALIGNMENT
        };

        for (MaintenanceType type : commonTypes) {
            assertTrue(type.isApplicableToFuelType("ICE"),
                    type + " should be applicable to ICE vehicles");
            assertTrue(type.isApplicableToFuelType("EV"),
                    type + " should be applicable to EV vehicles");
            assertTrue(type.isApplicableToFuelType("HYBRID"),
                    type + " should be applicable to HYBRID vehicles");
        }
    }

    @Test
    @DisplayName("Should return false for null fuel type")
    void testNullFuelType() {
        assertFalse(MaintenanceType.OIL_CHANGE.isApplicableToFuelType(null));
        assertFalse(MaintenanceType.BATTERY_CHECK.isApplicableToFuelType(null));
        assertFalse(MaintenanceType.TIRE_ROTATION.isApplicableToFuelType(null));
    }

    @Test
    @DisplayName("Should have correct category for ICE maintenance types")
    void testICECategory() {
        assertEquals("ICE", MaintenanceType.OIL_CHANGE.getCategory());
        assertEquals("ICE", MaintenanceType.AIR_FILTER.getCategory());
        assertEquals("ICE", MaintenanceType.FUEL_FILTER.getCategory());
        assertEquals("ICE", MaintenanceType.SPARK_PLUGS.getCategory());
    }

    @Test
    @DisplayName("Should have correct category for EV maintenance types")
    void testEVCategory() {
        assertEquals("EV", MaintenanceType.BATTERY_CHECK.getCategory());
        assertEquals("EV", MaintenanceType.BATTERY_MAINTENANCE.getCategory());
        assertEquals("EV", MaintenanceType.MOTOR_INSPECTION.getCategory());
        assertEquals("EV", MaintenanceType.HIGH_VOLTAGE_CHECK.getCategory());
    }

    @Test
    @DisplayName("Should have correct category for common maintenance types")
    void testCommonCategory() {
        assertEquals("COMMON", MaintenanceType.TIRE_ROTATION.getCategory());
        assertEquals("COMMON", MaintenanceType.BRAKE_PADS.getCategory());
        assertEquals("COMMON", MaintenanceType.SUSPENSION_CHECK.getCategory());
        assertEquals("COMMON", MaintenanceType.SAFETY_INSPECTION.getCategory());
    }

    @Test
    @DisplayName("Should have valid mileage intervals")
    void testMileageIntervals() {
        // Oil change should have reasonable interval (5000 km)
        assertEquals(5000, MaintenanceType.OIL_CHANGE.getDefaultMileageInterval());
        
        // Battery check should have reasonable interval (10000 km)
        assertEquals(10000, MaintenanceType.BATTERY_CHECK.getDefaultMileageInterval());
        
        // Tire rotation should have reasonable interval (8000 km)
        assertEquals(8000, MaintenanceType.TIRE_ROTATION.getDefaultMileageInterval());
        
        // Some maintenance types may not have mileage intervals (time-based only)
        assertTrue(MaintenanceType.WIPER_REPLACEMENT.getDefaultMileageInterval() >= 0);
    }

    @Test
    @DisplayName("Should have valid time intervals")
    void testTimeIntervals() {
        // Oil change should be every 6 months
        assertEquals(6, MaintenanceType.OIL_CHANGE.getDefaultTimeInterval());
        
        // Battery check should be every 6 months
        assertEquals(6, MaintenanceType.BATTERY_CHECK.getDefaultTimeInterval());
        
        // Tire rotation should be every 6 months
        assertEquals(6, MaintenanceType.TIRE_ROTATION.getDefaultTimeInterval());
        
        // All maintenance types should have positive time intervals
        for (MaintenanceType type : MaintenanceType.values()) {
            assertTrue(type.getDefaultTimeInterval() > 0,
                    type + " should have positive time interval");
        }
    }

    @Test
    @DisplayName("Should have descriptive display names")
    void testDisplayNames() {
        assertNotNull(MaintenanceType.OIL_CHANGE.getDisplayName());
        assertFalse(MaintenanceType.OIL_CHANGE.getDisplayName().isEmpty());
        
        assertNotNull(MaintenanceType.BATTERY_CHECK.getDisplayName());
        assertFalse(MaintenanceType.BATTERY_CHECK.getDisplayName().isEmpty());
        
        assertNotNull(MaintenanceType.TIRE_ROTATION.getDisplayName());
        assertFalse(MaintenanceType.TIRE_ROTATION.getDisplayName().isEmpty());
        
        // Display names should be human-readable, not just the enum name
        assertNotEquals("OIL_CHANGE", MaintenanceType.OIL_CHANGE.getDisplayName());
    }

    @Test
    @DisplayName("Should have all required ICE maintenance types")
    void testRequiredICETypes() {
        // Verify essential ICE maintenance types exist
        assertNotNull(MaintenanceType.valueOf("OIL_CHANGE"));
        assertNotNull(MaintenanceType.valueOf("OIL_FILTER"));
        assertNotNull(MaintenanceType.valueOf("AIR_FILTER"));
        assertNotNull(MaintenanceType.valueOf("FUEL_FILTER"));
        assertNotNull(MaintenanceType.valueOf("TRANSMISSION_FLUID"));
        assertNotNull(MaintenanceType.valueOf("COOLANT_FLUSH"));
        assertNotNull(MaintenanceType.valueOf("SPARK_PLUGS"));
        assertNotNull(MaintenanceType.valueOf("TIMING_BELT"));
    }

    @Test
    @DisplayName("Should have all required EV maintenance types")
    void testRequiredEVTypes() {
        // Verify essential EV maintenance types exist
        assertNotNull(MaintenanceType.valueOf("BATTERY_CHECK"));
        assertNotNull(MaintenanceType.valueOf("BATTERY_MAINTENANCE"));
        assertNotNull(MaintenanceType.valueOf("BATTERY_COOLING"));
        assertNotNull(MaintenanceType.valueOf("MOTOR_INSPECTION"));
        assertNotNull(MaintenanceType.valueOf("HIGH_VOLTAGE_CHECK"));
        assertNotNull(MaintenanceType.valueOf("CHARGING_PORT_CHECK"));
    }

    @Test
    @DisplayName("Should have all required common maintenance types")
    void testRequiredCommonTypes() {
        // Verify essential common maintenance types exist
        assertNotNull(MaintenanceType.valueOf("TIRE_ROTATION"));
        assertNotNull(MaintenanceType.valueOf("TIRE_REPLACEMENT"));
        assertNotNull(MaintenanceType.valueOf("BRAKE_PADS"));
        assertNotNull(MaintenanceType.valueOf("BRAKE_FLUID"));
        assertNotNull(MaintenanceType.valueOf("BRAKE_INSPECTION"));
        assertNotNull(MaintenanceType.valueOf("SUSPENSION_CHECK"));
        assertNotNull(MaintenanceType.valueOf("WHEEL_ALIGNMENT"));
        assertNotNull(MaintenanceType.valueOf("SAFETY_INSPECTION"));
    }

    @Test
    @DisplayName("Should count maintenance types correctly")
    void testMaintenanceTypeCount() {
        MaintenanceType[] allTypes = MaintenanceType.values();
        
        // Count by category
        long iceCount = 0;
        long evCount = 0;
        long commonCount = 0;
        
        for (MaintenanceType type : allTypes) {
            switch (type.getCategory()) {
                case "ICE":
                    iceCount++;
                    break;
                case "EV":
                    evCount++;
                    break;
                case "COMMON":
                    commonCount++;
                    break;
            }
        }
        
        // Verify we have a good distribution
        assertTrue(iceCount > 0, "Should have ICE maintenance types");
        assertTrue(evCount > 0, "Should have EV maintenance types");
        assertTrue(commonCount > 0, "Should have common maintenance types");
        
        // Total should match
        assertEquals(allTypes.length, iceCount + evCount + commonCount,
                "Total maintenance types should equal sum of categories");
    }
}
