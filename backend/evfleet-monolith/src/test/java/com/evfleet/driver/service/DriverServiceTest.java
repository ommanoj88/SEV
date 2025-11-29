package com.evfleet.driver.service;

import com.evfleet.common.exception.ResourceNotFoundException;
import com.evfleet.driver.dto.DriverRequest;
import com.evfleet.driver.dto.DriverResponse;
import com.evfleet.driver.model.Driver;
import com.evfleet.driver.repository.DriverRepository;
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

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for DriverService
 * 
 * PR #8: Driver Assignment Validation
 * Tests cover:
 * - Double-assignment prevention
 * - License expiry validation
 * - Driver status validation
 * - Vehicle status validation
 * - Cross-company security validation
 * - Audit logging scenarios
 */
@ExtendWith(MockitoExtension.class)
class DriverServiceTest {

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @InjectMocks
    private DriverService driverService;

    private Driver activeDriver;
    private Driver expiredLicenseDriver;
    private Driver inactiveDriver;
    private Driver assignedDriver;
    private Vehicle availableVehicle;
    private Vehicle assignedVehicle;
    private Vehicle maintenanceVehicle;
    private Vehicle chargingVehicle;

    private static final Long COMPANY_ID = 1L;
    private static final Long OTHER_COMPANY_ID = 2L;

    @BeforeEach
    void setUp() {
        // Active driver with valid license
        activeDriver = Driver.builder()
                .id(1L)
                .companyId(COMPANY_ID)
                .name("John Active")
                .phone("1234567890")
                .email("john@test.com")
                .licenseNumber("DL-12345")
                .licenseExpiry(LocalDate.now().plusYears(1))
                .status(Driver.DriverStatus.ACTIVE)
                .currentVehicleId(null)
                .totalTrips(0)
                .totalDistance(0.0)
                .safetyScore(100.0)
                .build();

        // Driver with expired license
        expiredLicenseDriver = Driver.builder()
                .id(2L)
                .companyId(COMPANY_ID)
                .name("Jane Expired")
                .phone("0987654321")
                .email("jane@test.com")
                .licenseNumber("DL-54321")
                .licenseExpiry(LocalDate.now().minusDays(1)) // Expired yesterday
                .status(Driver.DriverStatus.ACTIVE)
                .currentVehicleId(null)
                .build();

        // Inactive driver
        inactiveDriver = Driver.builder()
                .id(3L)
                .companyId(COMPANY_ID)
                .name("Bob Inactive")
                .phone("1112223333")
                .email("bob@test.com")
                .licenseNumber("DL-11111")
                .licenseExpiry(LocalDate.now().plusYears(1))
                .status(Driver.DriverStatus.INACTIVE)
                .currentVehicleId(null)
                .build();

        // Already assigned driver
        assignedDriver = Driver.builder()
                .id(4L)
                .companyId(COMPANY_ID)
                .name("Charlie Assigned")
                .phone("4445556666")
                .email("charlie@test.com")
                .licenseNumber("DL-44444")
                .licenseExpiry(LocalDate.now().plusYears(1))
                .status(Driver.DriverStatus.ON_TRIP)
                .currentVehicleId(100L) // Already assigned to vehicle 100
                .build();

        // Available vehicle
        availableVehicle = Vehicle.builder()
                .id(10L)
                .companyId(COMPANY_ID)
                .licensePlate("KA-01-EV-0001")
                .status(Vehicle.VehicleStatus.ACTIVE)
                .currentDriverId(null)
                .make("Tesla")
                .model("Model 3")
                .build();

        // Already assigned vehicle
        assignedVehicle = Vehicle.builder()
                .id(20L)
                .companyId(COMPANY_ID)
                .licensePlate("KA-01-EV-0002")
                .status(Vehicle.VehicleStatus.IN_TRIP)
                .currentDriverId(99L) // Already assigned to driver 99
                .make("Tesla")
                .model("Model Y")
                .build();

        // Vehicle in maintenance
        maintenanceVehicle = Vehicle.builder()
                .id(30L)
                .companyId(COMPANY_ID)
                .licensePlate("KA-01-EV-0003")
                .status(Vehicle.VehicleStatus.MAINTENANCE)
                .currentDriverId(null)
                .make("Tata")
                .model("Nexon EV")
                .build();

        // Vehicle charging
        chargingVehicle = Vehicle.builder()
                .id(40L)
                .companyId(COMPANY_ID)
                .licensePlate("KA-01-EV-0004")
                .status(Vehicle.VehicleStatus.CHARGING)
                .currentDriverId(null)
                .make("MG")
                .model("ZS EV")
                .build();
    }

    @Nested
    @DisplayName("Vehicle Assignment Tests")
    class AssignVehicleTests {

