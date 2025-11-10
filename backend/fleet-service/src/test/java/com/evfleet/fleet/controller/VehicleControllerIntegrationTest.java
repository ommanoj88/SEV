package com.evfleet.fleet.controller;

import com.evfleet.fleet.dto.AvailableFeaturesDTO;
import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.VehicleRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Integration tests for VehicleController available features endpoint
 * Tests the full stack from controller to service to ensure proper feature availability
 * 
 * @since 2.0.0 (PR 8: Feature Availability in APIs)
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class VehicleControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private VehicleRepository vehicleRepository;

    @Autowired
    private ObjectMapper objectMapper;

    private Vehicle evVehicle;
    private Vehicle iceVehicle;
    private Vehicle hybridVehicle;

    @BeforeEach
    void setUp() {
        vehicleRepository.deleteAll();

        // Create EV vehicle
        evVehicle = new Vehicle();
        evVehicle.setVehicleNumber("TEST-EV-001");
        evVehicle.setType(Vehicle.VehicleType.LCV);
        evVehicle.setFuelType(FuelType.EV);
        evVehicle.setCompanyId(1L);
        evVehicle.setMake("Tesla");
        evVehicle.setModel("Model 3");
        evVehicle.setYear(2023);
        evVehicle.setBatteryCapacity(75.0);
        evVehicle.setCurrentBatterySoc(80.0);
        evVehicle.setStatus(Vehicle.VehicleStatus.ACTIVE);
        evVehicle.setCreatedAt(LocalDateTime.now());
        evVehicle.setUpdatedAt(LocalDateTime.now());
        evVehicle = vehicleRepository.save(evVehicle);

        // Create ICE vehicle
        iceVehicle = new Vehicle();
        iceVehicle.setVehicleNumber("TEST-ICE-001");
        iceVehicle.setType(Vehicle.VehicleType.LCV);
        iceVehicle.setFuelType(FuelType.ICE);
        iceVehicle.setCompanyId(1L);
        iceVehicle.setMake("Toyota");
        iceVehicle.setModel("Camry");
        iceVehicle.setYear(2022);
        iceVehicle.setFuelTankCapacity(50.0);
        iceVehicle.setFuelLevel(70.0);
        iceVehicle.setStatus(Vehicle.VehicleStatus.ACTIVE);
        iceVehicle.setCreatedAt(LocalDateTime.now());
        iceVehicle.setUpdatedAt(LocalDateTime.now());
        iceVehicle = vehicleRepository.save(iceVehicle);

        // Create Hybrid vehicle
        hybridVehicle = new Vehicle();
        hybridVehicle.setVehicleNumber("TEST-HYB-001");
        hybridVehicle.setType(Vehicle.VehicleType.LCV);
        hybridVehicle.setFuelType(FuelType.HYBRID);
        hybridVehicle.setCompanyId(1L);
        hybridVehicle.setMake("Toyota");
        hybridVehicle.setModel("Prius");
        hybridVehicle.setYear(2023);
        hybridVehicle.setBatteryCapacity(50.0);
        hybridVehicle.setCurrentBatterySoc(60.0);
        hybridVehicle.setFuelTankCapacity(40.0);
        hybridVehicle.setFuelLevel(50.0);
        hybridVehicle.setStatus(Vehicle.VehicleStatus.ACTIVE);
        hybridVehicle.setCreatedAt(LocalDateTime.now());
        hybridVehicle.setUpdatedAt(LocalDateTime.now());
        hybridVehicle = vehicleRepository.save(hybridVehicle);
    }

    // ===== PR 8: Available Features Integration Tests =====

    @Test
    void testGetAvailableFeatures_ForEVVehicle_ShouldReturnEVFeatures() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(get("/api/v1/vehicles/{id}/available-features", evVehicle.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batteryTrackingAvailable").value(true))
                .andExpect(jsonPath("$.chargingManagementAvailable").value(true))
                .andExpect(jsonPath("$.energyAnalyticsAvailable").value(true))
                .andExpect(jsonPath("$.fuelConsumptionAvailable").value(false))
                .andExpect(jsonPath("$.fuelStationDiscoveryAvailable").value(false))
                .andExpect(jsonPath("$.engineDiagnosticsAvailable").value(false))
                .andExpect(jsonPath("$.features").isArray())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        AvailableFeaturesDTO features = objectMapper.readValue(responseBody, AvailableFeaturesDTO.class);

        assertNotNull(features);
        assertNotNull(features.getFeatures());
        assertEquals(5, features.getFeatures().size());
        assertTrue(features.getFeatures().contains("BATTERY_TRACKING"));
        assertTrue(features.getFeatures().contains("CHARGING_MANAGEMENT"));
        assertTrue(features.getFeatures().contains("ENERGY_ANALYTICS"));
        assertFalse(features.getFeatures().contains("FUEL_CONSUMPTION"));
        assertFalse(features.getFeatures().contains("ENGINE_DIAGNOSTICS"));
    }

    @Test
    void testGetAvailableFeatures_ForICEVehicle_ShouldReturnICEFeatures() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(get("/api/v1/vehicles/{id}/available-features", iceVehicle.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batteryTrackingAvailable").value(false))
                .andExpect(jsonPath("$.chargingManagementAvailable").value(false))
                .andExpect(jsonPath("$.energyAnalyticsAvailable").value(false))
                .andExpect(jsonPath("$.fuelConsumptionAvailable").value(true))
                .andExpect(jsonPath("$.fuelStationDiscoveryAvailable").value(true))
                .andExpect(jsonPath("$.engineDiagnosticsAvailable").value(true))
                .andExpect(jsonPath("$.features").isArray())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        AvailableFeaturesDTO features = objectMapper.readValue(responseBody, AvailableFeaturesDTO.class);

        assertNotNull(features);
        assertNotNull(features.getFeatures());
        assertEquals(4, features.getFeatures().size());
        assertTrue(features.getFeatures().contains("FUEL_CONSUMPTION"));
        assertTrue(features.getFeatures().contains("ENGINE_DIAGNOSTICS"));
        assertTrue(features.getFeatures().contains("FUEL_STATION_DISCOVERY"));
        assertFalse(features.getFeatures().contains("BATTERY_TRACKING"));
        assertFalse(features.getFeatures().contains("CHARGING_MANAGEMENT"));
    }

    @Test
    void testGetAvailableFeatures_ForHybridVehicle_ShouldReturnAllFeatures() throws Exception {
        // When & Then
        MvcResult result = mockMvc.perform(get("/api/v1/vehicles/{id}/available-features", hybridVehicle.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.batteryTrackingAvailable").value(true))
                .andExpect(jsonPath("$.chargingManagementAvailable").value(true))
                .andExpect(jsonPath("$.energyAnalyticsAvailable").value(true))
                .andExpect(jsonPath("$.fuelConsumptionAvailable").value(true))
                .andExpect(jsonPath("$.fuelStationDiscoveryAvailable").value(true))
                .andExpect(jsonPath("$.engineDiagnosticsAvailable").value(true))
                .andExpect(jsonPath("$.features").isArray())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        AvailableFeaturesDTO features = objectMapper.readValue(responseBody, AvailableFeaturesDTO.class);

        assertNotNull(features);
        assertNotNull(features.getFeatures());
        assertEquals(10, features.getFeatures().size());
        
        // Verify hybrid has features from both EV and ICE
        assertTrue(features.getFeatures().contains("BATTERY_TRACKING"));
        assertTrue(features.getFeatures().contains("CHARGING_MANAGEMENT"));
        assertTrue(features.getFeatures().contains("FUEL_CONSUMPTION"));
        assertTrue(features.getFeatures().contains("ENGINE_DIAGNOSTICS"));
        assertTrue(features.getFeatures().contains("HYBRID_MODE_OPTIMIZATION"));
    }

    @Test
    void testGetAvailableFeatures_WithNonExistentVehicle_ShouldReturn404() throws Exception {
        // When & Then
        Long nonExistentId = 999999L;
        mockMvc.perform(get("/api/v1/vehicles/{id}/available-features", nonExistentId))
                .andExpect(status().isNotFound());
    }

    @Test
    void testGetAvailableFeatures_ValidatesEndpointPath() throws Exception {
        // Test that the endpoint is properly registered
        mockMvc.perform(get("/api/v1/vehicles/{id}/available-features", evVehicle.getId()))
                .andExpect(status().isOk())
                .andExpect(content().contentType("application/json"));
    }

    @Test
    void testGetAvailableFeatures_ReturnsCorrectFeaturesForDifferentVehicleTypes() throws Exception {
        // Test EV features
        MvcResult evResult = mockMvc.perform(get("/api/v1/vehicles/{id}/available-features", evVehicle.getId()))
                .andExpect(status().isOk())
                .andReturn();
        
        AvailableFeaturesDTO evFeatures = objectMapper.readValue(
            evResult.getResponse().getContentAsString(), AvailableFeaturesDTO.class);

        // Test ICE features
        MvcResult iceResult = mockMvc.perform(get("/api/v1/vehicles/{id}/available-features", iceVehicle.getId()))
                .andExpect(status().isOk())
                .andReturn();
        
        AvailableFeaturesDTO iceFeatures = objectMapper.readValue(
            iceResult.getResponse().getContentAsString(), AvailableFeaturesDTO.class);

        // Verify they are different
        assertNotEquals(evFeatures.getFeatures().size(), iceFeatures.getFeatures().size());
        assertTrue(evFeatures.isBatteryTrackingAvailable());
        assertFalse(iceFeatures.isBatteryTrackingAvailable());
        assertFalse(evFeatures.isFuelConsumptionAvailable());
        assertTrue(iceFeatures.isFuelConsumptionAvailable());
    }
}
