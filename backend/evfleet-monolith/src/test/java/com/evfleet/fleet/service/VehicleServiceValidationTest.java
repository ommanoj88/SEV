package com.evfleet.fleet.service;

import com.evfleet.common.event.EventPublisher;
import com.evfleet.common.exception.InvalidInputException;
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
 * Unit tests for VehicleService multi-fuel validation logic
 * 
 * Tests the validation requirements specified in 1.MULTI_FUEL_ANALYSIS.md:
 * - EV vehicles must have batteryCapacity and defaultChargerType
 * - ICE vehicles must have fuelTankCapacity
 * - HYBRID vehicles must have both battery and fuel fields
 * - Battery SOC must be between 0-100
 * - Fuel level must not exceed tank capacity
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class VehicleServiceValidationTest {

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
                .status(Vehicle.VehicleStatus.ACTIVE);
    }

    // ===== EV Validation Tests =====

    @Test
    void testCreateEV_WithValidFields_Success() {
        Vehicle vehicle = createBaseVehicle()
                .fuelType(FuelType.EV)
                .batteryCapacity(50.0)
                .defaultChargerType("CCS")
                .currentBatterySoc(80.0)
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);
        when(vehicleRepository.save(any())).thenReturn(vehicle);

        Vehicle result = vehicleService.createVehicle(vehicle);
        assertNotNull(result);
    }

    @Test
    void testCreateEV_WithoutBatteryCapacity_ThrowsException() {
        Vehicle vehicle = createBaseVehicle()
                .fuelType(FuelType.EV)
                .defaultChargerType("CCS")
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> vehicleService.createVehicle(vehicle));
        assertTrue(exception.getMessage().contains("batteryCapacity"));
    }

    @Test
    void testCreateEV_WithZeroBatteryCapacity_ThrowsException() {
        Vehicle vehicle = createBaseVehicle()
                .fuelType(FuelType.EV)
                .batteryCapacity(0.0)
                .defaultChargerType("CCS")
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> vehicleService.createVehicle(vehicle));
        assertTrue(exception.getMessage().contains("batteryCapacity"));
    }

    @Test
    void testCreateEV_WithoutDefaultChargerType_ThrowsException() {
        Vehicle vehicle = createBaseVehicle()
                .fuelType(FuelType.EV)
                .batteryCapacity(50.0)
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> vehicleService.createVehicle(vehicle));
        assertTrue(exception.getMessage().contains("defaultChargerType"));
    }

    // ===== ICE Validation Tests =====

    @Test
    void testCreateICE_WithValidFields_Success() {
        Vehicle vehicle = createBaseVehicle()
                .fuelType(FuelType.ICE)
                .fuelTankCapacity(50.0)
                .fuelLevel(30.0)
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);
        when(vehicleRepository.save(any())).thenReturn(vehicle);

        Vehicle result = vehicleService.createVehicle(vehicle);
        assertNotNull(result);
    }

    @Test
    void testCreateICE_WithoutFuelTankCapacity_ThrowsException() {
        Vehicle vehicle = createBaseVehicle()
                .fuelType(FuelType.ICE)
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> vehicleService.createVehicle(vehicle));
        assertTrue(exception.getMessage().contains("fuelTankCapacity"));
    }

    @Test
    void testCreateICE_WithZeroFuelTankCapacity_ThrowsException() {
        Vehicle vehicle = createBaseVehicle()
                .fuelType(FuelType.ICE)
                .fuelTankCapacity(0.0)
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> vehicleService.createVehicle(vehicle));
        assertTrue(exception.getMessage().contains("fuelTankCapacity"));
    }

    // ===== HYBRID Validation Tests =====

    @Test
    void testCreateHybrid_WithValidFields_Success() {
        Vehicle vehicle = createBaseVehicle()
                .fuelType(FuelType.HYBRID)
                .batteryCapacity(20.0)
                .currentBatterySoc(50.0)
                .fuelTankCapacity(40.0)
                .fuelLevel(25.0)
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);
        when(vehicleRepository.save(any())).thenReturn(vehicle);

        Vehicle result = vehicleService.createVehicle(vehicle);
        assertNotNull(result);
    }

    @Test
    void testCreateHybrid_WithoutBatteryCapacity_ThrowsException() {
        Vehicle vehicle = createBaseVehicle()
                .fuelType(FuelType.HYBRID)
                .fuelTankCapacity(40.0)
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> vehicleService.createVehicle(vehicle));
        assertTrue(exception.getMessage().contains("batteryCapacity"));
    }

    @Test
    void testCreateHybrid_WithoutFuelTankCapacity_ThrowsException() {
        Vehicle vehicle = createBaseVehicle()
                .fuelType(FuelType.HYBRID)
                .batteryCapacity(20.0)
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> vehicleService.createVehicle(vehicle));
        assertTrue(exception.getMessage().contains("fuelTankCapacity"));
    }

    // ===== State Validation Tests =====

    @Test
    void testCreateEV_WithInvalidBatterySoc_Negative_ThrowsException() {
        Vehicle vehicle = createBaseVehicle()
                .fuelType(FuelType.EV)
                .batteryCapacity(50.0)
                .defaultChargerType("CCS")
                .currentBatterySoc(-10.0)
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> vehicleService.createVehicle(vehicle));
        assertTrue(exception.getMessage().contains("currentBatterySoc"));
        assertTrue(exception.getMessage().contains("0 and 100"));
    }

    @Test
    void testCreateEV_WithInvalidBatterySoc_Over100_ThrowsException() {
        Vehicle vehicle = createBaseVehicle()
                .fuelType(FuelType.EV)
                .batteryCapacity(50.0)
                .defaultChargerType("CCS")
                .currentBatterySoc(150.0)
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> vehicleService.createVehicle(vehicle));
        assertTrue(exception.getMessage().contains("currentBatterySoc"));
        assertTrue(exception.getMessage().contains("0 and 100"));
    }

    @Test
    void testCreateICE_WithFuelLevelExceedingCapacity_ThrowsException() {
        Vehicle vehicle = createBaseVehicle()
                .fuelType(FuelType.ICE)
                .fuelTankCapacity(50.0)
                .fuelLevel(100.0)  // Exceeds capacity
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> vehicleService.createVehicle(vehicle));
        assertTrue(exception.getMessage().contains("fuelLevel"));
        assertTrue(exception.getMessage().contains("cannot exceed"));
    }

    @Test
    void testCreateICE_WithNegativeFuelLevel_ThrowsException() {
        Vehicle vehicle = createBaseVehicle()
                .fuelType(FuelType.ICE)
                .fuelTankCapacity(50.0)
                .fuelLevel(-5.0)
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> vehicleService.createVehicle(vehicle));
        assertTrue(exception.getMessage().contains("fuelLevel"));
        assertTrue(exception.getMessage().contains("greater than or equal to 0"));
    }

    @Test
    void testCreateVehicle_WithNullFuelType_ThrowsException() {
        Vehicle vehicle = createBaseVehicle()
                .fuelType(null)
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> vehicleService.createVehicle(vehicle));
        assertTrue(exception.getMessage().contains("fuelType"));
    }

    // ===== Year Validation Tests =====

    @Test
    void testCreateVehicle_WithNullYear_ThrowsException() {
        Vehicle vehicle = createBaseVehicle()
                .fuelType(FuelType.EV)
                .batteryCapacity(50.0)
                .defaultChargerType("CCS")
                .year(null)
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> vehicleService.createVehicle(vehicle));
        assertTrue(exception.getMessage().contains("year"));
    }

    @Test
    void testCreateVehicle_WithYearBefore1900_ThrowsException() {
        Vehicle vehicle = createBaseVehicle()
                .fuelType(FuelType.EV)
                .batteryCapacity(50.0)
                .defaultChargerType("CCS")
                .year(1899)
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> vehicleService.createVehicle(vehicle));
        assertTrue(exception.getMessage().contains("year"));
        assertTrue(exception.getMessage().contains("1900"));
    }

    @Test
    void testCreateVehicle_WithFutureYear_ThrowsException() {
        Vehicle vehicle = createBaseVehicle()
                .fuelType(FuelType.EV)
                .batteryCapacity(50.0)
                .defaultChargerType("CCS")
                .year(2100)  // Way in the future
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> vehicleService.createVehicle(vehicle));
        assertTrue(exception.getMessage().contains("year"));
    }

    // ===== Charger Type Validation Tests =====

    @Test
    void testCreateEV_WithInvalidChargerType_ThrowsException() {
        Vehicle vehicle = createBaseVehicle()
                .fuelType(FuelType.EV)
                .batteryCapacity(50.0)
                .defaultChargerType("INVALID_CHARGER")
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> vehicleService.createVehicle(vehicle));
        assertTrue(exception.getMessage().contains("defaultChargerType"));
        assertTrue(exception.getMessage().contains("must be one of"));
    }

    @Test
    void testCreateEV_WithValidCCSCharger_Success() {
        Vehicle vehicle = createBaseVehicle()
                .fuelType(FuelType.EV)
                .batteryCapacity(50.0)
                .defaultChargerType("CCS2")
                .currentBatterySoc(80.0)
                .build();

        when(vehicleRepository.existsByVehicleNumber(any())).thenReturn(false);
        when(vehicleRepository.save(any())).thenReturn(vehicle);

        Vehicle result = vehicleService.createVehicle(vehicle);
        assertNotNull(result);
    }

    // ===== Location Validation Tests =====

    @Test
    void testUpdateLocation_WithInvalidLatitude_TooLow_ThrowsException() {
        Vehicle vehicle = createBaseVehicle()
                .id(1L)
                .fuelType(FuelType.EV)
                .batteryCapacity(50.0)
                .defaultChargerType("CCS")
                .build();

        when(vehicleRepository.findById(1L)).thenReturn(java.util.Optional.of(vehicle));

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> vehicleService.updateVehicleLocation(1L, -91.0, 0.0));
        assertTrue(exception.getMessage().contains("latitude"));
        assertTrue(exception.getMessage().contains("-90"));
    }

    @Test
    void testUpdateLocation_WithInvalidLatitude_TooHigh_ThrowsException() {
        Vehicle vehicle = createBaseVehicle()
                .id(1L)
                .fuelType(FuelType.EV)
                .batteryCapacity(50.0)
                .defaultChargerType("CCS")
                .build();

        when(vehicleRepository.findById(1L)).thenReturn(java.util.Optional.of(vehicle));

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> vehicleService.updateVehicleLocation(1L, 91.0, 0.0));
        assertTrue(exception.getMessage().contains("latitude"));
        assertTrue(exception.getMessage().contains("90"));
    }

    @Test
    void testUpdateLocation_WithInvalidLongitude_TooLow_ThrowsException() {
        Vehicle vehicle = createBaseVehicle()
                .id(1L)
                .fuelType(FuelType.EV)
                .batteryCapacity(50.0)
                .defaultChargerType("CCS")
                .build();

        when(vehicleRepository.findById(1L)).thenReturn(java.util.Optional.of(vehicle));

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> vehicleService.updateVehicleLocation(1L, 0.0, -181.0));
        assertTrue(exception.getMessage().contains("longitude"));
        assertTrue(exception.getMessage().contains("-180"));
    }

    @Test
    void testUpdateLocation_WithInvalidLongitude_TooHigh_ThrowsException() {
        Vehicle vehicle = createBaseVehicle()
                .id(1L)
                .fuelType(FuelType.EV)
                .batteryCapacity(50.0)
                .defaultChargerType("CCS")
                .build();

        when(vehicleRepository.findById(1L)).thenReturn(java.util.Optional.of(vehicle));

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> vehicleService.updateVehicleLocation(1L, 0.0, 181.0));
        assertTrue(exception.getMessage().contains("longitude"));
        assertTrue(exception.getMessage().contains("180"));
    }

    @Test
    void testUpdateLocation_WithValidCoordinates_Success() {
        Vehicle vehicle = createBaseVehicle()
                .id(1L)
                .fuelType(FuelType.EV)
                .batteryCapacity(50.0)
                .defaultChargerType("CCS")
                .currentBatterySoc(80.0)
                .type(Vehicle.VehicleType.TWO_WHEELER)
                .build();

        when(vehicleRepository.findById(1L)).thenReturn(java.util.Optional.of(vehicle));
        when(vehicleRepository.save(any())).thenReturn(vehicle);

        Vehicle result = vehicleService.updateVehicleLocation(1L, 28.6139, 77.2090);  // Delhi coordinates
        assertNotNull(result);
    }

    // ===== Delete Vehicle Tests =====

    @Test
    void testDeleteVehicle_WhenInTrip_ThrowsException() {
        Vehicle vehicle = createBaseVehicle()
                .id(1L)
                .fuelType(FuelType.EV)
                .batteryCapacity(50.0)
                .defaultChargerType("CCS")
                .status(Vehicle.VehicleStatus.IN_TRIP)
                .build();

        when(vehicleRepository.findById(1L)).thenReturn(java.util.Optional.of(vehicle));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> vehicleService.deleteVehicle(1L));
        assertTrue(exception.getMessage().contains("active trip"));
    }

    @Test
    void testDeleteVehicle_WhenHasAssignedDriver_ThrowsException() {
        Vehicle vehicle = createBaseVehicle()
                .id(1L)
                .fuelType(FuelType.EV)
                .batteryCapacity(50.0)
                .defaultChargerType("CCS")
                .status(Vehicle.VehicleStatus.ACTIVE)
                .currentDriverId(100L)
                .build();

        when(vehicleRepository.findById(1L)).thenReturn(java.util.Optional.of(vehicle));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> vehicleService.deleteVehicle(1L));
        assertTrue(exception.getMessage().contains("assigned driver"));
    }

    @Test
    void testDeleteVehicle_WhenCharging_ThrowsException() {
        Vehicle vehicle = createBaseVehicle()
                .id(1L)
                .fuelType(FuelType.EV)
                .batteryCapacity(50.0)
                .defaultChargerType("CCS")
                .status(Vehicle.VehicleStatus.CHARGING)
                .build();

        when(vehicleRepository.findById(1L)).thenReturn(java.util.Optional.of(vehicle));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> vehicleService.deleteVehicle(1L));
        assertTrue(exception.getMessage().contains("charging"));
    }

    @Test
    void testDeleteVehicle_WhenInactive_Success() {
        Vehicle vehicle = createBaseVehicle()
                .id(1L)
                .fuelType(FuelType.EV)
                .batteryCapacity(50.0)
                .defaultChargerType("CCS")
                .status(Vehicle.VehicleStatus.INACTIVE)
                .currentDriverId(null)
                .build();

        when(vehicleRepository.findById(1L)).thenReturn(java.util.Optional.of(vehicle));

        // Should not throw
        assertDoesNotThrow(() -> vehicleService.deleteVehicle(1L));
    }

    // ===== Update Vehicle Tests =====

    @Test
    void testUpdateVehicle_WithDuplicateVehicleNumber_ThrowsException() {
        Vehicle existingVehicle = createBaseVehicle()
                .id(1L)
                .fuelType(FuelType.EV)
                .batteryCapacity(50.0)
                .defaultChargerType("CCS")
                .vehicleNumber("EXISTING-001")
                .build();

        Vehicle updateRequest = Vehicle.builder()
                .vehicleNumber("DUPLICATE-002")
                .build();

        when(vehicleRepository.findById(1L)).thenReturn(java.util.Optional.of(existingVehicle));
        when(vehicleRepository.existsByVehicleNumber("DUPLICATE-002")).thenReturn(true);

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
                () -> vehicleService.updateVehicle(1L, updateRequest));
        assertTrue(exception.getMessage().contains("Vehicle number already exists"));
    }

    @Test
    void testUpdateVehicle_ChangeFuelTypeWithValidFields_Success() {
        Vehicle existingVehicle = createBaseVehicle()
                .id(1L)
                .fuelType(FuelType.EV)
                .batteryCapacity(50.0)
                .defaultChargerType("CCS")
                .build();

        Vehicle updateRequest = Vehicle.builder()
                .fuelType(FuelType.ICE)
                .fuelTankCapacity(60.0)
                .build();

        when(vehicleRepository.findById(1L)).thenReturn(java.util.Optional.of(existingVehicle));
        when(vehicleRepository.save(any())).thenReturn(existingVehicle);

        Vehicle result = vehicleService.updateVehicle(1L, updateRequest);
        assertNotNull(result);
    }

    @Test
    void testUpdateVehicle_ChangeFuelTypeWithoutRequiredFields_ThrowsException() {
        Vehicle existingVehicle = createBaseVehicle()
                .id(1L)
                .fuelType(FuelType.EV)
                .batteryCapacity(50.0)
                .defaultChargerType("CCS")
                .build();

        Vehicle updateRequest = Vehicle.builder()
                .fuelType(FuelType.ICE)  // Changing to ICE without fuelTankCapacity
                .build();

        when(vehicleRepository.findById(1L)).thenReturn(java.util.Optional.of(existingVehicle));

        InvalidInputException exception = assertThrows(InvalidInputException.class,
                () -> vehicleService.updateVehicle(1L, updateRequest));
        assertTrue(exception.getMessage().contains("fuelTankCapacity"));
    }
}
