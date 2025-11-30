package com.evfleet.maintenance.service;

import com.evfleet.common.exception.ResourceNotFoundException;
import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.VehicleRepository;
import com.evfleet.maintenance.dto.MaintenanceRecordRequest;
import com.evfleet.maintenance.dto.MaintenanceRecordResponse;
import com.evfleet.maintenance.model.MaintenanceRecord;
import com.evfleet.maintenance.repository.MaintenanceLineItemRepository;
import com.evfleet.maintenance.repository.MaintenancePolicyRepository;
import com.evfleet.maintenance.repository.MaintenanceRecordRepository;
import com.evfleet.analytics.service.AnalyticsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

/**
 * Unit tests for MaintenanceService
 * PR #42: Backend Service Unit Tests
 *
 * Tests cover:
 * - createMaintenanceRecord
 * - getAllMaintenanceRecords
 * - getMaintenanceRecordById
 * - getMaintenanceRecordsByVehicle
 * - getUpcomingMaintenance
 * - updateMaintenanceRecord
 * - deleteMaintenanceRecord
 * - completeMaintenanceRecord
 * - Fuel type validation for maintenance types
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("MaintenanceService Tests")
class MaintenanceServiceTest {

    @Mock
    private MaintenanceRecordRepository maintenanceRecordRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private AnalyticsService analyticsService;

    @Mock
    private MaintenancePolicyRepository maintenancePolicyRepository;

    @Mock
    private MaintenanceLineItemRepository maintenanceLineItemRepository;

    @InjectMocks
    private MaintenanceService maintenanceService;

    private MaintenanceRecord testRecord;
    private MaintenanceRecordRequest testRequest;
    private Vehicle testEVVehicle;
    private Vehicle testICEVehicle;

    @BeforeEach
    void setUp() {
        testEVVehicle = Vehicle.builder()
                .id(10L)
                .vehicleNumber("EV-001")
                .companyId(100L)
                .fuelType(FuelType.EV)
                .build();

        testICEVehicle = Vehicle.builder()
                .id(11L)
                .vehicleNumber("ICE-001")
                .companyId(100L)
                .fuelType(FuelType.ICE)
                .build();

        testRecord = MaintenanceRecord.builder()
                .id(1L)
                .vehicleId(10L)
                .companyId(100L)
                .type(MaintenanceRecord.MaintenanceType.TIRE_ROTATION)
                .scheduledDate(LocalDate.now().plusDays(7))
                .status(MaintenanceRecord.MaintenanceStatus.SCHEDULED)
                .description("Regular tire rotation")
                .cost(BigDecimal.valueOf(150.00))
                .build();

        testRequest = MaintenanceRecordRequest.builder()
                .vehicleId(10L)
                .type("TIRE_ROTATION")
                .scheduledDate(LocalDate.now().plusDays(7))
                .description("Regular tire rotation")
                .cost(BigDecimal.valueOf(150.00))
                .build();
    }

    @Nested
    @DisplayName("createMaintenanceRecord")
    class CreateMaintenanceRecord {

        @Test
        @DisplayName("Should create maintenance record successfully")
        void shouldCreateMaintenanceRecordSuccessfully() {
            given(vehicleRepository.findById(10L)).willReturn(Optional.of(testEVVehicle));
            given(maintenanceRecordRepository.save(any(MaintenanceRecord.class))).willReturn(testRecord);

            MaintenanceRecordResponse result = maintenanceService.createMaintenanceRecord(100L, testRequest);

            assertThat(result).isNotNull();
            assertThat(result.getVehicleId()).isEqualTo(10L);
            verify(maintenanceRecordRepository).save(any(MaintenanceRecord.class));
        }

        @Test
        @DisplayName("Should throw exception when vehicle not found")
        void shouldThrowExceptionWhenVehicleNotFound() {
            given(vehicleRepository.findById(999L)).willReturn(Optional.empty());
            testRequest.setVehicleId(999L);

            assertThatThrownBy(() -> maintenanceService.createMaintenanceRecord(100L, testRequest))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(maintenanceRecordRepository, never()).save(any(MaintenanceRecord.class));
        }

        @Test
        @DisplayName("Should create record with default SCHEDULED status")
        void shouldCreateRecordWithDefaultScheduledStatus() {
            testRequest.setStatus(null);
            given(vehicleRepository.findById(10L)).willReturn(Optional.of(testEVVehicle));
            given(maintenanceRecordRepository.save(any(MaintenanceRecord.class))).willAnswer(invocation -> {
                MaintenanceRecord saved = invocation.getArgument(0);
                assertThat(saved.getStatus()).isEqualTo(MaintenanceRecord.MaintenanceStatus.SCHEDULED);
                return testRecord;
            });

            maintenanceService.createMaintenanceRecord(100L, testRequest);

            verify(maintenanceRecordRepository).save(argThat(record ->
                record.getStatus() == MaintenanceRecord.MaintenanceStatus.SCHEDULED
            ));
        }

        @Test
        @DisplayName("Should throw exception for EV-invalid maintenance type on ICE vehicle")
        void shouldThrowExceptionForEVOnlyMaintenanceOnICE() {
            testRequest.setType("BATTERY_REPLACEMENT");
            testRequest.setVehicleId(11L);
            given(vehicleRepository.findById(11L)).willReturn(Optional.of(testICEVehicle));

            assertThatThrownBy(() -> maintenanceService.createMaintenanceRecord(100L, testRequest))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(maintenanceRecordRepository, never()).save(any(MaintenanceRecord.class));
        }

        @Test
        @DisplayName("Should throw exception for ICE-only maintenance type on EV vehicle")
        void shouldThrowExceptionForICEOnlyMaintenanceOnEV() {
            testRequest.setType("OIL_CHANGE");
            given(vehicleRepository.findById(10L)).willReturn(Optional.of(testEVVehicle));

            assertThatThrownBy(() -> maintenanceService.createMaintenanceRecord(100L, testRequest))
                    .isInstanceOf(IllegalArgumentException.class);

            verify(maintenanceRecordRepository, never()).save(any(MaintenanceRecord.class));
        }
    }

    @Nested
    @DisplayName("getMaintenanceRecord operations")
    class GetMaintenanceRecordOperations {

        @Test
        @DisplayName("Should get all maintenance records for company")
        void shouldGetAllMaintenanceRecordsForCompany() {
            List<MaintenanceRecord> records = Arrays.asList(testRecord);
            given(maintenanceRecordRepository.findByCompanyId(100L)).willReturn(records);
            given(maintenanceLineItemRepository.findByMaintenanceRecordId(anyLong())).willReturn(List.of());

            List<MaintenanceRecordResponse> result = maintenanceService.getAllMaintenanceRecords(100L);

            assertThat(result).hasSize(1);
        }

        @Test
        @DisplayName("Should get maintenance record by ID")
        void shouldGetMaintenanceRecordById() {
            given(maintenanceRecordRepository.findById(1L)).willReturn(Optional.of(testRecord));
            given(maintenanceLineItemRepository.findByMaintenanceRecordId(1L)).willReturn(List.of());

            MaintenanceRecordResponse result = maintenanceService.getMaintenanceRecordById(1L);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("Should throw exception when record not found")
        void shouldThrowExceptionWhenRecordNotFound() {
            given(maintenanceRecordRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> maintenanceService.getMaintenanceRecordById(999L))
                    .isInstanceOf(ResourceNotFoundException.class);
        }

        @Test
        @DisplayName("Should get maintenance records by vehicle")
        void shouldGetMaintenanceRecordsByVehicle() {
            List<MaintenanceRecord> records = Arrays.asList(testRecord);
            given(maintenanceRecordRepository.findByVehicleId(10L)).willReturn(records);
            given(maintenanceLineItemRepository.findByMaintenanceRecordId(anyLong())).willReturn(List.of());

            List<MaintenanceRecordResponse> result = maintenanceService.getMaintenanceRecordsByVehicle(10L);

            assertThat(result).hasSize(1);
            assertThat(result.get(0).getVehicleId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("Should get upcoming maintenance")
        void shouldGetUpcomingMaintenance() {
            List<MaintenanceRecord> records = Arrays.asList(testRecord);
            given(maintenanceRecordRepository.findByCompanyIdAndStatusAndScheduledDateAfter(
                    eq(100L), eq(MaintenanceRecord.MaintenanceStatus.SCHEDULED), any(LocalDate.class)))
                    .willReturn(records);

            List<MaintenanceRecordResponse> result = maintenanceService.getUpcomingMaintenance(100L);

            assertThat(result).hasSize(1);
        }
    }

    @Nested
    @DisplayName("updateMaintenanceRecord")
    class UpdateMaintenanceRecord {

        @Test
        @DisplayName("Should update maintenance record successfully")
        void shouldUpdateMaintenanceRecordSuccessfully() {
            MaintenanceRecordRequest updateRequest = MaintenanceRecordRequest.builder()
                    .description("Updated description")
                    .cost(BigDecimal.valueOf(200.00))
                    .build();

            given(maintenanceRecordRepository.findById(1L)).willReturn(Optional.of(testRecord));
            given(maintenanceRecordRepository.save(any(MaintenanceRecord.class))).willAnswer(i -> i.getArgument(0));

            MaintenanceRecordResponse result = maintenanceService.updateMaintenanceRecord(1L, updateRequest);

            assertThat(result).isNotNull();
            verify(maintenanceRecordRepository).save(any(MaintenanceRecord.class));
        }

        @Test
        @DisplayName("Should throw exception when updating non-existent record")
        void shouldThrowExceptionWhenUpdatingNonExistent() {
            given(maintenanceRecordRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> maintenanceService.updateMaintenanceRecord(999L, testRequest))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("deleteMaintenanceRecord")
    class DeleteMaintenanceRecord {

        @Test
        @DisplayName("Should delete maintenance record successfully")
        void shouldDeleteMaintenanceRecordSuccessfully() {
            given(maintenanceRecordRepository.findById(1L)).willReturn(Optional.of(testRecord));

            maintenanceService.deleteMaintenanceRecord(1L);

            verify(maintenanceRecordRepository).delete(testRecord);
        }

        @Test
        @DisplayName("Should throw exception when deleting non-existent record")
        void shouldThrowExceptionWhenDeletingNonExistent() {
            given(maintenanceRecordRepository.findById(999L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> maintenanceService.deleteMaintenanceRecord(999L))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(maintenanceRecordRepository, never()).delete(any(MaintenanceRecord.class));
        }

        @Test
        @DisplayName("Should throw exception when deleting in-progress record")
        void shouldThrowExceptionWhenDeletingInProgressRecord() {
            testRecord.setStatus(MaintenanceRecord.MaintenanceStatus.IN_PROGRESS);
            given(maintenanceRecordRepository.findById(1L)).willReturn(Optional.of(testRecord));

            assertThatThrownBy(() -> maintenanceService.deleteMaintenanceRecord(1L))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Cannot delete");

            verify(maintenanceRecordRepository, never()).delete(any(MaintenanceRecord.class));
        }
    }

    @Nested
    @DisplayName("completeMaintenanceRecord")
    class CompleteMaintenanceRecord {

        @Test
        @DisplayName("Should complete maintenance record successfully")
        void shouldCompleteMaintenanceRecordSuccessfully() {
            testRecord.setStatus(MaintenanceRecord.MaintenanceStatus.IN_PROGRESS);
            given(maintenanceRecordRepository.findById(1L)).willReturn(Optional.of(testRecord));
            given(maintenanceRecordRepository.save(any(MaintenanceRecord.class))).willAnswer(i -> i.getArgument(0));

            MaintenanceRecordResponse result = maintenanceService.completeMaintenanceRecord(
                    1L, BigDecimal.valueOf(175.00), "Completed successfully");

            assertThat(result.getStatus()).isEqualTo("COMPLETED");
            verify(maintenanceRecordRepository).save(argThat(record ->
                record.getStatus() == MaintenanceRecord.MaintenanceStatus.COMPLETED &&
                record.getCompletedDate() != null
            ));
        }

        @Test
        @DisplayName("Should throw exception when completing already completed record")
        void shouldThrowExceptionWhenCompletingAlreadyCompleted() {
            testRecord.setStatus(MaintenanceRecord.MaintenanceStatus.COMPLETED);
            given(maintenanceRecordRepository.findById(1L)).willReturn(Optional.of(testRecord));

            assertThatThrownBy(() -> maintenanceService.completeMaintenanceRecord(
                    1L, BigDecimal.valueOf(175.00), "Completed successfully"))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already completed");
        }

        @Test
        @DisplayName("Should throw exception when completing cancelled record")
        void shouldThrowExceptionWhenCompletingCancelled() {
            testRecord.setStatus(MaintenanceRecord.MaintenanceStatus.CANCELLED);
            given(maintenanceRecordRepository.findById(1L)).willReturn(Optional.of(testRecord));

            assertThatThrownBy(() -> maintenanceService.completeMaintenanceRecord(
                    1L, BigDecimal.valueOf(175.00), "Completed successfully"))
                    .isInstanceOf(IllegalStateException.class);
        }
    }

    @Nested
    @DisplayName("Maintenance Type Validation")
    class MaintenanceTypeValidation {

        @Test
        @DisplayName("Should allow tire rotation for all fuel types")
        void shouldAllowTireRotationForAllFuelTypes() {
            testRequest.setType("TIRE_ROTATION");
            
            // EV
            given(vehicleRepository.findById(10L)).willReturn(Optional.of(testEVVehicle));
            given(maintenanceRecordRepository.save(any(MaintenanceRecord.class))).willReturn(testRecord);

            assertThatCode(() -> maintenanceService.createMaintenanceRecord(100L, testRequest))
                    .doesNotThrowAnyException();

            // ICE
            testRequest.setVehicleId(11L);
            given(vehicleRepository.findById(11L)).willReturn(Optional.of(testICEVehicle));

            assertThatCode(() -> maintenanceService.createMaintenanceRecord(100L, testRequest))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should allow brake inspection for all fuel types")
        void shouldAllowBrakeInspectionForAllFuelTypes() {
            testRequest.setType("BRAKE_INSPECTION");
            testRecord.setType(MaintenanceRecord.MaintenanceType.BRAKE_INSPECTION);
            
            given(vehicleRepository.findById(10L)).willReturn(Optional.of(testEVVehicle));
            given(maintenanceRecordRepository.save(any(MaintenanceRecord.class))).willReturn(testRecord);

            assertThatCode(() -> maintenanceService.createMaintenanceRecord(100L, testRequest))
                    .doesNotThrowAnyException();
        }

        @Test
        @DisplayName("Should only allow battery check for EV and Hybrid")
        void shouldOnlyAllowBatteryCheckForEVAndHybrid() {
            testRequest.setType("BATTERY_CHECK");
            testRecord.setType(MaintenanceRecord.MaintenanceType.BATTERY_CHECK);

            // EV - should succeed
            given(vehicleRepository.findById(10L)).willReturn(Optional.of(testEVVehicle));
            given(maintenanceRecordRepository.save(any(MaintenanceRecord.class))).willReturn(testRecord);

            assertThatCode(() -> maintenanceService.createMaintenanceRecord(100L, testRequest))
                    .doesNotThrowAnyException();

            // ICE - should fail
            testRequest.setVehicleId(11L);
            given(vehicleRepository.findById(11L)).willReturn(Optional.of(testICEVehicle));

            assertThatThrownBy(() -> maintenanceService.createMaintenanceRecord(100L, testRequest))
                    .isInstanceOf(IllegalArgumentException.class);
        }
    }
}
