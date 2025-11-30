package com.evfleet.fleet.service;

import com.evfleet.common.event.EventPublisher;
import com.evfleet.common.exception.InvalidInputException;
import com.evfleet.common.exception.ResourceNotFoundException;
import com.evfleet.driver.repository.DriverRepository;
import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.VehicleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit tests for VehicleService
 * PR #42: Backend Service Unit Tests
 *
 * Tests cover:
 * - createVehicle with various fuel types
 * - updateVehicle with validation
 * - deleteVehicle with safety checks
 * - getVehicleById
 * - getVehiclesByCompany
 * - assignDriver
 * - unassignDriver
 * - updateBatterySoc
 * - Validation scenarios
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("VehicleService Tests")
class VehicleServiceTest {

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private EventPublisher eventPublisher;

    @InjectMocks
    private VehicleService vehicleService;

    private Vehicle testEVVehicle;
    private Vehicle testICEVehicle;
    private Vehicle testHybridVehicle;

    @BeforeEach
    void setUp() {
        testEVVehicle = Vehicle.builder()
                .id(1L)
                .vehicleNumber("EV-001")
                .make("Tesla")
                .model("Model 3")
                .year(2023)
                .fuelType(FuelType.EV)
                .batteryCapacity(75.0)
                .currentBatterySoc(85.0)
                .defaultChargerType("CCS2")
                .companyId(100L)
                .status(Vehicle.VehicleStatus.AVAILABLE)
                .build();

        testICEVehicle = Vehicle.builder()
                .id(2L)
                .vehicleNumber("ICE-001")
                .make("Toyota")
                .model("Camry")
                .year(2023)
                .fuelType(FuelType.ICE)
                .fuelTankCapacity(60.0)
                .fuelLevel(45.0)
                .companyId(100L)
                .status(Vehicle.VehicleStatus.AVAILABLE)
                .build();

        testHybridVehicle = Vehicle.builder()
                .id(3L)
                .vehicleNumber("HYB-001")
                .make("Toyota")
                .model("Prius")
                .year(2023)
                .fuelType(FuelType.HYBRID)
                .batteryCapacity(8.8)
                .currentBatterySoc(70.0)
                .defaultChargerType("Type2")
                .fuelTankCapacity(43.0)
                .fuelLevel(30.0)
                .companyId(100L)
                .status(Vehicle.VehicleStatus.AVAILABLE)
                .build();
    }

    @Nested
    @DisplayName("createVehicle")
    class CreateVehicle {

        @Test
        @DisplayName("Should create EV vehicle successfully")
        void shouldCreateEVVehicleSuccessfully() {
            given(vehicleRepository.existsByVehicleNumber("EV-001")).willReturn(false);
            given(vehicleRepository.save(any(Vehicle.class))).willReturn(testEVVehicle);

            Vehicle result = vehicleService.createVehicle(testEVVehicle);

            assertThat(result).isNotNull();
            assertThat(result.getVehicleNumber()).isEqualTo("EV-001");
            assertThat(result.getFuelType()).isEqualTo(FuelType.EV);
            verify(vehicleRepository).save(any(Vehicle.class));
            verify(eventPublisher).publish(any());
        }

        @Test
        @DisplayName("Should create ICE vehicle successfully")
        void shouldCreateICEVehicleSuccessfully() {
            given(vehicleRepository.existsByVehicleNumber("ICE-001")).willReturn(false);
            given(vehicleRepository.save(any(Vehicle.class))).willReturn(testICEVehicle);

            Vehicle result = vehicleService.createVehicle(testICEVehicle);

            assertThat(result).isNotNull();
            assertThat(result.getFuelType()).isEqualTo(FuelType.ICE);
            verify(vehicleRepository).save(any(Vehicle.class));
        }

        @Test
        @DisplayName("Should throw exception for duplicate vehicle number")
        void shouldThrowExceptionForDuplicateVehicleNumber() {
            given(vehicleRepository.existsByVehicleNumber("EV-001")).willReturn(true);

            assertThatThrownBy(() -> vehicleService.createVehicle(testEVVehicle))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Vehicle number already exists");

            verify(vehicleRepository, never()).save(any(Vehicle.class));
        }

        @Test
        @DisplayName("Should throw exception for null vehicle number")
        void shouldThrowExceptionForNullVehicleNumber() {
            testEVVehicle.setVehicleNumber(null);

            assertThatThrownBy(() -> vehicleService.createVehicle(testEVVehicle))
                    .isInstanceOf(InvalidInputException.class);

            verify(vehicleRepository, never()).save(any(Vehicle.class));
        }

        @Test
        @DisplayName("Should throw exception for EV without battery capacity")
        void shouldThrowExceptionForEVWithoutBatteryCapacity() {
            testEVVehicle.setBatteryCapacity(null);
            given(vehicleRepository.existsByVehicleNumber("EV-001")).willReturn(false);

            assertThatThrownBy(() -> vehicleService.createVehicle(testEVVehicle))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessageContaining("batteryCapacity");
        }

