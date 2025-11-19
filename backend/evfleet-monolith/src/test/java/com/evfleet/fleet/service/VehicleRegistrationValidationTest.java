package com.evfleet.fleet.service;

import com.evfleet.common.event.EventPublisher;
import com.evfleet.driver.repository.DriverRepository;
import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

/**
 * Unit tests for VehicleService registration validation logic
 * 
 * Tests the validation requirements specified in 2.VEHICLE_REGISTRATION_ANALYSIS.md:
 * - License plate must be unique
 * - VIN must be unique
 * - These validations are in addition to existing vehicle number uniqueness
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class VehicleRegistrationValidationTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private EventPublisher eventPublisher;

    private VehicleService vehicleService;

    @BeforeEach
    void setUp() {
        vehicleService = new VehicleService(vehicleRepository, driverRepository, eventPublisher);
    }

    private Vehicle.VehicleBuilder createBaseVehicle() {
        return Vehicle.builder()
                .companyId(1L)
                .vehicleNumber("TEST-001")
                .type(Vehicle.VehicleType.TWO_WHEELER)
                .make("Test Make")
                .model("Test Model")
                .year(2024)
                .status(Vehicle.VehicleStatus.ACTIVE)
                .fuelType(FuelType.EV)
                .batteryCapacity(50.0)
                .defaultChargerType("CCS");
    }

    // ===== License Plate Uniqueness Tests =====

    @Test
    void testCreateVehicle_WithUniqueLicensePlate_Success() {
        Vehicle vehicle = createBaseVehicle()
                .licensePlate("KA01AB1234")
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);
        when(vehicleRepository.existsByLicensePlate(any())).thenReturn(false);
        when(vehicleRepository.save(any())).thenReturn(vehicle);

        Vehicle result = vehicleService.createVehicle(vehicle);
        assertNotNull(result);
    }

    @Test
    void testCreateVehicle_WithDuplicateLicensePlate_ThrowsException() {
        Vehicle vehicle = createBaseVehicle()
                .licensePlate("KA01AB1234")
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);
        when(vehicleRepository.existsByLicensePlate("KA01AB1234")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> vehicleService.createVehicle(vehicle));
        assertTrue(exception.getMessage().contains("License plate already exists"));
    }

    @Test
    void testCreateVehicle_WithNullLicensePlate_Success() {
        Vehicle vehicle = createBaseVehicle()
                .licensePlate(null)
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);
        when(vehicleRepository.save(any())).thenReturn(vehicle);

        Vehicle result = vehicleService.createVehicle(vehicle);
        assertNotNull(result);
    }

    @Test
    void testCreateVehicle_WithEmptyLicensePlate_Success() {
        Vehicle vehicle = createBaseVehicle()
                .licensePlate("")
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);
        when(vehicleRepository.save(any())).thenReturn(vehicle);

        Vehicle result = vehicleService.createVehicle(vehicle);
        assertNotNull(result);
    }

    @Test
    void testCreateVehicle_WithWhitespaceLicensePlate_Success() {
        Vehicle vehicle = createBaseVehicle()
                .licensePlate("   ")
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);
        when(vehicleRepository.save(any())).thenReturn(vehicle);

        Vehicle result = vehicleService.createVehicle(vehicle);
        assertNotNull(result);
    }

    // ===== VIN Uniqueness Tests =====

    @Test
    void testCreateVehicle_WithUniqueVIN_Success() {
        Vehicle vehicle = createBaseVehicle()
                .vin("1HGBH41JXMN109186")
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);
        when(vehicleRepository.existsByVin(any())).thenReturn(false);
        when(vehicleRepository.save(any())).thenReturn(vehicle);

        Vehicle result = vehicleService.createVehicle(vehicle);
        assertNotNull(result);
    }

    @Test
    void testCreateVehicle_WithDuplicateVIN_ThrowsException() {
        Vehicle vehicle = createBaseVehicle()
                .vin("1HGBH41JXMN109186")
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);
        when(vehicleRepository.existsByVin("1HGBH41JXMN109186")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> vehicleService.createVehicle(vehicle));
        assertTrue(exception.getMessage().contains("VIN already exists"));
    }

    @Test
    void testCreateVehicle_WithNullVIN_Success() {
        Vehicle vehicle = createBaseVehicle()
                .vin(null)
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);
        when(vehicleRepository.save(any())).thenReturn(vehicle);

        Vehicle result = vehicleService.createVehicle(vehicle);
        assertNotNull(result);
    }

    @Test
    void testCreateVehicle_WithEmptyVIN_Success() {
        Vehicle vehicle = createBaseVehicle()
                .vin("")
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);
        when(vehicleRepository.save(any())).thenReturn(vehicle);

        Vehicle result = vehicleService.createVehicle(vehicle);
        assertNotNull(result);
    }

    @Test
    void testCreateVehicle_WithWhitespaceVIN_Success() {
        Vehicle vehicle = createBaseVehicle()
                .vin("   ")
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);
        when(vehicleRepository.save(any())).thenReturn(vehicle);

        Vehicle result = vehicleService.createVehicle(vehicle);
        assertNotNull(result);
    }

    // ===== Combined Validation Tests =====

    @Test
    void testCreateVehicle_WithBothLicensePlateAndVIN_Success() {
        Vehicle vehicle = createBaseVehicle()
                .licensePlate("KA01AB1234")
                .vin("1HGBH41JXMN109186")
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);
        when(vehicleRepository.existsByLicensePlate(any())).thenReturn(false);
        when(vehicleRepository.existsByVin(any())).thenReturn(false);
        when(vehicleRepository.save(any())).thenReturn(vehicle);

        Vehicle result = vehicleService.createVehicle(vehicle);
        assertNotNull(result);
    }

    @Test
    void testCreateVehicle_WithDuplicateLicensePlateAndUniqueVIN_ThrowsException() {
        Vehicle vehicle = createBaseVehicle()
                .licensePlate("KA01AB1234")
                .vin("1HGBH41JXMN109186")
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);
        when(vehicleRepository.existsByLicensePlate("KA01AB1234")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> vehicleService.createVehicle(vehicle));
        assertTrue(exception.getMessage().contains("License plate already exists"));
    }

    @Test
    void testCreateVehicle_WithUniqueLicensePlateAndDuplicateVIN_ThrowsException() {
        Vehicle vehicle = createBaseVehicle()
                .licensePlate("KA01AB1234")
                .vin("1HGBH41JXMN109186")
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);
        when(vehicleRepository.existsByLicensePlate(any())).thenReturn(false);
        when(vehicleRepository.existsByVin("1HGBH41JXMN109186")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> vehicleService.createVehicle(vehicle));
        assertTrue(exception.getMessage().contains("VIN already exists"));
    }

    // ===== Existing Vehicle Number Uniqueness Test (Regression) =====

    @Test
    void testCreateVehicle_WithDuplicateVehicleNumber_ThrowsException() {
        Vehicle vehicle = createBaseVehicle()
                .build();

        when(vehicleRepository.existsByVehicleNumber("TEST-001")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> vehicleService.createVehicle(vehicle));
        assertTrue(exception.getMessage().contains("Vehicle number already exists"));
    }
}
