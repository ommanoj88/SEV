package com.evfleet.maintenance.service;

import com.evfleet.maintenance.entity.MaintenanceSchedule;
import com.evfleet.maintenance.model.MaintenanceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for MaintenanceScheduleBuilder
 * Tests the generation of maintenance schedules for different fuel types
 */
class MaintenanceScheduleBuilderTest {

    private MaintenanceScheduleBuilder scheduleBuilder;

    @BeforeEach
    void setUp() {
        scheduleBuilder = new MaintenanceScheduleBuilder();
    }

    @Test
    @DisplayName("Should generate schedules for ICE vehicle")
    void testGenerateSchedulesForICEVehicle() {
        // Given
        String vehicleId = "VEH001";
        String fuelType = "ICE";
        Integer currentMileage = 10000;

        // When
        List<MaintenanceSchedule> schedules = scheduleBuilder.generateSchedulesForVehicle(
                vehicleId, fuelType, currentMileage);

        // Then
        assertNotNull(schedules);
        assertFalse(schedules.isEmpty());
        
        // ICE vehicle should have ICE-specific and COMMON maintenance, but not EV-specific
        assertTrue(schedules.stream()
                .anyMatch(s -> s.getServiceType().equals("OIL_CHANGE")),
                "ICE vehicle should have OIL_CHANGE maintenance");
        assertTrue(schedules.stream()
                .anyMatch(s -> s.getServiceType().equals("AIR_FILTER")),
                "ICE vehicle should have AIR_FILTER maintenance");
        assertTrue(schedules.stream()
                .anyMatch(s -> s.getServiceType().equals("TIRE_ROTATION")),
                "ICE vehicle should have TIRE_ROTATION (common) maintenance");
        assertFalse(schedules.stream()
                .anyMatch(s -> s.getServiceType().equals("BATTERY_CHECK")),
                "ICE vehicle should not have BATTERY_CHECK (EV-specific) maintenance");
    }

    @Test
    @DisplayName("Should generate schedules for EV vehicle")
    void testGenerateSchedulesForEVVehicle() {
        // Given
        String vehicleId = "VEH002";
        String fuelType = "EV";
        Integer currentMileage = 15000;

        // When
        List<MaintenanceSchedule> schedules = scheduleBuilder.generateSchedulesForVehicle(
                vehicleId, fuelType, currentMileage);

        // Then
        assertNotNull(schedules);
        assertFalse(schedules.isEmpty());
        
        // EV vehicle should have EV-specific and COMMON maintenance, but not ICE-specific
        assertTrue(schedules.stream()
                .anyMatch(s -> s.getServiceType().equals("BATTERY_CHECK")),
                "EV vehicle should have BATTERY_CHECK maintenance");
        assertTrue(schedules.stream()
                .anyMatch(s -> s.getServiceType().equals("MOTOR_INSPECTION")),
                "EV vehicle should have MOTOR_INSPECTION maintenance");
        assertTrue(schedules.stream()
                .anyMatch(s -> s.getServiceType().equals("TIRE_ROTATION")),
                "EV vehicle should have TIRE_ROTATION (common) maintenance");
        assertFalse(schedules.stream()
                .anyMatch(s -> s.getServiceType().equals("OIL_CHANGE")),
                "EV vehicle should not have OIL_CHANGE (ICE-specific) maintenance");
    }

    @Test
    @DisplayName("Should generate schedules for HYBRID vehicle")
    void testGenerateSchedulesForHybridVehicle() {
        // Given
        String vehicleId = "VEH003";
        String fuelType = "HYBRID";
        Integer currentMileage = 20000;

        // When
        List<MaintenanceSchedule> schedules = scheduleBuilder.generateSchedulesForVehicle(
                vehicleId, fuelType, currentMileage);

        // Then
        assertNotNull(schedules);
        assertFalse(schedules.isEmpty());
        
        // HYBRID vehicle should have ALL maintenance types (ICE, EV, and COMMON)
        assertTrue(schedules.stream()
                .anyMatch(s -> s.getServiceType().equals("OIL_CHANGE")),
                "HYBRID vehicle should have OIL_CHANGE (ICE) maintenance");
        assertTrue(schedules.stream()
                .anyMatch(s -> s.getServiceType().equals("BATTERY_CHECK")),
                "HYBRID vehicle should have BATTERY_CHECK (EV) maintenance");
        assertTrue(schedules.stream()
                .anyMatch(s -> s.getServiceType().equals("TIRE_ROTATION")),
                "HYBRID vehicle should have TIRE_ROTATION (common) maintenance");
        
        // Verify HYBRID has more schedules than ICE or EV alone
        int hybridScheduleCount = schedules.size();
        int iceScheduleCount = scheduleBuilder.generateSchedulesForVehicle(vehicleId, "ICE", currentMileage).size();
        int evScheduleCount = scheduleBuilder.generateSchedulesForVehicle(vehicleId, "EV", currentMileage).size();
        
        assertTrue(hybridScheduleCount > iceScheduleCount, 
                "HYBRID should have more schedules than ICE alone");
        assertTrue(hybridScheduleCount > evScheduleCount, 
                "HYBRID should have more schedules than EV alone");
    }