        @Test
        @DisplayName("Should successfully assign vehicle to active driver with valid license")
        void assignVehicle_Success() {
            // Arrange
            when(driverRepository.findById(1L)).thenReturn(Optional.of(activeDriver));
            when(vehicleRepository.findById(10L)).thenReturn(Optional.of(availableVehicle));
            when(driverRepository.save(any(Driver.class))).thenAnswer(inv -> inv.getArgument(0));
            when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            DriverResponse response = driverService.assignVehicle(1L, 10L);

            // Assert
            assertNotNull(response);
            verify(driverRepository).save(any(Driver.class));
            verify(vehicleRepository).save(any(Vehicle.class));
        }

        @Test
        @DisplayName("Should reject assignment when driver license is expired")
        void assignVehicle_ExpiredLicense_ThrowsException() {
            // Arrange
            when(driverRepository.findById(2L)).thenReturn(Optional.of(expiredLicenseDriver));
            when(vehicleRepository.findById(10L)).thenReturn(Optional.of(availableVehicle));

            // Act & Assert
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> driverService.assignVehicle(2L, 10L)
            );

            assertTrue(exception.getMessage().contains("expired license"));
            assertTrue(exception.getMessage().contains("Jane Expired"));
            verify(driverRepository, never()).save(any());
            verify(vehicleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject assignment when driver status is INACTIVE")
        void assignVehicle_InactiveDriver_ThrowsException() {
            // Arrange
            when(driverRepository.findById(3L)).thenReturn(Optional.of(inactiveDriver));
            when(vehicleRepository.findById(10L)).thenReturn(Optional.of(availableVehicle));

            // Act & Assert
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> driverService.assignVehicle(3L, 10L)
            );

            assertTrue(exception.getMessage().contains("not active"));
            assertTrue(exception.getMessage().contains("INACTIVE"));
            verify(driverRepository, never()).save(any());
            verify(vehicleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject assignment when driver is already assigned to another vehicle")
        void assignVehicle_DriverAlreadyAssigned_ThrowsException() {
            // Arrange
            when(driverRepository.findById(4L)).thenReturn(Optional.of(assignedDriver));
            when(vehicleRepository.findById(10L)).thenReturn(Optional.of(availableVehicle));

            // Act & Assert
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> driverService.assignVehicle(4L, 10L)
            );

            assertTrue(exception.getMessage().contains("already assigned"));
            assertTrue(exception.getMessage().contains("Charlie Assigned"));
            verify(driverRepository, never()).save(any());
            verify(vehicleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject assignment when vehicle is already assigned to another driver")
        void assignVehicle_VehicleAlreadyAssigned_ThrowsException() {
            // Arrange
            when(driverRepository.findById(1L)).thenReturn(Optional.of(activeDriver));
            when(vehicleRepository.findById(20L)).thenReturn(Optional.of(assignedVehicle));

            // Act & Assert
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> driverService.assignVehicle(1L, 20L)
            );

            assertTrue(exception.getMessage().contains("already assigned"));
            assertTrue(exception.getMessage().contains("KA-01-EV-0002"));
            verify(driverRepository, never()).save(any());
            verify(vehicleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject assignment when vehicle is in MAINTENANCE status")
        void assignVehicle_VehicleInMaintenance_ThrowsException() {
            // Arrange
            when(driverRepository.findById(1L)).thenReturn(Optional.of(activeDriver));
            when(vehicleRepository.findById(30L)).thenReturn(Optional.of(maintenanceVehicle));

            // Act & Assert
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> driverService.assignVehicle(1L, 30L)
            );

            assertTrue(exception.getMessage().contains("MAINTENANCE"));
            assertTrue(exception.getMessage().contains("cannot be assigned"));
            verify(driverRepository, never()).save(any());
            verify(vehicleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject assignment when vehicle is in CHARGING status")
        void assignVehicle_VehicleCharging_ThrowsException() {
            // Arrange
            when(driverRepository.findById(1L)).thenReturn(Optional.of(activeDriver));
            when(vehicleRepository.findById(40L)).thenReturn(Optional.of(chargingVehicle));

            // Act & Assert
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> driverService.assignVehicle(1L, 40L)
            );

            assertTrue(exception.getMessage().contains("CHARGING"));
            assertTrue(exception.getMessage().contains("cannot be assigned"));
            verify(driverRepository, never()).save(any());
            verify(vehicleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject cross-company assignment - security violation")
        void assignVehicle_CrossCompany_ThrowsSecurityException() {
            // Arrange
            Vehicle otherCompanyVehicle = Vehicle.builder()
                    .id(50L)
                    .companyId(OTHER_COMPANY_ID) // Different company!
                    .licensePlate("MH-01-EV-0001")
                    .status(Vehicle.VehicleStatus.ACTIVE)
                    .currentDriverId(null)
                    .make("BYD")
                    .model("Atto 3")
                    .build();

            when(driverRepository.findById(1L)).thenReturn(Optional.of(activeDriver));
            when(vehicleRepository.findById(50L)).thenReturn(Optional.of(otherCompanyVehicle));

            // Act & Assert
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> driverService.assignVehicle(1L, 50L)
            );

            assertTrue(exception.getMessage().contains("Security violation"));
            assertTrue(exception.getMessage().contains("same company"));
            verify(driverRepository, never()).save(any());
            verify(vehicleRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when driver not found")
        void assignVehicle_DriverNotFound_ThrowsException() {
            // Arrange
            when(driverRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                ResourceNotFoundException.class,
                () -> driverService.assignVehicle(999L, 10L)
            );

            verify(vehicleRepository, never()).findById(any());
            verify(driverRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when vehicle not found")
        void assignVehicle_VehicleNotFound_ThrowsException() {
            // Arrange
            when(driverRepository.findById(1L)).thenReturn(Optional.of(activeDriver));
            when(vehicleRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                ResourceNotFoundException.class,
                () -> driverService.assignVehicle(1L, 999L)
            );

            verify(driverRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should handle driver with license expiring within 7 days - allow with warning")
        void assignVehicle_LicenseExpiringSoon_AllowsWithWarning() {
            // Arrange
            Driver expiringLicenseDriver = Driver.builder()
                    .id(5L)
                    .companyId(COMPANY_ID)
                    .name("Soon Expiring")
                    .phone("5556667777")
                    .email("soon@test.com")
                    .licenseNumber("DL-55555")
                    .licenseExpiry(LocalDate.now().plusDays(3)) // Expires in 3 days
                    .status(Driver.DriverStatus.ACTIVE)
                    .currentVehicleId(null)
                    .build();

            when(driverRepository.findById(5L)).thenReturn(Optional.of(expiringLicenseDriver));
            when(vehicleRepository.findById(10L)).thenReturn(Optional.of(availableVehicle));
            when(driverRepository.save(any(Driver.class))).thenAnswer(inv -> inv.getArgument(0));
            when(vehicleRepository.save(any(Vehicle.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act - Should not throw, but log a warning
            DriverResponse response = driverService.assignVehicle(5L, 10L);

            // Assert
            assertNotNull(response);
            verify(driverRepository).save(any(Driver.class));
            verify(vehicleRepository).save(any(Vehicle.class));
        }

        @Test
        @DisplayName("Should handle driver with ON_LEAVE status - reject assignment")
        void assignVehicle_DriverOnLeave_ThrowsException() {
            // Arrange
            Driver onLeaveDriver = Driver.builder()
                    .id(6L)
                    .companyId(COMPANY_ID)
                    .name("On Leave")
                    .phone("6667778888")
                    .email("leave@test.com")
                    .licenseNumber("DL-66666")
                    .licenseExpiry(LocalDate.now().plusYears(1))
                    .status(Driver.DriverStatus.ON_LEAVE)
                    .currentVehicleId(null)
                    .build();

            when(driverRepository.findById(6L)).thenReturn(Optional.of(onLeaveDriver));
            when(vehicleRepository.findById(10L)).thenReturn(Optional.of(availableVehicle));

            // Act & Assert
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> driverService.assignVehicle(6L, 10L)
            );

            assertTrue(exception.getMessage().contains("not active"));
            assertTrue(exception.getMessage().contains("ON_LEAVE"));
        }

        @Test
        @DisplayName("Should handle driver with ON_TRIP status - reject assignment")
        void assignVehicle_DriverOnTrip_ThrowsException() {
            // Arrange
            Driver onTripDriver = Driver.builder()
                    .id(7L)
                    .companyId(COMPANY_ID)
                    .name("On Trip")
                    .phone("7778889999")
                    .email("trip@test.com")
                    .licenseNumber("DL-77777")
                    .licenseExpiry(LocalDate.now().plusYears(1))
                    .status(Driver.DriverStatus.ON_TRIP)
                    .currentVehicleId(null) // Edge case: ON_TRIP but no vehicle
                    .build();

            when(driverRepository.findById(7L)).thenReturn(Optional.of(onTripDriver));
            when(vehicleRepository.findById(10L)).thenReturn(Optional.of(availableVehicle));

            // Act & Assert
            IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> driverService.assignVehicle(7L, 10L)
            );

            assertTrue(exception.getMessage().contains("not active"));
            assertTrue(exception.getMessage().contains("ON_TRIP"));
        }
    }

    @Nested
    @DisplayName("Unassign Vehicle Tests")
    class UnassignVehicleTests {

        @Test
        @DisplayName("Should successfully unassign vehicle from driver")
        void unassignVehicle_Success() {
            // Arrange
            when(driverRepository.findById(4L)).thenReturn(Optional.of(assignedDriver));
            when(vehicleRepository.findById(100L)).thenReturn(Optional.of(assignedVehicle));
            when(driverRepository.save(any(Driver.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            DriverResponse response = driverService.unassignVehicle(4L);

            // Assert
            assertNotNull(response);
            verify(driverRepository).save(any(Driver.class));
            verify(vehicleRepository).save(any(Vehicle.class));
        }

        @Test
        @DisplayName("Should handle unassign when driver has no vehicle assigned")
        void unassignVehicle_NoVehicleAssigned_Success() {
            // Arrange
            when(driverRepository.findById(1L)).thenReturn(Optional.of(activeDriver));
            when(driverRepository.save(any(Driver.class))).thenAnswer(inv -> inv.getArgument(0));

            // Act
            DriverResponse response = driverService.unassignVehicle(1L);

            // Assert
            assertNotNull(response);
            verify(driverRepository).save(any(Driver.class));
            verify(vehicleRepository, never()).findById(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when driver not found")
        void unassignVehicle_DriverNotFound_ThrowsException() {
            // Arrange
            when(driverRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                ResourceNotFoundException.class,
                () -> driverService.unassignVehicle(999L)
            );

            verify(driverRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Create Driver Tests")
    class CreateDriverTests {

        @Test
        @DisplayName("Should reject driver creation with expired license")
        void createDriver_ExpiredLicense_ThrowsException() {
            // Arrange
            DriverRequest request = DriverRequest.builder()
                    .name("New Driver")
                    .phone("9998887777")
                    .email("new@test.com")
                    .licenseNumber("DL-NEW-123")
                    .licenseExpiry(LocalDate.now().minusDays(1))
                    .build();

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> driverService.createDriver(COMPANY_ID, request)
            );

            assertTrue(exception.getMessage().contains("past"));
            verify(driverRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should reject driver creation with invalid license number format")
        void createDriver_InvalidLicenseFormat_ThrowsException() {
            // Arrange - license number too short
            DriverRequest request = DriverRequest.builder()
                    .name("New Driver")
                    .phone("9998887777")
                    .email("new@test.com")
                    .licenseNumber("AB")  // Too short (min 5 chars)
                    .licenseExpiry(LocalDate.now().plusYears(1))
                    .build();

            // Act & Assert
            IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> driverService.createDriver(COMPANY_ID, request)
            );

            assertTrue(exception.getMessage().contains("Invalid license number"));
            verify(driverRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should successfully create driver with valid data")
        void createDriver_ValidData_Success() {
            // Arrange
            DriverRequest request = DriverRequest.builder()
                    .name("New Driver")
                    .phone("9998887777")
                    .email("new@test.com")
                    .licenseNumber("DL-VALID-123")
                    .licenseExpiry(LocalDate.now().plusYears(1))
                    .build();

            when(driverRepository.save(any(Driver.class))).thenAnswer(inv -> {
                Driver d = inv.getArgument(0);
                d.setId(100L);
                return d;
            });

            // Act
            DriverResponse response = driverService.createDriver(COMPANY_ID, request);

            // Assert
            assertNotNull(response);
            verify(driverRepository).save(any(Driver.class));
        }
    }

    @Nested
    @DisplayName("Delete Driver Tests")
    class DeleteDriverTests {

        @Test
        @DisplayName("Should delete driver successfully")
        void deleteDriver_Success() {
            // Arrange
            when(driverRepository.existsById(1L)).thenReturn(true);
            doNothing().when(driverRepository).deleteById(1L);

            // Act
            driverService.deleteDriver(1L);

            // Assert
            verify(driverRepository).deleteById(1L);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when driver not found")
        void deleteDriver_NotFound_ThrowsException() {
            // Arrange
            when(driverRepository.existsById(999L)).thenReturn(false);

            // Act & Assert
            assertThrows(
                ResourceNotFoundException.class,
                () -> driverService.deleteDriver(999L)
            );

            verify(driverRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("Get Drivers with Expiring Licenses Tests")
    class ExpiringLicensesTests {

        @Test
        @DisplayName("Should return drivers with licenses expiring within specified days")
        void getDriversWithExpiringLicenses_Success() {
            // Arrange
            when(driverRepository.findByCompanyIdAndLicenseExpiryBefore(eq(COMPANY_ID), any(LocalDate.class)))
                    .thenReturn(java.util.List.of(expiredLicenseDriver));

            // Act
            var result = driverService.getDriversWithExpiringLicenses(COMPANY_ID, 30);

            // Assert
            assertNotNull(result);
            assertFalse(result.isEmpty());
            verify(driverRepository).findByCompanyIdAndLicenseExpiryBefore(eq(COMPANY_ID), any(LocalDate.class));
        }
    }
}
