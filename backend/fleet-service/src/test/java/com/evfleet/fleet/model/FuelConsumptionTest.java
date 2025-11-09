package com.evfleet.fleet.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FuelConsumption entity
 * Tests entity creation, metrics calculation, and business logic
 */
class FuelConsumptionTest {

    private FuelConsumption fuelConsumption;

    @BeforeEach
    void setUp() {
        fuelConsumption = new FuelConsumption();
    }

    @Test
    void testEntityCreation() {
        // Test basic entity creation
        FuelConsumption fc = FuelConsumption.builder()
            .vehicleId(1L)
            .timestamp(LocalDateTime.now())
            .fuelConsumedLiters(10.0)
            .distanceKm(100.0)
            .build();

        assertNotNull(fc);
        assertEquals(1L, fc.getVehicleId());
        assertEquals(10.0, fc.getFuelConsumedLiters());
        assertEquals(100.0, fc.getDistanceKm());
    }

    @Test
    void testFuelEfficiencyCalculation() {
        // Test automatic fuel efficiency calculation
        fuelConsumption.setFuelConsumedLiters(10.0);
        fuelConsumption.setDistanceKm(100.0);
        fuelConsumption.calculateMetrics();

        assertEquals(10.0, fuelConsumption.getFuelEfficiencyKmpl(), 0.01, 
            "Fuel efficiency should be 100km / 10L = 10 kmpl");
    }

    @Test
    void testFuelEfficiencyWithZeroFuel() {
        // Test that zero fuel doesn't cause division by zero
        fuelConsumption.setFuelConsumedLiters(0.0);
        fuelConsumption.setDistanceKm(100.0);
        fuelConsumption.calculateMetrics();

        // Should not calculate efficiency with zero fuel
        assertNull(fuelConsumption.getFuelEfficiencyKmpl(), 
            "Fuel efficiency should not be calculated with zero fuel");
    }

    @Test
    void testCostCalculation() {
        // Test automatic cost calculation
        fuelConsumption.setFuelConsumedLiters(10.0);
        fuelConsumption.setFuelPricePerLiter(100.0); // ₹100 per liter
        fuelConsumption.calculateMetrics();

        assertEquals(1000.0, fuelConsumption.getCostInr(), 0.01, 
            "Cost should be 10L * ₹100 = ₹1000");
    }

    @Test
    void testCO2EmissionsCalculationForPetrol() {
        // Test CO2 emissions calculation for petrol
        fuelConsumption.setFuelConsumedLiters(10.0);
        fuelConsumption.setFuelTypeDetail("PETROL");
        fuelConsumption.calculateMetrics();

        assertEquals(23.1, fuelConsumption.getCo2EmissionsKg(), 0.01, 
            "CO2 emissions should be 10L * 2.31 kg/L = 23.1 kg");
    }

    @Test
    void testCO2EmissionsCalculationForDiesel() {
        // Test CO2 emissions calculation for diesel
        fuelConsumption.setFuelConsumedLiters(10.0);
        fuelConsumption.setFuelTypeDetail("DIESEL");
        fuelConsumption.calculateMetrics();

        assertEquals(26.8, fuelConsumption.getCo2EmissionsKg(), 0.01, 
            "CO2 emissions should be 10L * 2.68 kg/L = 26.8 kg");
    }

    @Test
    void testCO2EmissionsCalculationForCNG() {
        // Test CO2 emissions calculation for CNG
        fuelConsumption.setFuelConsumedLiters(10.0);
        fuelConsumption.setFuelTypeDetail("CNG");
        fuelConsumption.calculateMetrics();

        assertEquals(18.9, fuelConsumption.getCo2EmissionsKg(), 0.01, 
            "CO2 emissions should be 10L * 1.89 kg/L = 18.9 kg");
    }

    @Test
    void testCO2EmissionsCalculationForUnknownFuel() {
        // Test CO2 emissions with unknown fuel type (should use average)
        fuelConsumption.setFuelConsumedLiters(10.0);
        fuelConsumption.setFuelTypeDetail("UNKNOWN");
        fuelConsumption.calculateMetrics();

        assertEquals(25.0, fuelConsumption.getCo2EmissionsKg(), 0.01, 
            "CO2 emissions should use average factor: 10L * 2.5 kg/L = 25.0 kg");
    }

