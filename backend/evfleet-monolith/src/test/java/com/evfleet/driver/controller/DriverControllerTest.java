package com.evfleet.driver.controller;

import com.evfleet.driver.dto.DriverRequest;
import com.evfleet.driver.dto.DriverResponse;
import com.evfleet.driver.model.Driver;
import com.evfleet.driver.service.DriverService;
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

import java.time.LocalDate;
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
 * Unit tests for DriverController
 * PR #41: Backend Controller Unit Tests
 *
 * Tests cover:
 * - POST /api/v1/drivers (create driver)
 * - GET /api/v1/drivers (get all drivers)
 * - GET /api/v1/drivers/{id} (get by ID)
 * - GET /api/v1/drivers/active (get active drivers)
 * - GET /api/v1/drivers/available (get available drivers)
 * - PUT /api/v1/drivers/{id} (update driver)
 * - POST /api/v1/drivers/{id}/assign (assign vehicle)
 * - POST /api/v1/drivers/{id}/unassign (unassign vehicle)
 * - DELETE /api/v1/drivers/{id} (delete driver)
 * - Error cases (404, 400)
 */
@WebMvcTest(DriverController.class)
@DisplayName("DriverController Tests")
class DriverControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private DriverService driverService;

    private DriverRequest testDriverRequest;
    private DriverResponse testDriverResponse;

    @BeforeEach
    void setUp() {
        testDriverRequest = DriverRequest.builder()
                .firstName("Rajesh")
                .lastName("Kumar")
                .email("rajesh.kumar@test.com")
                .phone("+91-9876543210")
                .licenseNumber("DL-1234567890")
                .licenseExpiry(LocalDate.now().plusYears(2))
                .status(Driver.DriverStatus.ACTIVE)
                .build();

        testDriverResponse = DriverResponse.builder()
                .id(1L)
                .firstName("Rajesh")
                .lastName("Kumar")
                .email("rajesh.kumar@test.com")
                .phone("+91-9876543210")
                .licenseNumber("DL-1234567890")
                .licenseExpiry(LocalDate.now().plusYears(2))
                .status(Driver.DriverStatus.ACTIVE)
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/drivers")
    class CreateDriver {

        @Test
        @DisplayName("Should create driver successfully")
        void shouldCreateDriverSuccessfully() throws Exception {
            given(driverService.createDriver(anyLong(), any(DriverRequest.class)))
                    .willReturn(testDriverResponse);

            mockMvc.perform(post("/api/v1/drivers")
                    .param("companyId", "100")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testDriverRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.firstName").value("Rajesh"))
                    .andExpect(jsonPath("$.data.lastName").value("Kumar"));
        }

        @Test
        @DisplayName("Should return 400 for missing companyId")
        void shouldReturn400ForMissingCompanyId() throws Exception {
            mockMvc.perform(post("/api/v1/drivers")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testDriverRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/drivers")
    class GetAllDrivers {

        @Test
        @DisplayName("Should return all drivers for company")
        void shouldReturnAllDriversForCompany() throws Exception {
            List<DriverResponse> drivers = Arrays.asList(testDriverResponse);
            given(driverService.getAllDrivers(100L)).willReturn(drivers);

            mockMvc.perform(get("/api/v1/drivers")
                    .param("companyId", "100")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].firstName").value("Rajesh"));
        }

        @Test
        @DisplayName("Should return empty list for company with no drivers")
        void shouldReturnEmptyListForCompanyWithNoDrivers() throws Exception {
            given(driverService.getAllDrivers(999L)).willReturn(List.of());

            mockMvc.perform(get("/api/v1/drivers")
                    .param("companyId", "999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/drivers/{id}")
    class GetDriverById {

        @Test
        @DisplayName("Should return driver when found")
        void shouldReturnDriverWhenFound() throws Exception {
            given(driverService.getDriverById(1L)).willReturn(testDriverResponse);

            mockMvc.perform(get("/api/v1/drivers/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(1))
                    .andExpect(jsonPath("$.data.email").value("rajesh.kumar@test.com"));
        }

        @Test
        @DisplayName("Should return 404 when driver not found")
        void shouldReturn404WhenNotFound() throws Exception {
            given(driverService.getDriverById(999L))
                    .willThrow(new jakarta.persistence.EntityNotFoundException("Driver not found"));

            mockMvc.perform(get("/api/v1/drivers/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/drivers/active")
    class GetActiveDrivers {

        @Test
        @DisplayName("Should return active drivers")
        void shouldReturnActiveDrivers() throws Exception {
            List<DriverResponse> activeDrivers = Arrays.asList(testDriverResponse);
            given(driverService.getActiveDrivers(100L)).willReturn(activeDrivers);

            mockMvc.perform(get("/api/v1/drivers/active")
                    .param("companyId", "100")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data[0].status").value("ACTIVE"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/drivers/available")
    class GetAvailableDrivers {

        @Test
        @DisplayName("Should return available drivers")
        void shouldReturnAvailableDrivers() throws Exception {
            List<DriverResponse> availableDrivers = Arrays.asList(testDriverResponse);
            given(driverService.getAvailableDrivers(100L)).willReturn(availableDrivers);

            mockMvc.perform(get("/api/v1/drivers/available")
                    .param("companyId", "100")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/drivers/{id}")
    class UpdateDriver {

        @Test
        @DisplayName("Should update driver successfully")
        void shouldUpdateDriverSuccessfully() throws Exception {
            DriverResponse updatedResponse = DriverResponse.builder()
                    .id(1L)
                    .firstName("Rajesh")
                    .lastName("Kumar-Updated")
                    .email("rajesh.updated@test.com")
                    .phone("+91-9876543210")
                    .status(Driver.DriverStatus.ACTIVE)
                    .build();

            given(driverService.updateDriver(anyLong(), any(DriverRequest.class)))
                    .willReturn(updatedResponse);

            DriverRequest updateRequest = DriverRequest.builder()
                    .firstName("Rajesh")
                    .lastName("Kumar-Updated")
                    .email("rajesh.updated@test.com")
                    .build();

            mockMvc.perform(put("/api/v1/drivers/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.lastName").value("Kumar-Updated"))
                    .andExpect(jsonPath("$.data.email").value("rajesh.updated@test.com"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/drivers/{id}/assign")
    class AssignVehicle {

        @Test
        @DisplayName("Should assign vehicle to driver")
        void shouldAssignVehicleToDriver() throws Exception {
            DriverResponse assignedResponse = DriverResponse.builder()
                    .id(1L)
                    .firstName("Rajesh")
                    .lastName("Kumar")
                    .vehicleId(10L)
                    .status(Driver.DriverStatus.ACTIVE)
                    .build();

            given(driverService.assignVehicle(1L, 10L)).willReturn(assignedResponse);

            mockMvc.perform(post("/api/v1/drivers/1/assign")
                    .param("vehicleId", "10")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.vehicleId").value(10));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/drivers/{id}/unassign")
    class UnassignVehicle {

        @Test
        @DisplayName("Should unassign vehicle from driver")
        void shouldUnassignVehicleFromDriver() throws Exception {
            DriverResponse unassignedResponse = DriverResponse.builder()
                    .id(1L)
                    .firstName("Rajesh")
                    .lastName("Kumar")
                    .vehicleId(null)
                    .status(Driver.DriverStatus.ACTIVE)
                    .build();

            given(driverService.unassignVehicle(1L)).willReturn(unassignedResponse);

            mockMvc.perform(post("/api/v1/drivers/1/unassign")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.vehicleId").isEmpty());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/drivers/{id}")
    class DeleteDriver {

        @Test
        @DisplayName("Should delete driver successfully")
        void shouldDeleteDriverSuccessfully() throws Exception {
            doNothing().when(driverService).deleteDriver(1L);

            mockMvc.perform(delete("/api/v1/drivers/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Driver deleted successfully"));

            verify(driverService, times(1)).deleteDriver(1L);
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent driver")
        void shouldReturn404WhenDeletingNonExistent() throws Exception {
            doThrow(new jakarta.persistence.EntityNotFoundException("Driver not found"))
                    .when(driverService).deleteDriver(999L);

            mockMvc.perform(delete("/api/v1/drivers/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/drivers/leaderboard")
    class GetDriverLeaderboard {

        @Test
        @DisplayName("Should return driver leaderboard")
        void shouldReturnDriverLeaderboard() throws Exception {
            List<DriverResponse> leaderboard = Arrays.asList(
                    DriverResponse.builder().id(1L).firstName("Top").lastName("Driver").safetyScore(95.0).build(),
                    DriverResponse.builder().id(2L).firstName("Second").lastName("Driver").safetyScore(90.0).build()
            );
            given(driverService.getDriverLeaderboard(100L)).willReturn(leaderboard);

            mockMvc.perform(get("/api/v1/drivers/leaderboard")
                    .param("companyId", "100")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(2)));
        }
    }
}