    @Test
    @DisplayName("Should generate ICE-specific schedules only")
    void testGenerateICESchedulesOnly() {
        // Given
        String vehicleId = "VEH004";
        Integer currentMileage = 5000;

        // When
        List<MaintenanceSchedule> schedules = scheduleBuilder.generateICESchedules(vehicleId, currentMileage);

        // Then
        assertNotNull(schedules);
        assertFalse(schedules.isEmpty());
        
        // All schedules should be ICE-specific
        schedules.forEach(schedule -> {
            MaintenanceType type = MaintenanceType.valueOf(schedule.getServiceType());
            assertEquals("ICE", type.getCategory(), 
                    "All schedules should be ICE category");
        });
    }

    @Test
    @DisplayName("Should generate EV-specific schedules only")
    void testGenerateEVSchedulesOnly() {
        // Given
        String vehicleId = "VEH005";
        Integer currentMileage = 8000;

        // When
        List<MaintenanceSchedule> schedules = scheduleBuilder.generateEVSchedules(vehicleId, currentMileage);

        // Then
        assertNotNull(schedules);
        assertFalse(schedules.isEmpty());
        
        // All schedules should be EV-specific
        schedules.forEach(schedule -> {
            MaintenanceType type = MaintenanceType.valueOf(schedule.getServiceType());
            assertEquals("EV", type.getCategory(), 
                    "All schedules should be EV category");
        });
    }

    @Test
    @DisplayName("Should generate common schedules only")
    void testGenerateCommonSchedulesOnly() {
        // Given
        String vehicleId = "VEH006";
        Integer currentMileage = 12000;

        // When
        List<MaintenanceSchedule> schedules = scheduleBuilder.generateCommonSchedules(vehicleId, currentMileage);

        // Then
        assertNotNull(schedules);
        assertFalse(schedules.isEmpty());
        
        // All schedules should be COMMON category
        schedules.forEach(schedule -> {
            MaintenanceType type = MaintenanceType.valueOf(schedule.getServiceType());
            assertEquals("COMMON", type.getCategory(), 
                    "All schedules should be COMMON category");
        });
    }

    @Test
    @DisplayName("Should calculate due mileage correctly")
    void testDueMileageCalculation() {
        // Given
        String vehicleId = "VEH007";
        String fuelType = "ICE";
        Integer currentMileage = 7500;

        // When
        List<MaintenanceSchedule> schedules = scheduleBuilder.generateSchedulesForVehicle(
                vehicleId, fuelType, currentMileage);

        // Then
        MaintenanceSchedule oilChangeSchedule = schedules.stream()
                .filter(s -> s.getServiceType().equals("OIL_CHANGE"))
                .findFirst()
                .orElseThrow();

        // Oil change interval is 5000 km, current mileage is 7500
        // Next due should be 10000 km (next interval after 7500)
        assertEquals(10000, oilChangeSchedule.getDueMileage(),
                "Due mileage should be calculated correctly");
    }

    @Test
    @DisplayName("Should set appropriate priority levels")
    void testPriorityLevels() {
        // Given
        String vehicleId = "VEH008";
        String fuelType = "ICE";
        Integer currentMileage = 5000;

        // When
        List<MaintenanceSchedule> schedules = scheduleBuilder.generateSchedulesForVehicle(
                vehicleId, fuelType, currentMileage);

        // Then
        MaintenanceSchedule oilChangeSchedule = schedules.stream()
                .filter(s -> s.getServiceType().equals("OIL_CHANGE"))
                .findFirst()
                .orElseThrow();
        
        assertEquals("HIGH", oilChangeSchedule.getPriority(),
                "Oil change should be HIGH priority");
    }

    @Test
    @DisplayName("Should handle null current mileage")
    void testNullCurrentMileage() {
        // Given
        String vehicleId = "VEH009";
        String fuelType = "ICE";
        Integer currentMileage = null;

        // When
        List<MaintenanceSchedule> schedules = scheduleBuilder.generateSchedulesForVehicle(
                vehicleId, fuelType, currentMileage);

        // Then
        assertNotNull(schedules);
        assertFalse(schedules.isEmpty());
        
        // Should use 0 as default mileage
        schedules.forEach(schedule -> {
            if (schedule.getDueMileage() != null && schedule.getDueMileage() > 0) {
                MaintenanceType type = MaintenanceType.valueOf(schedule.getServiceType());
                assertTrue(schedule.getDueMileage() >= type.getDefaultMileageInterval(),
                        "Due mileage should be at least the default interval");
            }
        });
    }

