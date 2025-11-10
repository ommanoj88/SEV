package com.evfleet.fleet.service;

import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.model.Trip;
import com.evfleet.fleet.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for EVCostCalculator
 * Tests energy cost calculations, efficiency metrics, and carbon footprint calculations for EV trips
 * 
 * @since 2.0.0 (Multi-fuel support - PR 7)
 */
@ExtendWith(MockitoExtension.class)
class EVCostCalculatorTest {

    @InjectMocks
    private EVCostCalculator evCostCalculator;

    private Vehicle evVehicle;
    private Trip trip;

    @BeforeEach
    void setUp() {
        // Create EV vehicle
        evVehicle = new Vehicle();
        evVehicle.setId(1L);
        evVehicle.setVehicleNumber("EV001");
        evVehicle.setFuelType(FuelType.EV);
        evVehicle.setBatteryCapacity(75.0); // 75 kWh battery
        evVehicle.setCurrentBatterySoc(80.0); // 80% SOC

        // Create trip
        trip = new Trip();
        trip.setId(1L);
        trip.setVehicleId(1L);
        trip.setDistance(100.0); // 100 km
        trip.setEnergyConsumed(15.0); // 15 kWh
        trip.setStartBatterySoc(95.0);
        trip.setEndBatterySoc(75.0);
    }

    @Test
    void testCalculateEnergyCost_withDefaultRate() {
        // Given: Trip with 15 kWh consumed, default rate is ₹8/kWh
        // Expected: 15 * 8 = ₹120

        // When
        double energyCost = evCostCalculator.calculateEnergyCost(trip, evVehicle);

        // Then
        assertEquals(120.0, energyCost, 0.01);
    }

    @Test
    void testCalculateEnergyCost_withCustomRate() {
        // Given: Trip with 15 kWh consumed, custom rate ₹10/kWh
        // Expected: 15 * 10 = ₹150

        // When
        double energyCost = evCostCalculator.calculateEnergyCost(trip, evVehicle, 10.0);

        // Then
        assertEquals(150.0, energyCost, 0.01);
    }

    @Test
    void testCalculateEnergyCost_withNoEnergyConsumed() {
        // Given: Trip with no energy consumption data
        trip.setEnergyConsumed(null);

        // When
        double energyCost = evCostCalculator.calculateEnergyCost(trip, evVehicle);

        // Then
        assertEquals(0.0, energyCost, 0.01);
    }

    @Test
    void testCalculateEnergyCost_withZeroEnergyConsumed() {
        // Given: Trip with zero energy consumption
        trip.setEnergyConsumed(0.0);

        // When
        double energyCost = evCostCalculator.calculateEnergyCost(trip, evVehicle);

        // Then
        assertEquals(0.0, energyCost, 0.01);
    }

    @Test
    void testCalculateEnergyEfficiency() {
        // Given: Trip with 100 km distance and 15 kWh consumed
        // Expected: (15 / 100) * 100 = 15 kWh/100km

        // When
        double efficiency = evCostCalculator.calculateEnergyEfficiency(trip);

        // Then
        assertEquals(15.0, efficiency, 0.01);
    }

    @Test
    void testCalculateEnergyEfficiency_withHighEfficiency() {
        // Given: Trip with 100 km distance and 12 kWh consumed (efficient)
        trip.setEnergyConsumed(12.0);

        // When
        double efficiency = evCostCalculator.calculateEnergyEfficiency(trip);

        // Then
        assertEquals(12.0, efficiency, 0.01);
    }

    @Test
    void testCalculateEnergyEfficiency_withNoDistance() {
        // Given: Trip with no distance data
        trip.setDistance(null);

        // When
        double efficiency = evCostCalculator.calculateEnergyEfficiency(trip);

        // Then
        assertEquals(0.0, efficiency, 0.01);
    }

    @Test
    void testCalculateEnergyEfficiency_withNoEnergy() {
        // Given: Trip with no energy consumption data
        trip.setEnergyConsumed(null);

        // When
        double efficiency = evCostCalculator.calculateEnergyEfficiency(trip);

        // Then
        assertEquals(0.0, efficiency, 0.01);
    }

    @Test
    void testCalculateCarbonFootprint() {
        // Given: Trip with 15 kWh consumed, carbon factor 0.82 kg CO2/kWh
        // Expected: 15 * 0.82 = 12.3 kg CO2

        // When
        double carbonFootprint = evCostCalculator.calculateCarbonFootprint(trip);

        // Then
        assertEquals(12.3, carbonFootprint, 0.01);
    }

    @Test
    void testCalculateCarbonFootprint_withLargeConsumption() {
        // Given: Trip with 50 kWh consumed (long distance)
        trip.setEnergyConsumed(50.0);
        trip.setDistance(300.0);

        // When
        double carbonFootprint = evCostCalculator.calculateCarbonFootprint(trip);

        // Then
        assertEquals(41.0, carbonFootprint, 0.01); // 50 * 0.82
    }

    @Test
    void testCalculateCarbonFootprint_withNoEnergyConsumed() {
        // Given: Trip with no energy consumption data
        trip.setEnergyConsumed(null);

        // When
        double carbonFootprint = evCostCalculator.calculateCarbonFootprint(trip);

        // Then
        assertEquals(0.0, carbonFootprint, 0.01);
    }

    @Test
    void testCalculateCostPerKm() {
        // Given: Trip with 100 km distance and 15 kWh consumed
        // Cost: 15 * 8 = ₹120
        // Expected: 120 / 100 = ₹1.20/km

        // When
        double costPerKm = evCostCalculator.calculateCostPerKm(trip, evVehicle);

        // Then
        assertEquals(1.20, costPerKm, 0.01);
    }

