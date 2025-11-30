package com.evfleet.fleet.controller;

import com.evfleet.fleet.dto.VehicleRequest;
import com.evfleet.fleet.dto.VehicleResponse;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.service.VehicleService;
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
 * Unit tests for VehicleController
 * PR #41: Backend Controller Unit Tests
 *
 * Tests cover:
 * - GET /api/v1/vehicles (all vehicles, with company filter)
 * - POST /api/v1/vehicles (create vehicle)
 * - GET /api/v1/vehicles/{id} (get by ID)
 * - GET /api/v1/vehicles/company/{companyId} (get by company)
 * - PUT /api/v1/vehicles/{id} (update vehicle)
 * - PUT /api/v1/vehicles/{id}/location (update location)
 * - DELETE /api/v1/vehicles/{id} (delete vehicle)
 * - Error cases (404, 400)
 */
@WebMvcTest(VehicleController.class)
@DisplayName("VehicleController Tests")
class VehicleControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private VehicleService vehicleService;

    private Vehicle testVehicle;
    private VehicleRequest testVehicleRequest;
    private VehicleResponse testVehicleResponse;

    @BeforeEach
    void setUp() {
        testVehicle = Vehicle.builder()
                .id(1L)
                .companyId(100L)
                .vehicleNumber("EV-001")
                .type(Vehicle.VehicleType.CAR)
                .fuelType(Vehicle.FuelType.ELECTRIC)
                .make("Tesla")
                .model("Model 3")
                .year(2023)
                .batteryCapacity(75.0)
                .currentBatterySoc(85.0)
                .status(Vehicle.VehicleStatus.ACTIVE)
                .vin("1HGBH41JXMN109186")
                .licensePlate("MH-01-AB-1234")
                .color("White")
                .build();

        testVehicleRequest = VehicleRequest.builder()
                .companyId(100L)
                .vehicleNumber("EV-001")
                .type(Vehicle.VehicleType.CAR)
                .fuelType(Vehicle.FuelType.ELECTRIC)
                .make("Tesla")
                .model("Model 3")
                .year(2023)
                .batteryCapacity(75.0)
                .currentBatterySoc(85.0)
                .vin("1HGBH41JXMN109186")
                .licensePlate("MH-01-AB-1234")
                .color("White")
                .build();

        testVehicleResponse = VehicleResponse.from(testVehicle);
    }

    @Nested
    @DisplayName("GET /api/v1/vehicles")
    class GetAllVehicles {

        @Test
        @DisplayName("Should return empty list when no companyId provided")
        void shouldReturnEmptyListWhenNoCompanyId() throws Exception {
            mockMvc.perform(get("/api/v1/vehicles")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty());
        }

        @Test
        @DisplayName("Should return vehicles when companyId provided")
        void shouldReturnVehiclesForCompany() throws Exception {
            List<VehicleResponse> vehicles = Arrays.asList(testVehicleResponse);
            given(vehicleService.getVehiclesWithDriverNames(100L)).willReturn(vehicles);

            mockMvc.perform(get("/api/v1/vehicles")
                    .param("companyId", "100")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(1)))
                    .andExpect(jsonPath("$.data[0].vehicleNumber").value("EV-001"));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/vehicles")
    class CreateVehicle {

        @Test
        @DisplayName("Should create vehicle successfully")
        void shouldCreateVehicleSuccessfully() throws Exception {
            given(vehicleService.createVehicle(any(Vehicle.class))).willReturn(testVehicle);

            mockMvc.perform(post("/api/v1/vehicles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(testVehicleRequest)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.vehicleNumber").value("EV-001"))
                    .andExpect(jsonPath("$.make").value("Tesla"))
                    .andExpect(jsonPath("$.model").value("Model 3"));
        }

        @Test
        @DisplayName("Should return 400 for invalid request - missing required fields")
        void shouldReturn400ForInvalidRequest() throws Exception {
            VehicleRequest invalidRequest = VehicleRequest.builder().build();

            mockMvc.perform(post("/api/v1/vehicles")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(invalidRequest)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/vehicles/{id}")
    class GetVehicleById {

        @Test
        @DisplayName("Should return vehicle when found")
        void shouldReturnVehicleWhenFound() throws Exception {
            given(vehicleService.getVehicleById(1L)).willReturn(testVehicle);

            mockMvc.perform(get("/api/v1/vehicles/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(1))
                    .andExpect(jsonPath("$.vehicleNumber").value("EV-001"))
                    .andExpect(jsonPath("$.fuelType").value("ELECTRIC"));
        }

        @Test
        @DisplayName("Should return 404 when vehicle not found")
        void shouldReturn404WhenNotFound() throws Exception {
            given(vehicleService.getVehicleById(999L))
                    .willThrow(new jakarta.persistence.EntityNotFoundException("Vehicle not found"));

            mockMvc.perform(get("/api/v1/vehicles/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/vehicles/company/{companyId}")
    class GetVehiclesByCompany {

        @Test
        @DisplayName("Should return vehicles for company")
        void shouldReturnVehiclesForCompany() throws Exception {
            List<VehicleResponse> vehicles = Arrays.asList(testVehicleResponse);
            given(vehicleService.getVehiclesWithDriverNames(100L)).willReturn(vehicles);

            mockMvc.perform(get("/api/v1/vehicles/company/100")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$", hasSize(1)))
                    .andExpect(jsonPath("$[0].vehicleNumber").value("EV-001"));
        }

        @Test
        @DisplayName("Should return empty list for company with no vehicles")
        void shouldReturnEmptyListForCompanyWithNoVehicles() throws Exception {
            given(vehicleService.getVehiclesWithDriverNames(999L)).willReturn(List.of());

            mockMvc.perform(get("/api/v1/vehicles/company/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/vehicles/{id}")
    class UpdateVehicle {

        @Test
        @DisplayName("Should update vehicle successfully")
        void shouldUpdateVehicleSuccessfully() throws Exception {
            Vehicle updatedVehicle = Vehicle.builder()
                    .id(1L)
                    .companyId(100L)
                    .vehicleNumber("EV-001-UPDATED")
                    .type(Vehicle.VehicleType.CAR)
                    .fuelType(Vehicle.FuelType.ELECTRIC)
                    .make("Tesla")
                    .model("Model Y")
                    .year(2024)
                    .batteryCapacity(82.0)
                    .status(Vehicle.VehicleStatus.ACTIVE)
                    .build();

            given(vehicleService.updateVehicle(anyLong(), any(Vehicle.class))).willReturn(updatedVehicle);

            VehicleRequest updateRequest = VehicleRequest.builder()
                    .vehicleNumber("EV-001-UPDATED")
                    .type(Vehicle.VehicleType.CAR)
                    .fuelType(Vehicle.FuelType.ELECTRIC)
                    .make("Tesla")
                    .model("Model Y")
                    .year(2024)
                    .batteryCapacity(82.0)
                    .build();

            mockMvc.perform(put("/api/v1/vehicles/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(updateRequest)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.vehicleNumber").value("EV-001-UPDATED"))
                    .andExpect(jsonPath("$.model").value("Model Y"))
                    .andExpect(jsonPath("$.year").value(2024));
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/vehicles/{id}/location")
    class UpdateVehicleLocation {

        @Test
        @DisplayName("Should update vehicle location successfully")
        void shouldUpdateLocationSuccessfully() throws Exception {
            Vehicle vehicleWithLocation = Vehicle.builder()
                    .id(1L)
                    .vehicleNumber("EV-001")
                    .currentLatitude(19.0760)
                    .currentLongitude(72.8777)
                    .status(Vehicle.VehicleStatus.ACTIVE)
                    .build();

            given(vehicleService.updateVehicleLocation(1L, 19.0760, 72.8777))
                    .willReturn(vehicleWithLocation);

            mockMvc.perform(put("/api/v1/vehicles/1/location")
                    .param("latitude", "19.0760")
                    .param("longitude", "72.8777")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/vehicles/{id}")
    class DeleteVehicle {

        @Test
        @DisplayName("Should delete vehicle successfully")
        void shouldDeleteVehicleSuccessfully() throws Exception {
            doNothing().when(vehicleService).deleteVehicle(1L);

            mockMvc.perform(delete("/api/v1/vehicles/1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.message").value("Vehicle deleted successfully"));

            verify(vehicleService, times(1)).deleteVehicle(1L);
        }

        @Test
        @DisplayName("Should return 404 when deleting non-existent vehicle")
        void shouldReturn404WhenDeletingNonExistent() throws Exception {
            doThrow(new jakarta.persistence.EntityNotFoundException("Vehicle not found"))
                    .when(vehicleService).deleteVehicle(999L);

            mockMvc.perform(delete("/api/v1/vehicles/999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/vehicles/health")
    class HealthCheck {

        @Test
        @DisplayName("Should return health check response")
        void shouldReturnHealthCheck() throws Exception {
            mockMvc.perform(get("/api/v1/vehicles/health")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(content().string("Fleet Service is running"));
        }
    }
}