    @Test
    @DisplayName("Should throw exception for null vehicle ID")
    void testNullVehicleId() {
        // Given
        String vehicleId = null;
        String fuelType = "ICE";
        Integer currentMileage = 5000;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            scheduleBuilder.generateSchedulesForVehicle(vehicleId, fuelType, currentMileage);
        }, "Should throw IllegalArgumentException for null vehicle ID");
    }

    @Test
    @DisplayName("Should throw exception for null fuel type")
    void testNullFuelType() {
        // Given
        String vehicleId = "VEH010";
        String fuelType = null;
        Integer currentMileage = 5000;

        // When & Then
        assertThrows(IllegalArgumentException.class, () -> {
            scheduleBuilder.generateSchedulesForVehicle(vehicleId, fuelType, currentMileage);
        }, "Should throw IllegalArgumentException for null fuel type");
    }

    @Test
    @DisplayName("Should get applicable maintenance types for ICE")
    void testGetApplicableMaintenanceTypesForICE() {
        // When
        List<MaintenanceType> types = scheduleBuilder.getApplicableMaintenanceTypes("ICE");

        // Then
        assertNotNull(types);
        assertFalse(types.isEmpty());
        assertTrue(types.contains(MaintenanceType.OIL_CHANGE));
        assertTrue(types.contains(MaintenanceType.TIRE_ROTATION)); // Common
        assertFalse(types.contains(MaintenanceType.BATTERY_CHECK)); // EV-only
    }

    @Test
    @DisplayName("Should get applicable maintenance types for EV")
    void testGetApplicableMaintenanceTypesForEV() {
        // When
        List<MaintenanceType> types = scheduleBuilder.getApplicableMaintenanceTypes("EV");

        // Then
        assertNotNull(types);
        assertFalse(types.isEmpty());
        assertTrue(types.contains(MaintenanceType.BATTERY_CHECK));
        assertTrue(types.contains(MaintenanceType.TIRE_ROTATION)); // Common
        assertFalse(types.contains(MaintenanceType.OIL_CHANGE)); // ICE-only
    }

    @Test
    @DisplayName("Should check if maintenance is applicable to fuel type")
    void testIsMaintenanceApplicable() {
        // Test ICE maintenance
        assertTrue(scheduleBuilder.isMaintenanceApplicable(MaintenanceType.OIL_CHANGE, "ICE"));
        assertTrue(scheduleBuilder.isMaintenanceApplicable(MaintenanceType.OIL_CHANGE, "HYBRID"));
        assertFalse(scheduleBuilder.isMaintenanceApplicable(MaintenanceType.OIL_CHANGE, "EV"));

        // Test EV maintenance
        assertTrue(scheduleBuilder.isMaintenanceApplicable(MaintenanceType.BATTERY_CHECK, "EV"));
        assertTrue(scheduleBuilder.isMaintenanceApplicable(MaintenanceType.BATTERY_CHECK, "HYBRID"));
        assertFalse(scheduleBuilder.isMaintenanceApplicable(MaintenanceType.BATTERY_CHECK, "ICE"));

        // Test common maintenance
        assertTrue(scheduleBuilder.isMaintenanceApplicable(MaintenanceType.TIRE_ROTATION, "ICE"));
        assertTrue(scheduleBuilder.isMaintenanceApplicable(MaintenanceType.TIRE_ROTATION, "EV"));
        assertTrue(scheduleBuilder.isMaintenanceApplicable(MaintenanceType.TIRE_ROTATION, "HYBRID"));
    }

    @Test
    @DisplayName("Should create schedule for specific maintenance type")
    void testGetScheduleForMaintenanceType() {
        // Given
        String vehicleId = "VEH011";
        MaintenanceType type = MaintenanceType.OIL_CHANGE;
        Integer currentMileage = 3000;

        // When
        MaintenanceSchedule schedule = scheduleBuilder.getScheduleForMaintenanceType(
                vehicleId, type, currentMileage);

        // Then
        assertNotNull(schedule);
        assertEquals(vehicleId, schedule.getVehicleId());
        assertEquals(type.name(), schedule.getServiceType());
        assertNotNull(schedule.getDueDate());
        assertNotNull(schedule.getDueMileage());
        assertEquals("SCHEDULED", schedule.getStatus());
    }
}