    @Test
    void testCalculateCostPerKm_withNoDistance() {
        // Given: Trip with no distance data
        trip.setDistance(null);

        // When
        double costPerKm = evCostCalculator.calculateCostPerKm(trip, evVehicle);

        // Then
        assertEquals(0.0, costPerKm, 0.01);
    }

    @Test
    void testCalculateCostPerKm_withZeroDistance() {
        // Given: Trip with zero distance
        trip.setDistance(0.0);

        // When
        double costPerKm = evCostCalculator.calculateCostPerKm(trip, evVehicle);

        // Then
        assertEquals(0.0, costPerKm, 0.01);
    }

    @Test
    void testCalculateEstimatedRange() {
        // Given: Vehicle with 75 kWh battery at 80% SOC = 60 kWh available
        // Average efficiency: 15 kWh/100km
        // Expected: (60 / 15) * 100 = 400 km

        // When
        double estimatedRange = evCostCalculator.calculateEstimatedRange(evVehicle, 15.0);

        // Then
        assertEquals(400.0, estimatedRange, 0.01);
    }

    @Test
    void testCalculateEstimatedRange_withLowSOC() {
        // Given: Vehicle at 20% SOC = 15 kWh available
        evVehicle.setCurrentBatterySoc(20.0);
        // Average efficiency: 15 kWh/100km
        // Expected: (15 / 15) * 100 = 100 km

        // When
        double estimatedRange = evCostCalculator.calculateEstimatedRange(evVehicle, 15.0);

        // Then
        assertEquals(100.0, estimatedRange, 0.01);
    }

    @Test
    void testCalculateEstimatedRange_withHighEfficiency() {
        // Given: Vehicle at 80% SOC = 60 kWh available
        // High efficiency: 10 kWh/100km (very efficient driving)
        // Expected: (60 / 10) * 100 = 600 km

        // When
        double estimatedRange = evCostCalculator.calculateEstimatedRange(evVehicle, 10.0);

        // Then
        assertEquals(600.0, estimatedRange, 0.01);
    }

    @Test
    void testCalculateEstimatedRange_withNoBatteryCapacity() {
        // Given: Vehicle with no battery capacity data
        evVehicle.setBatteryCapacity(null);

        // When
        double estimatedRange = evCostCalculator.calculateEstimatedRange(evVehicle, 15.0);

        // Then
        assertEquals(0.0, estimatedRange, 0.01);
    }

    @Test
    void testCalculateEstimatedRange_withZeroEfficiency() {
        // Given: Zero or invalid efficiency
        // When
        double estimatedRange = evCostCalculator.calculateEstimatedRange(evVehicle, 0.0);

        // Then
        assertEquals(0.0, estimatedRange, 0.01);
    }

    @Test
    void testGetDefaultElectricityCost() {
        // When
        double defaultCost = evCostCalculator.getDefaultElectricityCost();

        // Then
        assertEquals(8.0, defaultCost, 0.01);
    }

    @Test
    void testGetCarbonEmissionFactor() {
        // When
        double carbonFactor = evCostCalculator.getCarbonEmissionFactor();

        // Then
        assertEquals(0.82, carbonFactor, 0.01);
    }

    @Test
    void testRealWorldScenario_cityDriving() {
        // Given: City driving scenario - short trip, moderate consumption
        trip.setDistance(50.0); // 50 km
        trip.setEnergyConsumed(8.5); // 8.5 kWh (17 kWh/100km - typical city)
        trip.setStartBatterySoc(90.0);
        trip.setEndBatterySoc(78.67);

        // When
        double energyCost = evCostCalculator.calculateEnergyCost(trip, evVehicle);
        double efficiency = evCostCalculator.calculateEnergyEfficiency(trip);
        double carbonFootprint = evCostCalculator.calculateCarbonFootprint(trip);
        double costPerKm = evCostCalculator.calculateCostPerKm(trip, evVehicle);

        // Then
        assertEquals(68.0, energyCost, 0.1); // 8.5 * 8
        assertEquals(17.0, efficiency, 0.1); // (8.5 / 50) * 100
        assertEquals(6.97, carbonFootprint, 0.1); // 8.5 * 0.82
        assertEquals(1.36, costPerKm, 0.1); // 68 / 50
    }

    @Test
    void testRealWorldScenario_highwayDriving() {
        // Given: Highway driving scenario - long trip, better efficiency
        trip.setDistance(200.0); // 200 km
        trip.setEnergyConsumed(26.0); // 26 kWh (13 kWh/100km - efficient highway)
        trip.setStartBatterySoc(95.0);
        trip.setEndBatterySoc(60.33);

        // When
        double energyCost = evCostCalculator.calculateEnergyCost(trip, evVehicle);
        double efficiency = evCostCalculator.calculateEnergyEfficiency(trip);
        double carbonFootprint = evCostCalculator.calculateCarbonFootprint(trip);
        double costPerKm = evCostCalculator.calculateCostPerKm(trip, evVehicle);

        // Then
        assertEquals(208.0, energyCost, 0.1); // 26 * 8
        assertEquals(13.0, efficiency, 0.1); // (26 / 200) * 100
        assertEquals(21.32, carbonFootprint, 0.1); // 26 * 0.82
        assertEquals(1.04, costPerKm, 0.1); // 208 / 200
    }
}
