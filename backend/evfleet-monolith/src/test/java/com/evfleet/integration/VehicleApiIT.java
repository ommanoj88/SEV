package com.evfleet.integration;

import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.VehicleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Vehicle API
 * PR #44: Integration Tests
 *
 * Tests the full stack from Controller -> Service -> Repository -> Database
 * Uses @SpringBootTest with embedded H2 database
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Vehicle API Integration Tests")
class VehicleApiIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private VehicleRepository vehicleRepository;

    private Vehicle testVehicle;

    @BeforeEach
    void setUp() {
        vehicleRepository.deleteAll();
        
        testVehicle = Vehicle.builder()
                .vehicleNumber("TEST-EV-001")
                .make("Tesla")
                .model("Model 3")
                .year(2023)
                .fuelType(FuelType.EV)
                .batteryCapacity(75.0)
                .currentBatterySoc(85.0)
                .defaultChargerType("CCS2")
                .companyId(1L)
                .status(Vehicle.VehicleStatus.AVAILABLE)
                .build();
    }

    @Nested
    @DisplayName("POST /api/v1/vehicles")
    class CreateVehicle {

        @Test
        @DisplayName("Should create EV vehicle successfully")
        void shouldCreateEVVehicleSuccessfully() throws Exception {
            String requestBody = """
                {
                    "vehicleNumber": "NEW-EV-001",
                    "make": "Tesla",
                    "model": "Model Y",
                    "year": 2024,
                    "fuelType": "EV",
                    "batteryCapacity": 82.0,
                    "currentBatterySoc": 100.0,
                    "defaultChargerType": "CCS2",
                    "status": "AVAILABLE"
                }
                """;

            mockMvc.perform(post("/api/v1/vehicles")
                    .param("companyId", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.vehicleNumber").value("NEW-EV-001"))
                    .andExpect(jsonPath("$.data.fuelType").value("EV"));
        }

        @Test
        @DisplayName("Should create ICE vehicle successfully")
        void shouldCreateICEVehicleSuccessfully() throws Exception {
            String requestBody = """
                {
                    "vehicleNumber": "NEW-ICE-001",
                    "make": "Toyota",
                    "model": "Camry",
                    "year": 2023,
                    "fuelType": "ICE",
                    "fuelTankCapacity": 60.0,
                    "fuelLevel": 45.0,
                    "status": "AVAILABLE"
                }
                """;

            mockMvc.perform(post("/api/v1/vehicles")
                    .param("companyId", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.data.fuelType").value("ICE"))
                    .andExpect(jsonPath("$.data.fuelTankCapacity").value(60.0));
        }

        @Test
        @DisplayName("Should reject duplicate vehicle number")
        void shouldRejectDuplicateVehicleNumber() throws Exception {
            // Save first vehicle
            vehicleRepository.save(testVehicle);

            String requestBody = """
                {
                    "vehicleNumber": "TEST-EV-001",
                    "make": "Tesla",
                    "model": "Model S",
                    "year": 2024,
                    "fuelType": "EV",
                    "batteryCapacity": 100.0,
                    "defaultChargerType": "CCS2"
                }
                """;

            mockMvc.perform(post("/api/v1/vehicles")
                    .param("companyId", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should reject EV without battery capacity")
        void shouldRejectEVWithoutBatteryCapacity() throws Exception {
            String requestBody = """
                {
                    "vehicleNumber": "INVALID-EV-001",
                    "make": "Tesla",
                    "model": "Model 3",
                    "year": 2023,
                    "fuelType": "EV",
                    "defaultChargerType": "CCS2"
                }
                """;

            mockMvc.perform(post("/api/v1/vehicles")
                    .param("companyId", "1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/vehicles")
    class GetVehicles {

        @Test
        @DisplayName("Should return all vehicles for company")
        void shouldReturnAllVehiclesForCompany() throws Exception {
            vehicleRepository.save(testVehicle);

            mockMvc.perform(get("/api/v1/vehicles")
                    .param("companyId", "1")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
        }

        @Test
        @DisplayName("Should return empty list for company with no vehicles")
        void shouldReturnEmptyListForNewCompany() throws Exception {
            mockMvc.perform(get("/api/v1/vehicles")
                    .param("companyId", "99999")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data").isEmpty());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/vehicles/{id}")
    class GetVehicleById {

        @Test
        @DisplayName("Should return vehicle by ID")
        void shouldReturnVehicleById() throws Exception {
            Vehicle saved = vehicleRepository.save(testVehicle);

            mockMvc.perform(get("/api/v1/vehicles/{id}", saved.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.id").value(saved.getId()))
                    .andExpect(jsonPath("$.data.vehicleNumber").value("TEST-EV-001"));
        }

        @Test
        @DisplayName("Should return 404 for non-existent vehicle")
        void shouldReturn404ForNonExistentVehicle() throws Exception {
            mockMvc.perform(get("/api/v1/vehicles/{id}", 99999)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/vehicles/{id}")
    class UpdateVehicle {

        @Test
        @DisplayName("Should update vehicle successfully")
        void shouldUpdateVehicleSuccessfully() throws Exception {
            Vehicle saved = vehicleRepository.save(testVehicle);

            String requestBody = """
                {
                    "model": "Model Y Performance",
                    "year": 2024,
                    "currentBatterySoc": 95.0
                }
                """;

            mockMvc.perform(put("/api/v1/vehicles/{id}", saved.getId())
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(requestBody))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.model").value("Model Y Performance"))
                    .andExpect(jsonPath("$.data.year").value(2024));
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/vehicles/{id}")
    class DeleteVehicle {

        @Test
        @DisplayName("Should delete vehicle successfully")
        void shouldDeleteVehicleSuccessfully() throws Exception {
            Vehicle saved = vehicleRepository.save(testVehicle);

            mockMvc.perform(delete("/api/v1/vehicles/{id}", saved.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            // Verify deleted
            mockMvc.perform(get("/api/v1/vehicles/{id}", saved.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should reject deletion of vehicle in trip")
        void shouldRejectDeletionOfVehicleInTrip() throws Exception {
            testVehicle.setStatus(Vehicle.VehicleStatus.IN_TRIP);
            Vehicle saved = vehicleRepository.save(testVehicle);

            mockMvc.perform(delete("/api/v1/vehicles/{id}", saved.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("Battery SOC Update")
    class BatterySOCUpdate {

        @Test
        @DisplayName("Should update battery SOC successfully")
        void shouldUpdateBatterySOCSuccessfully() throws Exception {
            Vehicle saved = vehicleRepository.save(testVehicle);

            mockMvc.perform(patch("/api/v1/vehicles/{id}/battery-soc", saved.getId())
                    .param("soc", "92.5")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.currentBatterySoc").value(92.5));
        }

        @Test
        @DisplayName("Should reject invalid battery SOC")
        void shouldRejectInvalidBatterySOC() throws Exception {
            Vehicle saved = vehicleRepository.save(testVehicle);

            mockMvc.perform(patch("/api/v1/vehicles/{id}/battery-soc", saved.getId())
                    .param("soc", "150.0")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }
}
