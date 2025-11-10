package com.evfleet.fleet.validation;

import com.evfleet.fleet.dto.VehicleRequest;
import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FuelTypeValidator
 * Tests validation logic for fuel-type-specific requirements
 * 
 * @since 2.0.0 (PR 5: Vehicle CRUD API Updates)
 */
class FuelTypeValidatorTest {

    private FuelTypeValidator validator;

    @BeforeEach
    void setUp() {
        validator = new FuelTypeValidator();
    }

    // ===== EV Vehicle Tests =====

    @Test
    void testValidateEVVehicle_WithValidBatteryCapacity_ShouldPass() {
        // Given
        VehicleRequest request = createBasicRequest();
        request.setFuelType(FuelType.EV);
        request.setBatteryCapacity(75.0);

        // When & Then - should not throw
        assertDoesNotThrow(() -> validator.validateVehicleRequest(request));
    }

    @Test
    void testValidateEVVehicle_WithNullBatteryCapacity_ShouldFail() {
        // Given
        VehicleRequest request = createBasicRequest();
        request.setFuelType(FuelType.EV);
        request.setBatteryCapacity(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateVehicleRequest(request)
        );
        assertTrue(exception.getMessage().contains("Battery capacity is required"));
    }

    @Test
    void testValidateEVVehicle_WithZeroBatteryCapacity_ShouldFail() {
        // Given
        VehicleRequest request = createBasicRequest();
        request.setFuelType(FuelType.EV);
        request.setBatteryCapacity(0.0);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateVehicleRequest(request)
        );
        assertTrue(exception.getMessage().contains("Battery capacity is required"));
    }

    @Test
    void testValidateEVVehicle_WithNegativeBatteryCapacity_ShouldFail() {
        // Given
        VehicleRequest request = createBasicRequest();
        request.setFuelType(FuelType.EV);
        request.setBatteryCapacity(-10.0);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateVehicleRequest(request)
        );
        assertTrue(exception.getMessage().contains("Battery capacity is required"));
    }

    // ===== ICE Vehicle Tests =====

    @Test
    void testValidateICEVehicle_WithValidFuelTankCapacity_ShouldPass() {
        // Given
        VehicleRequest request = createBasicRequest();
        request.setFuelType(FuelType.ICE);
        request.setFuelTankCapacity(60.0);

        // When & Then - should not throw
        assertDoesNotThrow(() -> validator.validateVehicleRequest(request));
    }

    @Test
    void testValidateICEVehicle_WithNullFuelTankCapacity_ShouldFail() {
        // Given
        VehicleRequest request = createBasicRequest();
        request.setFuelType(FuelType.ICE);
        request.setFuelTankCapacity(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateVehicleRequest(request)
        );
        assertTrue(exception.getMessage().contains("Fuel tank capacity is required"));
    }

    @Test
    void testValidateICEVehicle_WithZeroFuelTankCapacity_ShouldFail() {
        // Given
        VehicleRequest request = createBasicRequest();
        request.setFuelType(FuelType.ICE);
        request.setFuelTankCapacity(0.0);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateVehicleRequest(request)
        );
        assertTrue(exception.getMessage().contains("Fuel tank capacity is required"));
    }

    // ===== Hybrid Vehicle Tests =====

    @Test
    void testValidateHybridVehicle_WithBothCapacities_ShouldPass() {
        // Given
        VehicleRequest request = createBasicRequest();
        request.setFuelType(FuelType.HYBRID);
        request.setBatteryCapacity(50.0);
        request.setFuelTankCapacity(45.0);

        // When & Then - should not throw
        assertDoesNotThrow(() -> validator.validateVehicleRequest(request));
    }

    @Test
    void testValidateHybridVehicle_WithOnlyBatteryCapacity_ShouldFail() {
        // Given
        VehicleRequest request = createBasicRequest();
        request.setFuelType(FuelType.HYBRID);
        request.setBatteryCapacity(50.0);
        request.setFuelTankCapacity(null);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateVehicleRequest(request)
        );
        assertTrue(exception.getMessage().contains("Fuel tank capacity is required"));
    }

    @Test
    void testValidateHybridVehicle_WithOnlyFuelTankCapacity_ShouldFail() {
        // Given
        VehicleRequest request = createBasicRequest();
        request.setFuelType(FuelType.HYBRID);
        request.setBatteryCapacity(null);
        request.setFuelTankCapacity(45.0);

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateVehicleRequest(request)
        );
        assertTrue(exception.getMessage().contains("Battery capacity is required"));
    }

    @Test
    void testValidateHybridVehicle_WithNeitherCapacity_ShouldFail() {
        // Given
        VehicleRequest request = createBasicRequest();
        request.setFuelType(FuelType.HYBRID);
        request.setBatteryCapacity(null);
        request.setFuelTankCapacity(null);

        // When & Then - should fail on battery capacity first
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateVehicleRequest(request)
        );
        assertTrue(exception.getMessage().contains("Battery capacity is required"));
    }

    // ===== Null Fuel Type Tests =====

    @Test
    void testValidateVehicle_WithNullFuelType_ShouldPass() {
        // Given - null fuel type should pass for backward compatibility
        VehicleRequest request = createBasicRequest();
        request.setFuelType(null);

        // When & Then - should not throw
        assertDoesNotThrow(() -> validator.validateVehicleRequest(request));
    }

    // ===== Optional Fields Consistency Tests =====

    @Test
    void testValidateOptionalFields_ICEWithBatteryCapacity_ShouldFail() {
        // Given
        VehicleRequest request = createBasicRequest();
        request.setFuelType(FuelType.ICE);
        request.setFuelTankCapacity(60.0);
        request.setBatteryCapacity(50.0); // Should not be set for ICE

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateOptionalFieldsConsistency(request)
        );
        assertTrue(exception.getMessage().contains("Battery capacity should not be set for ICE vehicles"));
    }

    @Test
    void testValidateOptionalFields_ICEWithChargerType_ShouldFail() {
        // Given
        VehicleRequest request = createBasicRequest();
        request.setFuelType(FuelType.ICE);
        request.setFuelTankCapacity(60.0);
        request.setDefaultChargerType("Type 2"); // Should not be set for ICE

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateOptionalFieldsConsistency(request)
        );
        assertTrue(exception.getMessage().contains("Charger type should not be set for ICE vehicles"));
    }

    @Test
    void testValidateOptionalFields_EVWithFuelTankCapacity_ShouldFail() {
        // Given
        VehicleRequest request = createBasicRequest();
        request.setFuelType(FuelType.EV);
        request.setBatteryCapacity(75.0);
        request.setFuelTankCapacity(60.0); // Should not be set for EV

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateOptionalFieldsConsistency(request)
        );
        assertTrue(exception.getMessage().contains("Fuel tank capacity should not be set for EV vehicles"));
    }

    @Test
    void testValidateOptionalFields_EVWithEngineType_ShouldFail() {
        // Given
        VehicleRequest request = createBasicRequest();
        request.setFuelType(FuelType.EV);
        request.setBatteryCapacity(75.0);
        request.setEngineType("Petrol"); // Should not be set for EV

        // When & Then
        IllegalArgumentException exception = assertThrows(
            IllegalArgumentException.class,
            () -> validator.validateOptionalFieldsConsistency(request)
        );
        assertTrue(exception.getMessage().contains("Engine type should not be set for EV vehicles"));
    }

    @Test
    void testValidateOptionalFields_HybridWithBothFields_ShouldPass() {
        // Given
        VehicleRequest request = createBasicRequest();
        request.setFuelType(FuelType.HYBRID);
        request.setBatteryCapacity(50.0);
        request.setFuelTankCapacity(45.0);
        request.setDefaultChargerType("Type 2");
        request.setEngineType("Petrol-Electric");

        // When & Then - should not throw
        assertDoesNotThrow(() -> validator.validateOptionalFieldsConsistency(request));
    }

    // ===== Helper Methods =====

    private VehicleRequest createBasicRequest() {
        VehicleRequest request = new VehicleRequest();
        request.setCompanyId(1L);
        request.setVehicleNumber("TEST001");
        request.setType(Vehicle.VehicleType.LCV);
        request.setMake("TestMake");
        request.setModel("TestModel");
        request.setYear(2024);
        request.setStatus(Vehicle.VehicleStatus.ACTIVE);
        return request;
    }
}
