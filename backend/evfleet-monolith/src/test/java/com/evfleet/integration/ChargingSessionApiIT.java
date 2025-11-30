package com.evfleet.integration;

import com.evfleet.charging.model.ChargingSession;
import com.evfleet.charging.model.ChargingStation;
import com.evfleet.charging.repository.ChargingSessionRepository;
import com.evfleet.charging.repository.ChargingStationRepository;
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

import java.math.BigDecimal;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for Charging Session API
 * PR #44: Integration Tests
 *
 * Tests the complete charging workflow from session start to completion
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("Charging Session API Integration Tests")
class ChargingSessionApiIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ChargingSessionRepository sessionRepository;

    @Autowired
    private ChargingStationRepository stationRepository;

    @Autowired
    private VehicleRepository vehicleRepository;

    private Vehicle testVehicle;
    private ChargingStation testStation;

    @BeforeEach
    void setUp() {
        sessionRepository.deleteAll();
        
        // Create test vehicle
        testVehicle = vehicleRepository.save(Vehicle.builder()
                .vehicleNumber("CHARGING-TEST-001")
                .make("Tesla")
                .model("Model 3")
                .year(2023)
                .fuelType(FuelType.EV)
                .type(Vehicle.VehicleType.LCV)
                .batteryCapacity(75.0)
                .currentBatterySoc(30.0)
                .defaultChargerType("CCS2")
                .companyId(1L)
                .status(Vehicle.VehicleStatus.AVAILABLE)
                .build());

        // Create test station
        testStation = stationRepository.save(ChargingStation.builder()
                .name("Test Station 1")
                .address("123 Test Street")
                .latitude(40.7128)
                .longitude(-74.0060)
                .companyId(1L)
                .totalSlots(4)
                .availableSlots(3)
                .chargerType("CCS2")
                .powerOutput(150.0)
                .pricePerKwh(BigDecimal.valueOf(0.35))
                .status(ChargingStation.StationStatus.ACTIVE)
                .build());
    }

    @Nested
    @DisplayName("POST /api/v1/charging/sessions/start")
    class StartChargingSession {

        @Test
        @DisplayName("Should start charging session successfully")
        void shouldStartSessionSuccessfully() throws Exception {
            mockMvc.perform(post("/api/v1/charging/sessions/start")
                    .param("vehicleId", String.valueOf(testVehicle.getId()))
                    .param("stationId", String.valueOf(testStation.getId()))
                    .param("companyId", "1")
                    .param("initialSoc", "30.0")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.vehicleId").value(testVehicle.getId()))
                    .andExpect(jsonPath("$.data.stationId").value(testStation.getId()))
                    .andExpect(jsonPath("$.data.status").value("ACTIVE"));
        }

        @Test
        @DisplayName("Should decrement available slots when session starts")
        void shouldDecrementAvailableSlots() throws Exception {
            int initialSlots = testStation.getAvailableSlots();

            mockMvc.perform(post("/api/v1/charging/sessions/start")
                    .param("vehicleId", String.valueOf(testVehicle.getId()))
                    .param("stationId", String.valueOf(testStation.getId()))
                    .param("companyId", "1")
                    .param("initialSoc", "30.0")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());

            // Verify slots decremented
            ChargingStation updatedStation = stationRepository.findById(testStation.getId()).orElseThrow();
            org.assertj.core.api.Assertions.assertThat(updatedStation.getAvailableSlots())
                    .isEqualTo(initialSlots - 1);
        }

        @Test
        @DisplayName("Should reject session when vehicle already charging")
        void shouldRejectWhenVehicleAlreadyCharging() throws Exception {
            // Start first session
            mockMvc.perform(post("/api/v1/charging/sessions/start")
                    .param("vehicleId", String.valueOf(testVehicle.getId()))
                    .param("stationId", String.valueOf(testStation.getId()))
                    .param("companyId", "1")
                    .param("initialSoc", "30.0")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated());

            // Try to start second session
            mockMvc.perform(post("/api/v1/charging/sessions/start")
                    .param("vehicleId", String.valueOf(testVehicle.getId()))
                    .param("stationId", String.valueOf(testStation.getId()))
                    .param("companyId", "1")
                    .param("initialSoc", "30.0")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.success").value(false));
        }

        @Test
        @DisplayName("Should reject session when station is full")
        void shouldRejectWhenStationIsFull() throws Exception {
            testStation.setAvailableSlots(0);
            stationRepository.save(testStation);

            mockMvc.perform(post("/api/v1/charging/sessions/start")
                    .param("vehicleId", String.valueOf(testVehicle.getId()))
                    .param("stationId", String.valueOf(testStation.getId()))
                    .param("companyId", "1")
                    .param("initialSoc", "30.0")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    @DisplayName("POST /api/v1/charging/sessions/{id}/complete")
    class CompleteChargingSession {

        @Test
        @DisplayName("Should complete session successfully")
        void shouldCompleteSessionSuccessfully() throws Exception {
            // Start session first
            String startResponse = mockMvc.perform(post("/api/v1/charging/sessions/start")
                    .param("vehicleId", String.valueOf(testVehicle.getId()))
                    .param("stationId", String.valueOf(testStation.getId()))
                    .param("companyId", "1")
                    .param("initialSoc", "30.0")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            // Extract session ID from response
            Long sessionId = objectMapper.readTree(startResponse).path("data").path("id").asLong();

            // Complete session
            mockMvc.perform(post("/api/v1/charging/sessions/{id}/complete", sessionId)
                    .param("energyConsumed", "50.0")
                    .param("finalSoc", "95.0")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.status").value("COMPLETED"))
                    .andExpect(jsonPath("$.data.energyConsumed").value(50.0))
                    .andExpect(jsonPath("$.data.finalSoc").value(95.0));
        }

        @Test
        @DisplayName("Should calculate cost correctly")
        void shouldCalculateCostCorrectly() throws Exception {
            // Start session
            String startResponse = mockMvc.perform(post("/api/v1/charging/sessions/start")
                    .param("vehicleId", String.valueOf(testVehicle.getId()))
                    .param("stationId", String.valueOf(testStation.getId()))
                    .param("companyId", "1")
                    .param("initialSoc", "30.0")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            Long sessionId = objectMapper.readTree(startResponse).path("data").path("id").asLong();

            // Complete session with 100 kWh at $0.35/kWh = $35.00
            mockMvc.perform(post("/api/v1/charging/sessions/{id}/complete", sessionId)
                    .param("energyConsumed", "100.0")
                    .param("finalSoc", "100.0")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.cost").value(35.00));
        }

        @Test
        @DisplayName("Should increment available slots when session completes")
        void shouldIncrementAvailableSlots() throws Exception {
            // Start session
            String startResponse = mockMvc.perform(post("/api/v1/charging/sessions/start")
                    .param("vehicleId", String.valueOf(testVehicle.getId()))
                    .param("stationId", String.valueOf(testStation.getId()))
                    .param("companyId", "1")
                    .param("initialSoc", "30.0")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            Long sessionId = objectMapper.readTree(startResponse).path("data").path("id").asLong();
            int slotsAfterStart = stationRepository.findById(testStation.getId()).orElseThrow().getAvailableSlots();

            // Complete session
            mockMvc.perform(post("/api/v1/charging/sessions/{id}/complete", sessionId)
                    .param("energyConsumed", "50.0")
                    .param("finalSoc", "95.0")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Verify slots incremented
            ChargingStation updatedStation = stationRepository.findById(testStation.getId()).orElseThrow();
            org.assertj.core.api.Assertions.assertThat(updatedStation.getAvailableSlots())
                    .isEqualTo(slotsAfterStart + 1);
        }
    }

    @Nested
    @DisplayName("GET /api/v1/charging/sessions")
    class GetChargingSessions {

        @Test
        @DisplayName("Should return sessions by vehicle")
        void shouldReturnSessionsByVehicle() throws Exception {
            // Start and complete a session
            String startResponse = mockMvc.perform(post("/api/v1/charging/sessions/start")
                    .param("vehicleId", String.valueOf(testVehicle.getId()))
                    .param("stationId", String.valueOf(testStation.getId()))
                    .param("companyId", "1")
                    .param("initialSoc", "30.0")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isCreated())
                    .andReturn().getResponse().getContentAsString();

            Long sessionId = objectMapper.readTree(startResponse).path("data").path("id").asLong();

            mockMvc.perform(post("/api/v1/charging/sessions/{id}/complete", sessionId)
                    .param("energyConsumed", "50.0")
                    .param("finalSoc", "95.0")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());

            // Get sessions by vehicle
            mockMvc.perform(get("/api/v1/charging/sessions/vehicle/{vehicleId}", testVehicle.getId())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data", hasSize(greaterThanOrEqualTo(1))));
        }
    }
}
