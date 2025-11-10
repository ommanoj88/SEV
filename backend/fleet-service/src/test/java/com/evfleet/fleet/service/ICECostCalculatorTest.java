package com.evfleet.fleet.service;

import com.evfleet.fleet.model.FuelConsumption;
import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.model.Trip;
import com.evfleet.fleet.model.Vehicle;
import com.evfleet.fleet.repository.FuelConsumptionRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

/**
 * Unit tests for ICECostCalculator
 * Tests fuel cost calculations, efficiency metrics, and carbon footprint calculations for ICE trips
 * 
 * @since 2.0.0 (Multi-fuel support - PR 7)
 */
@ExtendWith(MockitoExtension.class)
class ICECostCalculatorTest {

    @Mock
    private FuelConsumptionRepository fuelConsumptionRepository;

    @InjectMocks
    private ICECostCalculator iceCostCalculator;

    private Vehicle iceVehicle;
    private Trip trip;

    @BeforeEach
    void setUp() {
        // Create ICE vehicle (Diesel)
        iceVehicle = new Vehicle();
        iceVehicle.setId(1L);
        iceVehicle.setVehicleNumber("ICE001");
        iceVehicle.setFuelType(FuelType.ICE);
        iceVehicle.setEngineType("DIESEL");
        iceVehicle.setFuelTankCapacity(60.0); // 60 liters
        iceVehicle.setFuelLevel(45.0); // 45 liters

        // Create trip
        trip = new Trip();
        trip.setId(1L);
        trip.setVehicleId(1L);
        trip.setDistance(100.0); // 100 km
        trip.setStartTime(LocalDateTime.now().minusHours(2));
        trip.setEndTime(LocalDateTime.now());
    }