        @Test
        @DisplayName("Should throw exception for ICE without fuel tank capacity")
        void shouldThrowExceptionForICEWithoutFuelTankCapacity() {
            testICEVehicle.setFuelTankCapacity(null);
            given(vehicleRepository.existsByVehicleNumber("ICE-001")).willReturn(false);

            assertThatThrownBy(() -> vehicleService.createVehicle(testICEVehicle))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessageContaining("fuelTankCapacity");
        }

        @Test
        @DisplayName("Should throw exception for invalid charger type")
        void shouldThrowExceptionForInvalidChargerType() {
            testEVVehicle.setDefaultChargerType("INVALID_TYPE");
            given(vehicleRepository.existsByVehicleNumber("EV-001")).willReturn(false);

            assertThatThrownBy(() -> vehicleService.createVehicle(testEVVehicle))
                    .isInstanceOf(InvalidInputException.class)
                    .hasMessageContaining("defaultChargerType");
        }
    }

    @Nested
    @DisplayName("updateVehicle")
    class UpdateVehicle {

        @Test
        @DisplayName("Should update vehicle successfully")
        void shouldUpdateVehicleSuccessfully() {
            Vehicle updatedVehicle = Vehicle.builder()
                    .make("Tesla")
                    .model("Model Y")
                    .year(2024)
                    .build();

            given(vehicleRepository.findById(1L)).willReturn(Optional.of(testEVVehicle));
            given(vehicleRepository.save(any(Vehicle.class))).willAnswer(i -> i.getArgument(0));

            Vehicle result = vehicleService.updateVehicle(1L, updatedVehicle);

            assertThat(result.getModel()).isEqualTo("Model Y");
            assertThat(result.getYear()).isEqualTo(2024);
            verify(vehicleRepository).save(any(Vehicle.class));
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent vehicle")
        void shouldThrowExceptionWhenUpdatingNonExistent() {
            given(vehicleRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> vehicleService.updateVehicle(999L, testEVVehicle))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should throw exception for duplicate vehicle number on update")
        void shouldThrowExceptionForDuplicateVehicleNumberOnUpdate() {
            Vehicle updatedVehicle = Vehicle.builder()
                    .vehicleNumber("EXISTING-001")
                    .build();

            given(vehicleRepository.findById(1L)).willReturn(Optional.of(testEVVehicle));
            given(vehicleRepository.existsByVehicleNumber("EXISTING-001")).willReturn(true);

            assertThatThrownBy(() -> vehicleService.updateVehicle(1L, updatedVehicle))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Vehicle number already exists");
        }
    }

    @Nested
    @DisplayName("deleteVehicle")
    class DeleteVehicle {

        @Test
        @DisplayName("Should delete vehicle successfully")
        void shouldDeleteVehicleSuccessfully() {
            given(vehicleRepository.findById(1L)).willReturn(Optional.of(testEVVehicle));

            vehicleService.deleteVehicle(1L);

            verify(vehicleRepository).delete(testEVVehicle);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent vehicle")
        void shouldThrowExceptionWhenDeletingNonExistent() {
            given(vehicleRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> vehicleService.deleteVehicle(999L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(vehicleRepository, never()).delete(any(Vehicle.class));
        }

        @Test
        @DisplayName("Should throw exception when deleting vehicle in active trip")
        void shouldThrowExceptionWhenDeletingVehicleInTrip() {
            testEVVehicle.setStatus(Vehicle.VehicleStatus.IN_TRIP);
            given(vehicleRepository.findById(1L)).willReturn(Optional.of(testEVVehicle));

            assertThatThrownBy(() -> vehicleService.deleteVehicle(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("active trip");

            verify(vehicleRepository, never()).delete(any(Vehicle.class));
        }

        @Test
        @DisplayName("Should throw exception when deleting vehicle with assigned driver")
        void shouldThrowExceptionWhenDeletingVehicleWithDriver() {
            testEVVehicle.setCurrentDriverId(50L);
            given(vehicleRepository.findById(1L)).willReturn(Optional.of(testEVVehicle));

            assertThatThrownBy(() -> vehicleService.deleteVehicle(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("assigned driver");

            verify(vehicleRepository, never()).delete(any(Vehicle.class));
        }

        @Test
        @DisplayName("Should throw exception when deleting charging vehicle")
        void shouldThrowExceptionWhenDeletingChargingVehicle() {
            testEVVehicle.setStatus(Vehicle.VehicleStatus.CHARGING);
            given(vehicleRepository.findById(1L)).willReturn(Optional.of(testEVVehicle));

            assertThatThrownBy(() -> vehicleService.deleteVehicle(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("charging");

            verify(vehicleRepository, never()).delete(any(Vehicle.class));
        }
    }

    @Nested
    @DisplayName("getVehicle operations")
    class GetVehicleOperations {

        @Test
        @DisplayName("Should get vehicle by ID")
        void shouldGetVehicleById() {
            given(vehicleRepository.findById(1L)).willReturn(Optional.of(testEVVehicle));

            Vehicle result = vehicleService.getVehicleById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw exception when vehicle not found")
        void shouldThrowExceptionWhenVehicleNotFound() {
            given(vehicleRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> vehicleService.getVehicleById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should get vehicles by company")
        void shouldGetVehiclesByCompany() {
            List<Vehicle> vehicles = Arrays.asList(testEVVehicle, testICEVehicle);
            given(vehicleRepository.findByCompanyId(100L)).willReturn(vehicles);

            List<Vehicle> result = vehicleService.getVehiclesByCompany(100L);

            assertThat(result).hasSize(2);
            verify(vehicleRepository).findByCompanyId(100L);
        }

        @Test
        @DisplayName("Should get vehicles by status")
        void shouldGetVehiclesByStatus() {
            List<Vehicle> vehicles = Arrays.asList(testEVVehicle);
            given(vehicleRepository.findByCompanyIdAndStatus(100L, Vehicle.VehicleStatus.AVAILABLE))
                    .willReturn(vehicles);

            List<Vehicle> result = vehicleService.getVehiclesByStatus(100L, Vehicle.VehicleStatus.AVAILABLE);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getStatus()).isEqualTo(Vehicle.VehicleStatus.AVAILABLE);
        }
    }

    @Nested
    @DisplayName("assignDriver")
    class AssignDriver {

        @Test
        @DisplayName("Should assign driver to vehicle")
        void shouldAssignDriverToVehicle() {
            given(vehicleRepository.findById(1L)).willReturn(Optional.of(testEVVehicle));
            given(vehicleRepository.save(any(Vehicle.class))).willAnswer(i -> i.getArgument(0));

            vehicleService.assignDriver(1L, 50L);

            verify(vehicleRepository).save(argThat(vehicle -> 
                vehicle.getCurrentDriverId().equals(50L)
            ));
        }

        @Test
        @DisplayName("Should throw exception when assigning to non-existent vehicle")
        void shouldThrowExceptionWhenAssigningToNonExistent() {
            given(vehicleRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> vehicleService.assignDriver(999L, 50L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateBatterySoc")
    class UpdateBatterySoc {

        @Test
        @DisplayName("Should update battery SOC successfully")
        void shouldUpdateBatterySocSuccessfully() {
            given(vehicleRepository.findById(1L)).willReturn(Optional.of(testEVVehicle));
            given(vehicleRepository.save(any(Vehicle.class))).willAnswer(i -> i.getArgument(0));

            vehicleService.updateBatterySoc(1L, 90.0);

            verify(vehicleRepository).save(argThat(vehicle ->
                vehicle.getCurrentBatterySoc().equals(90.0)
            ));
        }

        @Test
        @DisplayName("Should throw exception for invalid battery SOC")
        void shouldThrowExceptionForInvalidBatterySoc() {
            given(vehicleRepository.findById(1L)).willReturn(Optional.of(testEVVehicle));

            assertThatThrownBy(() -> vehicleService.updateBatterySoc(1L, 150.0))
                    .isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should publish event when battery is low")
        void shouldPublishEventWhenBatteryIsLow() {
            given(vehicleRepository.findById(1L)).willReturn(Optional.of(testEVVehicle));
            given(vehicleRepository.save(any(Vehicle.class))).willAnswer(i -> i.getArgument(0));

            vehicleService.updateBatterySoc(1L, 15.0);

            verify(eventPublisher).publish(any());
        }
    }

    @Nested
    @DisplayName("Hybrid Vehicle Validation")
    class HybridVehicleValidation {

        @Test
        @DisplayName("Should create hybrid vehicle with all required fields")
        void shouldCreateHybridVehicleSuccessfully() {
            given(vehicleRepository.existsByVehicleNumber("HYB-001")).willReturn(false);
            given(vehicleRepository.save(any(Vehicle.class))).willReturn(testHybridVehicle);

            Vehicle result = vehicleService.createVehicle(testHybridVehicle);

            assertThat(result).isNotNull();
            assertThat(result.getFuelType()).isEqualTo(FuelType.HYBRID);
            assertThat(result.getBatteryCapacity()).isNotNull();
            assertThat(result.getFuelTankCapacity()).isNotNull();
        }

        @Test
        @DisplayName("Should throw exception for hybrid without battery capacity")
        void shouldThrowExceptionForHybridWithoutBattery() {
            testHybridVehicle.setBatteryCapacity(null);
            given(vehicleRepository.existsByVehicleNumber("HYB-001")).willReturn(false);

            assertThatThrownBy(() -> vehicleService.createVehicle(testHybridVehicle))
                    .isInstanceOf(InvalidInputException.class);
        }

        @Test
        @DisplayName("Should throw exception for hybrid without fuel tank")
        void shouldThrowExceptionForHybridWithoutFuelTank() {
            testHybridVehicle.setFuelTankCapacity(null);
            given(vehicleRepository.existsByVehicleNumber("HYB-001")).willReturn(false);

            assertThatThrownBy(() -> vehicleService.createVehicle(testHybridVehicle))
                    .isInstanceOf(InvalidInputException.class);
        }
    }
}
