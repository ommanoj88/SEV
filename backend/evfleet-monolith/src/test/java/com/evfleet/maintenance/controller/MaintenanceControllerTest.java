package com.evfleet.maintenance.controller;

import com.evfleet.maintenance.dto.MaintenanceRecordRequest;
import com.evfleet.maintenance.dto.MaintenanceRecordResponse;
import com.evfleet.maintenance.model.MaintenanceRecord;
import com.evfleet.maintenance.service.MaintenanceService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Unit tests for MaintenanceController
 * PR #41: Backend Controller Unit Tests
 *
 * Tests cover:
 * - POST /api/v1/maintenance (create record)
 * - GET /api/v1/maintenance (get all records)
 * - GET /api/v1/maintenance/{id} (get by ID)
 * - GET /api/v1/maintenance/vehicle/{vehicleId} (get by vehicle)
 * - PUT /api/v1/maintenance/{id} (update record)
 * - POST /api/v1/maintenance/{id}/complete (complete maintenance)
 * - DELETE /api/v1/maintenance/{id} (delete record)
 * - Error cases (404, 400)
 */
@WebMvcTest(MaintenanceController.class)
@DisplayName("MaintenanceController Tests")
class MaintenanceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MaintenanceService maintenanceService;

    private MaintenanceRecord testRecord;
    private MaintenanceRecordRequest testRequest;
    private MaintenanceRecordResponse testResponse;

    @BeforeEach
    void setUp() {
        testRecord = MaintenanceRecord.builder()
                .id(1L)
                .vehicleId(10L)
                .companyId(100L)
                .maintenanceType(MaintenanceRecord.MaintenanceType.PREVENTIVE)
                .description("Regular vehicle inspection")
                .status(MaintenanceRecord.MaintenanceStatus.SCHEDULED)
                .scheduledDate(LocalDate.now().plusDays(7))
                .estimatedCost(BigDecimal.valueOf(500.00))
                .build();

        testRequest = MaintenanceRecordRequest.builder()
                .vehicleId(10L)
                .maintenanceType(MaintenanceRecord.MaintenanceType.PREVENTIVE)
                .description("Regular vehicle inspection")
                .scheduledDate(LocalDate.now().plusDays(7))
                .estimatedCost(BigDecimal.valueOf(500.00))
                .build();

        testResponse = MaintenanceRecordResponse.from(testRecord);
    }

    @Nested
    @DisplayName("POST /api/v1/maintenance")
    class CreateMaintenanceRecord {

        @Test
        @DisplayName("Should create maintenance record successfully")
        void shouldCreateRecordSuccessfully() throws Exception {
            given(maintenanceService.createMaintenanceRecord(anyLong(), any(MaintenanceRecordRequest.class)))
                    .willReturn(testResponse);

            mockMvc.perform(post("/api/v1/maintenance")
                    .param("companyId", "100")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.vehicleId").value(10));
        }

        @Test
        @DisplayName("Should return 400 for missing companyId")
        void shouldReturn400ForMissingCompanyId() throws Exception {
            mockMvc.perform(post("/api/v1/maintenance")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/maintenance")
    class GetAllMaintenanceRecords {

        @Test
        @DisplayName("Should return all records for company")
        void shouldReturnAllRecordsForCompany() throws Exception {
            List<MaintenanceRecordResponse> records = Arrays.asList(testResponse);
            given(maintenanceService.getAllRecords(100L)).willReturn(records);

            mockMvc.perform(get("/api/v1/maintenance")
                    .param("companyId", "100")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(1)));
        }

        @Test
        @DisplayName("Should return empty list for company with no records")
        void shouldReturnEmptyListForCompanyWithNoRecords() throws Exception {
            given(maintenanceService.getAllRecords(999L)).willReturn(List.of());

            mockMvc.perform(get("/api/v1/maintenance")
                    .param("companyId", "999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/maintenance/{id}")
    class GetMaintenanceRecordById {

        @Test
        @DisplayName("Should return record when found")
        void shouldReturnRecordWhenFound() throws Exception {
            given(maintenanceService.getRecordById(1L)).willReturn(testResponse);

            mockMvc.perform(get("/api/v1/maintenance/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.description").value("Regular vehicle inspection"));
        }

        @Test
        @DisplayName("Should return 404 when record not found")
        void shouldReturn404WhenNotFound() throws Exception {
            given(maintenanceService.getRecordById(999L))
                    .willThrow(new jakarta.persistence.EntityNotFoundException("Maintenance record not found"));

            mockMvc.perform(get("/api/v1/maintenance/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/maintenance/vehicle/{vehicleId}")
    class GetRecordsByVehicle {

        @Test
        @DisplayName("Should return records for vehicle")
        void shouldReturnRecordsForVehicle() throws Exception {
            List<MaintenanceRecordResponse> records = Arrays.asList(testResponse);
            given(maintenanceService.getRecordsByVehicle(10L)).willReturn(records);

            mockMvc.perform(get("/api/v1/maintenance/vehicle/10")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].vehicleId").value(10));
        }

        @Test
        @DisplayName("Should return empty list for vehicle with no records")
        void shouldReturnEmptyListForVehicleWithNoRecords() throws Exception {
            given(maintenanceService.getRecordsByVehicle(999L)).willReturn(List.of());

            mockMvc.perform(get("/api/v1/maintenance/vehicle/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/maintenance/{id}")
    class UpdateMaintenanceRecord {

        @Test
        @DisplayName("Should update record successfully")
        void shouldUpdateRecordSuccessfully() throws Exception {
            MaintenanceRecordResponse updatedResponse = MaintenanceRecordResponse.builder()
                    .id(1L)
                    .vehicleId(10L)
                    .description("Updated inspection notes")
                    .status(MaintenanceRecord.MaintenanceStatus.IN_PROGRESS)
                    .build();

            given(maintenanceService.updateRecord(anyLong(), any(MaintenanceRecordRequest.class)))
                    .willReturn(updatedResponse);

            MaintenanceRecordRequest updateRequest = MaintenanceRecordRequest.builder()
                    .description("Updated inspection notes")
                    .build();

            mockMvc.perform(put("/api/v1/maintenance/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.description").value("Updated inspection notes"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/maintenance/{id}/complete")
    class CompleteMaintenanceRecord {

        @Test
        @DisplayName("Should complete maintenance record")
        void shouldCompleteRecordSuccessfully() throws Exception {
            MaintenanceRecordResponse completedResponse = MaintenanceRecordResponse.builder()
                    .id(1L)
                    .vehicleId(10L)
                    .status(MaintenanceRecord.MaintenanceStatus.COMPLETED)
                    .completedDate(LocalDate.now())
                    .actualCost(BigDecimal.valueOf(450.00))
                    .build();

            given(maintenanceService.completeRecord(anyLong(), any(BigDecimal.class), any()))
                    .willReturn(completedResponse);

            mockMvc.perform(post("/api/v1/maintenance/1/complete")
                    .param("actualCost", "450.00")
                    .param("notes", "Completed without issues")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("COMPLETED"));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/maintenance/{id}")
    class DeleteMaintenanceRecord {

        @Test
        @DisplayName("Should delete record successfully")
        void shouldDeleteRecordSuccessfully() throws Exception {
            doNothing().when(maintenanceService).deleteRecord(1L);

            mockMvc.perform(delete("/api/v1/maintenance/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Maintenance record deleted successfully"));

            verify(maintenanceService, times(1)).deleteRecord(1L);
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent record")
        void shouldReturn404WhenDeletingNonExistent() throws Exception {
            doThrow(new jakarta.persistence.EntityNotFoundException("Maintenance record not found"))
                    .when(maintenanceService).deleteRecord(999L);

            mockMvc.perform(delete("/api/v1/maintenance/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/maintenance/upcoming")
    class GetUpcomingMaintenance {

        @Test
        @DisplayName("Should return upcoming maintenance records")
        void shouldReturnUpcomingMaintenance() throws Exception {
            List<MaintenanceRecordResponse> upcomingRecords = Arrays.asList(testResponse);
            given(maintenanceService.getUpcomingMaintenance(100L, 30)).willReturn(upcomingRecords);

            mockMvc.perform(get("/api/v1/maintenance/upcoming")
                    .param("companyId", "100")
                    .param("daysAhead", "30")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/maintenance/overdue")
    class GetOverdueMaintenance {

        @Test
        @DisplayName("Should return overdue maintenance records")
        void shouldReturnOverdueMaintenance() throws Exception {
            MaintenanceRecordResponse overdueRecord = MaintenanceRecordResponse.builder()
                    .id(2L)
                    .vehicleId(10L)
                    .status(MaintenanceRecord.MaintenanceStatus.OVERDUE)
                    .scheduledDate(LocalDate.now().minusDays(10))
                    .build();

            List<MaintenanceRecordResponse> overdueRecords = Arrays.asList(overdueRecord);
            given(maintenanceService.getOverdueMaintenance(100L)).willReturn(overdueRecords);

            mockMvc.perform(get("/api/v1/maintenance/overdue")
                    .param("companyId", "100")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }
}