    @Test
    void testCalculateFuelCost_withFuelConsumptionRecords() {
        // Given: Fuel consumption records showing 8 liters consumed
        FuelConsumption consumption = new FuelConsumption();
        consumption.setFuelConsumedLiters(8.0);
        when(fuelConsumptionRepository.findByVehicleIdAndTimestampBetweenOrderByTimestampDesc(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(consumption));

        // Expected: 8 liters * ₹95/liter (diesel) = ₹760

        // When
        double fuelCost = iceCostCalculator.calculateFuelCost(trip, iceVehicle);

        // Then
        assertEquals(760.0, fuelCost, 0.01);
    }

    @Test
    void testCalculateFuelCost_withMultipleFuelConsumptionRecords() {
        // Given: Multiple fuel consumption records totaling 10 liters
        FuelConsumption consumption1 = new FuelConsumption();
        consumption1.setFuelConsumedLiters(5.0);
        FuelConsumption consumption2 = new FuelConsumption();
        consumption2.setFuelConsumedLiters(5.0);
        
        when(fuelConsumptionRepository.findByVehicleIdAndTimestampBetweenOrderByTimestampDesc(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Arrays.asList(consumption1, consumption2));

        // Expected: 10 liters * ₹95/liter = ₹950

        // When
        double fuelCost = iceCostCalculator.calculateFuelCost(trip, iceVehicle);

        // Then
        assertEquals(950.0, fuelCost, 0.01);
    }

    @Test
    void testCalculateFuelCost_withEstimatedConsumption() {
        // Given: No fuel consumption records, fallback to estimation
        when(fuelConsumptionRepository.findByVehicleIdAndTimestampBetweenOrderByTimestampDesc(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.emptyList());

        // Expected: Estimated 8 liters (100km / 100 * 8) * ₹95/liter = ₹760

        // When
        double fuelCost = iceCostCalculator.calculateFuelCost(trip, iceVehicle);

        // Then
        assertEquals(760.0, fuelCost, 0.01);
    }

    @Test
    void testCalculateFuelCost_withPetrolVehicle() {
        // Given: Petrol vehicle
        iceVehicle.setEngineType("PETROL");
        FuelConsumption consumption = new FuelConsumption();
        consumption.setFuelConsumedLiters(8.0);
        when(fuelConsumptionRepository.findByVehicleIdAndTimestampBetweenOrderByTimestampDesc(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(consumption));

        // Expected: 8 liters * ₹105/liter (petrol) = ₹840

        // When
        double fuelCost = iceCostCalculator.calculateFuelCost(trip, iceVehicle);

        // Then
        assertEquals(840.0, fuelCost, 0.01);
    }

    @Test
    void testCalculateFuelCost_withCNGVehicle() {
        // Given: CNG vehicle
        iceVehicle.setEngineType("CNG");
        FuelConsumption consumption = new FuelConsumption();
        consumption.setFuelConsumedLiters(6.0); // 6 kg
        when(fuelConsumptionRepository.findByVehicleIdAndTimestampBetweenOrderByTimestampDesc(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(consumption));

        // Expected: 6 kg * ₹80/kg (CNG) = ₹480

        // When
        double fuelCost = iceCostCalculator.calculateFuelCost(trip, iceVehicle);

        // Then
        assertEquals(480.0, fuelCost, 0.01);
    }

    @Test
    void testCalculateFuelCost_withCustomRate() {
        // Given: Custom fuel rate ₹100/liter
        FuelConsumption consumption = new FuelConsumption();
        consumption.setFuelConsumedLiters(8.0);
        when(fuelConsumptionRepository.findByVehicleIdAndTimestampBetweenOrderByTimestampDesc(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(consumption));

        // Expected: 8 liters * ₹100/liter = ₹800

        // When
        double fuelCost = iceCostCalculator.calculateFuelCost(trip, iceVehicle, 100.0);

        // Then
        assertEquals(800.0, fuelCost, 0.01);
    }

    @Test
    void testCalculateFuelEfficiency() {
        // Given: 100 km distance, 8 liters consumed
        FuelConsumption consumption = new FuelConsumption();
        consumption.setFuelConsumedLiters(8.0);
        when(fuelConsumptionRepository.findByVehicleIdAndTimestampBetweenOrderByTimestampDesc(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(consumption));

        // Expected: (8 / 100) * 100 = 8 L/100km

        // When
        double efficiency = iceCostCalculator.calculateFuelEfficiency(trip, iceVehicle);

        // Then
        assertEquals(8.0, efficiency, 0.01);
    }

    @Test
    void testCalculateFuelEfficiency_withNoDistance() {
        // Given: Trip with no distance
        trip.setDistance(null);

        // When
        double efficiency = iceCostCalculator.calculateFuelEfficiency(trip, iceVehicle);

        // Then
        assertEquals(0.0, efficiency, 0.01);
    }

    @Test
    void testCalculateMileage() {
        // Given: 100 km distance, 8 liters consumed
        FuelConsumption consumption = new FuelConsumption();
        consumption.setFuelConsumedLiters(8.0);
        when(fuelConsumptionRepository.findByVehicleIdAndTimestampBetweenOrderByTimestampDesc(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(consumption));

        // Expected: 100 / 8 = 12.5 km/L

        // When
        double mileage = iceCostCalculator.calculateMileage(trip, iceVehicle);

        // Then
        assertEquals(12.5, mileage, 0.01);
    }

    @Test
    void testCalculateMileage_withHighEfficiency() {
        // Given: 100 km distance, 6 liters consumed (efficient)
        FuelConsumption consumption = new FuelConsumption();
        consumption.setFuelConsumedLiters(6.0);
        when(fuelConsumptionRepository.findByVehicleIdAndTimestampBetweenOrderByTimestampDesc(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(consumption));

        // Expected: 100 / 6 = 16.67 km/L

        // When
        double mileage = iceCostCalculator.calculateMileage(trip, iceVehicle);

        // Then
        assertEquals(16.67, mileage, 0.01);
    }

    @Test
    void testCalculateCarbonFootprint_diesel() {
        // Given: Diesel vehicle with 8 liters consumed
        FuelConsumption consumption = new FuelConsumption();
        consumption.setFuelConsumedLiters(8.0);
        when(fuelConsumptionRepository.findByVehicleIdAndTimestampBetweenOrderByTimestampDesc(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(consumption));

        // Expected: 8 * 2.68 (diesel carbon factor) = 21.44 kg CO2

        // When
        double carbonFootprint = iceCostCalculator.calculateCarbonFootprint(trip, iceVehicle);

        // Then
        assertEquals(21.44, carbonFootprint, 0.01);
    }

    @Test
    void testCalculateCarbonFootprint_petrol() {
        // Given: Petrol vehicle with 8 liters consumed
        iceVehicle.setEngineType("PETROL");
        FuelConsumption consumption = new FuelConsumption();
        consumption.setFuelConsumedLiters(8.0);
        when(fuelConsumptionRepository.findByVehicleIdAndTimestampBetweenOrderByTimestampDesc(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(consumption));

        // Expected: 8 * 2.31 (petrol carbon factor) = 18.48 kg CO2

        // When
        double carbonFootprint = iceCostCalculator.calculateCarbonFootprint(trip, iceVehicle);

        // Then
        assertEquals(18.48, carbonFootprint, 0.01);
    }

    @Test
    void testCalculateCarbonFootprint_cng() {
        // Given: CNG vehicle with 6 kg consumed
        iceVehicle.setEngineType("CNG");
        FuelConsumption consumption = new FuelConsumption();
        consumption.setFuelConsumedLiters(6.0);
        when(fuelConsumptionRepository.findByVehicleIdAndTimestampBetweenOrderByTimestampDesc(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(consumption));

        // Expected: 6 * 1.88 (CNG carbon factor) = 11.28 kg CO2

        // When
        double carbonFootprint = iceCostCalculator.calculateCarbonFootprint(trip, iceVehicle);

        // Then
        assertEquals(11.28, carbonFootprint, 0.01);
    }

    @Test
    void testCalculateCostPerKm() {
        // Given: 100 km distance, 8 liters consumed
        FuelConsumption consumption = new FuelConsumption();
        consumption.setFuelConsumedLiters(8.0);
        when(fuelConsumptionRepository.findByVehicleIdAndTimestampBetweenOrderByTimestampDesc(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(consumption));

        // Expected: (8 * 95) / 100 = ₹7.60/km

        // When
        double costPerKm = iceCostCalculator.calculateCostPerKm(trip, iceVehicle);

        // Then
        assertEquals(7.60, costPerKm, 0.01);
    }

    @Test
    void testCalculateCostPerKm_withNoDistance() {
        // Given: Trip with no distance
        trip.setDistance(null);

        // When
        double costPerKm = iceCostCalculator.calculateCostPerKm(trip, iceVehicle);

        // Then
        assertEquals(0.0, costPerKm, 0.01);
    }

    @Test
    void testCalculateEstimatedRange() {
        // Given: Vehicle with 45 liters fuel, average mileage 12.5 km/L
        // Expected: 45 * 12.5 = 562.5 km

        // When
        double estimatedRange = iceCostCalculator.calculateEstimatedRange(iceVehicle, 12.5);

        // Then
        assertEquals(562.5, estimatedRange, 0.01);
    }

    @Test
    void testCalculateEstimatedRange_withLowFuel() {
        // Given: Vehicle with 10 liters fuel (low), average mileage 12.5 km/L
        iceVehicle.setFuelLevel(10.0);
        // Expected: 10 * 12.5 = 125 km

        // When
        double estimatedRange = iceCostCalculator.calculateEstimatedRange(iceVehicle, 12.5);

        // Then
        assertEquals(125.0, estimatedRange, 0.01);
    }

    @Test
    void testCalculateEstimatedRange_withNoFuelLevel() {
        // Given: Vehicle with no fuel level data
        iceVehicle.setFuelLevel(null);

        // When
        double estimatedRange = iceCostCalculator.calculateEstimatedRange(iceVehicle, 12.5);

        // Then
        assertEquals(0.0, estimatedRange, 0.01);
    }

    @Test
    void testGetDefaultPetrolCost() {
        // When
        double defaultCost = iceCostCalculator.getDefaultPetrolCost();

        // Then
        assertEquals(105.0, defaultCost, 0.01);
    }

    @Test
    void testGetDefaultDieselCost() {
        // When
        double defaultCost = iceCostCalculator.getDefaultDieselCost();

        // Then
        assertEquals(95.0, defaultCost, 0.01);
    }

    @Test
    void testGetDefaultCngCost() {
        // When
        double defaultCost = iceCostCalculator.getDefaultCngCost();

        // Then
        assertEquals(80.0, defaultCost, 0.01);
    }

    @Test
    void testRealWorldScenario_dieselLCV() {
        // Given: Diesel LCV - typical commercial vehicle
        trip.setDistance(150.0); // 150 km trip
        FuelConsumption consumption = new FuelConsumption();
        consumption.setFuelConsumedLiters(12.0); // 12 liters (8 L/100km)
        when(fuelConsumptionRepository.findByVehicleIdAndTimestampBetweenOrderByTimestampDesc(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(consumption));

        // When
        double fuelCost = iceCostCalculator.calculateFuelCost(trip, iceVehicle);
        double efficiency = iceCostCalculator.calculateFuelEfficiency(trip, iceVehicle);
        double mileage = iceCostCalculator.calculateMileage(trip, iceVehicle);
        double carbonFootprint = iceCostCalculator.calculateCarbonFootprint(trip, iceVehicle);
        double costPerKm = iceCostCalculator.calculateCostPerKm(trip, iceVehicle);

        // Then
        assertEquals(1140.0, fuelCost, 0.1); // 12 * 95
        assertEquals(8.0, efficiency, 0.1); // (12 / 150) * 100
        assertEquals(12.5, mileage, 0.1); // 150 / 12
        assertEquals(32.16, carbonFootprint, 0.1); // 12 * 2.68
        assertEquals(7.60, costPerKm, 0.1); // 1140 / 150
    }

    @Test
    void testRealWorldScenario_petrolCityDriving() {
        // Given: Petrol vehicle in city conditions
        iceVehicle.setEngineType("PETROL");
        trip.setDistance(80.0); // 80 km city driving
        FuelConsumption consumption = new FuelConsumption();
        consumption.setFuelConsumedLiters(8.0); // 8 liters (10 L/100km - city)
        when(fuelConsumptionRepository.findByVehicleIdAndTimestampBetweenOrderByTimestampDesc(
                eq(1L), any(LocalDateTime.class), any(LocalDateTime.class)))
                .thenReturn(Collections.singletonList(consumption));

        // When
        double fuelCost = iceCostCalculator.calculateFuelCost(trip, iceVehicle);
        double efficiency = iceCostCalculator.calculateFuelEfficiency(trip, iceVehicle);
        double mileage = iceCostCalculator.calculateMileage(trip, iceVehicle);
        double carbonFootprint = iceCostCalculator.calculateCarbonFootprint(trip, iceVehicle);

        // Then
        assertEquals(840.0, fuelCost, 0.1); // 8 * 105
        assertEquals(10.0, efficiency, 0.1); // (8 / 80) * 100
        assertEquals(10.0, mileage, 0.1); // 80 / 8
        assertEquals(18.48, carbonFootprint, 0.1); // 8 * 2.31
    }
}