    @Test
    void testCO2EmissionsWithCaseInsensitiveFuelType() {
        // Test that fuel type detail is case-insensitive
        fuelConsumption.setFuelConsumedLiters(10.0);
        fuelConsumption.setFuelTypeDetail("petrol"); // lowercase
        fuelConsumption.calculateMetrics();

        assertEquals(23.1, fuelConsumption.getCo2EmissionsKg(), 0.01, 
            "Should handle lowercase fuel type");
    }

    @Test
    void testCompleteMetricsCalculation() {
        // Test all metrics calculated together
        fuelConsumption.setVehicleId(1L);
        fuelConsumption.setFuelConsumedLiters(15.0);
        fuelConsumption.setDistanceKm(180.0);
        fuelConsumption.setFuelPricePerLiter(95.0);
        fuelConsumption.setFuelTypeDetail("DIESEL");
        fuelConsumption.calculateMetrics();

        assertEquals(12.0, fuelConsumption.getFuelEfficiencyKmpl(), 0.01, 
            "Fuel efficiency should be 180/15 = 12 kmpl");
        assertEquals(1425.0, fuelConsumption.getCostInr(), 0.01, 
            "Cost should be 15 * 95 = ₹1425");
        assertEquals(40.2, fuelConsumption.getCo2EmissionsKg(), 0.01, 
            "CO2 emissions should be 15 * 2.68 = 40.2 kg");
    }

    @Test
    void testNullSafetyInCalculations() {
        // Test that null values don't cause exceptions
        fuelConsumption.setFuelConsumedLiters(null);
        fuelConsumption.setDistanceKm(100.0);
        
        assertDoesNotThrow(() -> fuelConsumption.calculateMetrics(), 
            "Should handle null fuel consumed gracefully");
    }

    @Test
    void testLocationFields() {
        // Test location fields
        fuelConsumption.setLatitude(28.7041);
        fuelConsumption.setLongitude(77.1025);
        fuelConsumption.setRefuelLocation("Delhi");

        assertEquals(28.7041, fuelConsumption.getLatitude());
        assertEquals(77.1025, fuelConsumption.getLongitude());
        assertEquals("Delhi", fuelConsumption.getRefuelLocation());
    }

    @Test
    void testTripAssociation() {
        // Test trip association
        fuelConsumption.setTripId(100L);
        assertEquals(100L, fuelConsumption.getTripId());
    }

    @Test
    void testTimestampHandling() {
        // Test timestamp field
        LocalDateTime now = LocalDateTime.now();
        fuelConsumption.setTimestamp(now);
        assertEquals(now, fuelConsumption.getTimestamp());
    }

    @Test
    void testNotesField() {
        // Test notes field
        String notes = "Regular refueling at highway station";
        fuelConsumption.setNotes(notes);
        assertEquals(notes, fuelConsumption.getNotes());
    }

    @Test
    void testBuilderPattern() {
        // Test builder pattern with all fields
        LocalDateTime timestamp = LocalDateTime.now();
        FuelConsumption fc = FuelConsumption.builder()
            .id(1L)
            .vehicleId(100L)
            .tripId(50L)
            .timestamp(timestamp)
            .fuelConsumedLiters(20.0)
            .distanceKm(250.0)
            .fuelEfficiencyKmpl(12.5)
            .costInr(1900.0)
            .fuelPricePerLiter(95.0)
            .co2EmissionsKg(53.6)
            .fuelTypeDetail("DIESEL")
            .refuelLocation("Mumbai")
            .latitude(19.0760)
            .longitude(72.8777)
            .notes("Test refueling")
            .createdAt(timestamp)
            .build();

        assertNotNull(fc);
        assertEquals(1L, fc.getId());
        assertEquals(100L, fc.getVehicleId());
        assertEquals(50L, fc.getTripId());
        assertEquals(20.0, fc.getFuelConsumedLiters());
        assertEquals(250.0, fc.getDistanceKm());
        assertEquals("DIESEL", fc.getFuelTypeDetail());
        assertEquals("Mumbai", fc.getRefuelLocation());
    }
}
