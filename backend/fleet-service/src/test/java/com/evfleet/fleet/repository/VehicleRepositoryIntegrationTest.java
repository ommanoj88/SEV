package com.evfleet.fleet.repository;

import com.evfleet.fleet.model.FuelType;
import com.evfleet.fleet.model.Vehicle;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for VehicleRepository - PR 4: Multi-fuel Query Methods
 * Tests database operations and custom queries for multi-fuel fleet management
 */
@DataJpaTest
@ActiveProfiles("test")
class VehicleRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private VehicleRepository vehicleRepository;

    private Vehicle evVehicle1;
    private Vehicle evVehicle2;
    private Vehicle iceVehicle1;
    private Vehicle iceVehicle2;
    private Vehicle hybridVehicle1;

    private static final Long COMPANY_ID_1 = 1L;
    private static final Long COMPANY_ID_2 = 2L;

    @BeforeEach
    void setUp() {
        // Clear any existing data
        vehicleRepository.deleteAll();
        entityManager.flush();
        entityManager.clear();

        // Create EV vehicles
        evVehicle1 = createVehicle("EV001", FuelType.EV, COMPANY_ID_1, 75.0, 25.0, null, null);
        evVehicle2 = createVehicle("EV002", FuelType.EV, COMPANY_ID_1, 100.0, 15.0, null, null);

        // Create ICE vehicles
        iceVehicle1 = createVehicle("ICE001", FuelType.ICE, COMPANY_ID_1, null, null, 60.0, 15.0);
        iceVehicle2 = createVehicle("ICE002", FuelType.ICE, COMPANY_ID_2, null, null, 50.0, 40.0);

        // Create Hybrid vehicle
        hybridVehicle1 = createVehicle("HYB001", FuelType.HYBRID, COMPANY_ID_1, 50.0, 30.0, 45.0, 25.0);

        // Save all vehicles
        evVehicle1 = vehicleRepository.save(evVehicle1);
        evVehicle2 = vehicleRepository.save(evVehicle2);
        iceVehicle1 = vehicleRepository.save(iceVehicle1);
        iceVehicle2 = vehicleRepository.save(iceVehicle2);
        hybridVehicle1 = vehicleRepository.save(hybridVehicle1);

        entityManager.flush();
        entityManager.clear();
    }

    private Vehicle createVehicle(String vehicleNumber, FuelType fuelType, Long companyId,
                                   Double batteryCapacity, Double currentBatterySoc,
                                   Double fuelTankCapacity, Double fuelLevel) {
        Vehicle vehicle = new Vehicle();
        vehicle.setCompanyId(companyId);
        vehicle.setVehicleNumber(vehicleNumber);
        vehicle.setType(Vehicle.VehicleType.LCV);
        vehicle.setFuelType(fuelType);
        vehicle.setMake("TestMake");
        vehicle.setModel("TestModel");
        vehicle.setYear(2024);
        vehicle.setStatus(Vehicle.VehicleStatus.ACTIVE);
        vehicle.setBatteryCapacity(batteryCapacity);
        vehicle.setCurrentBatterySoc(currentBatterySoc);
        vehicle.setFuelTankCapacity(fuelTankCapacity);
        vehicle.setFuelLevel(fuelLevel);
        vehicle.setCreatedAt(LocalDateTime.now());
        vehicle.setUpdatedAt(LocalDateTime.now());
        return vehicle;
    }

    @Test
    void testFindByFuelType_EV() {
        // When searching for EV vehicles
        List<Vehicle> evVehicles = vehicleRepository.findByFuelType(FuelType.EV);

        // Then only EV vehicles are returned
        assertEquals(2, evVehicles.size());
        assertTrue(evVehicles.stream().allMatch(v -> v.getFuelType() == FuelType.EV));
    }

    @Test
    void testFindByFuelType_ICE() {
        // When searching for ICE vehicles
        List<Vehicle> iceVehicles = vehicleRepository.findByFuelType(FuelType.ICE);

        // Then only ICE vehicles are returned
        assertEquals(2, iceVehicles.size());
        assertTrue(iceVehicles.stream().allMatch(v -> v.getFuelType() == FuelType.ICE));
    }

    @Test
    void testFindByFuelType_HYBRID() {
        // When searching for HYBRID vehicles
        List<Vehicle> hybridVehicles = vehicleRepository.findByFuelType(FuelType.HYBRID);

        // Then only HYBRID vehicles are returned
        assertEquals(1, hybridVehicles.size());
        assertEquals(FuelType.HYBRID, hybridVehicles.get(0).getFuelType());
    }

    @Test
    void testFindByCompanyIdAndFuelType_Company1_EV() {
        // When searching for company 1's EV vehicles
        List<Vehicle> vehicles = vehicleRepository.findByCompanyIdAndFuelType(COMPANY_ID_1, FuelType.EV);

        // Then only company 1's EV vehicles are returned
        assertEquals(2, vehicles.size());
        assertTrue(vehicles.stream().allMatch(v -> v.getCompanyId().equals(COMPANY_ID_1)));
        assertTrue(vehicles.stream().allMatch(v -> v.getFuelType() == FuelType.EV));
    }

    @Test
    void testFindByCompanyIdAndFuelType_Company1_ICE() {
        // When searching for company 1's ICE vehicles
        List<Vehicle> vehicles = vehicleRepository.findByCompanyIdAndFuelType(COMPANY_ID_1, FuelType.ICE);

        // Then only company 1's ICE vehicle is returned
        assertEquals(1, vehicles.size());
        assertEquals(COMPANY_ID_1, vehicles.get(0).getCompanyId());
        assertEquals(FuelType.ICE, vehicles.get(0).getFuelType());
    }

    @Test
    void testFindByCompanyIdAndFuelType_Company2_ICE() {
        // When searching for company 2's ICE vehicles
        List<Vehicle> vehicles = vehicleRepository.findByCompanyIdAndFuelType(COMPANY_ID_2, FuelType.ICE);

        // Then only company 2's ICE vehicle is returned
        assertEquals(1, vehicles.size());
        assertEquals(COMPANY_ID_2, vehicles.get(0).getCompanyId());
    }

    @Test
    void testGetFleetCompositionByCompany_Company1() {
        // When getting fleet composition for company 1
        List<Object[]> composition = vehicleRepository.getFleetCompositionByCompany(COMPANY_ID_1);

        // Then all fuel types are represented
        assertNotNull(composition);
        assertEquals(3, composition.size()); // EV, ICE, HYBRID

        // Verify counts
        for (Object[] row : composition) {
            FuelType fuelType = (FuelType) row[0];
            Long count = ((Number) row[1]).longValue();

            if (fuelType == FuelType.EV) {
                assertEquals(2L, count);
            } else if (fuelType == FuelType.ICE) {
                assertEquals(1L, count);
            } else if (fuelType == FuelType.HYBRID) {
                assertEquals(1L, count);
            }
        }
    }

    @Test
    void testGetFleetCompositionByCompany_Company2() {
        // When getting fleet composition for company 2
        List<Object[]> composition = vehicleRepository.getFleetCompositionByCompany(COMPANY_ID_2);

        // Then only ICE is represented
        assertNotNull(composition);
        assertEquals(1, composition.size());

        Object[] row = composition.get(0);
        assertEquals(FuelType.ICE, row[0]);
        assertEquals(1L, ((Number) row[1]).longValue());
    }

    @Test
    void testFindLowBatteryVehicles() {
        // When searching for vehicles with battery below 20%
        List<Vehicle> lowBatteryVehicles = vehicleRepository.findLowBatteryVehicles(COMPANY_ID_1, 20.0);

        // Then only EV002 (15% SOC) is returned
        assertEquals(1, lowBatteryVehicles.size());
        assertEquals("EV002", lowBatteryVehicles.get(0).getVehicleNumber());
        assertEquals(15.0, lowBatteryVehicles.get(0).getCurrentBatterySoc());
    }

    @Test
    void testFindLowBatteryVehicles_HigherThreshold() {
        // When searching for vehicles with battery below 30%
        List<Vehicle> lowBatteryVehicles = vehicleRepository.findLowBatteryVehicles(COMPANY_ID_1, 30.0);

        // Then EV002 (15%) and HYBRID (30%) should be considered
        assertEquals(2, lowBatteryVehicles.size());
        assertTrue(lowBatteryVehicles.stream().anyMatch(v -> v.getVehicleNumber().equals("EV002")));
    }

    @Test
    void testFindLowBatteryVehicles_NoResults() {
        // When searching for vehicles with battery below 10%
        List<Vehicle> lowBatteryVehicles = vehicleRepository.findLowBatteryVehicles(COMPANY_ID_1, 10.0);

        // Then no vehicles are returned
        assertEquals(0, lowBatteryVehicles.size());
    }

    @Test
    void testFindLowBatteryVehicles_OnlyEVAndHybrid() {
        // When searching for low battery vehicles
        List<Vehicle> lowBatteryVehicles = vehicleRepository.findLowBatteryVehicles(COMPANY_ID_1, 20.0);

        // Then ICE vehicles should not be included
        assertFalse(lowBatteryVehicles.stream().anyMatch(v -> v.getFuelType() == FuelType.ICE));
    }

    @Test
    void testFindLowFuelVehicles() {
        // When searching for vehicles with fuel below 30%
        // ICE001: 15/60 = 25%
        // HYB001: 25/45 = 55.6%
        List<Vehicle> lowFuelVehicles = vehicleRepository.findLowFuelVehicles(COMPANY_ID_1, 30.0);

        // Then only ICE001 (25%) is returned
        assertEquals(1, lowFuelVehicles.size());
        assertEquals("ICE001", lowFuelVehicles.get(0).getVehicleNumber());
    }

    @Test
    void testFindLowFuelVehicles_HigherThreshold() {
        // When searching for vehicles with fuel below 60%
        // ICE001: 15/60 = 25%
        // HYB001: 25/45 = 55.6%
        List<Vehicle> lowFuelVehicles = vehicleRepository.findLowFuelVehicles(COMPANY_ID_1, 60.0);

        // Then both ICE001 and HYB001 are returned
        assertEquals(2, lowFuelVehicles.size());
        assertTrue(lowFuelVehicles.stream().anyMatch(v -> v.getVehicleNumber().equals("ICE001")));
        assertTrue(lowFuelVehicles.stream().anyMatch(v -> v.getVehicleNumber().equals("HYB001")));
    }

    @Test
    void testFindLowFuelVehicles_NoResults() {
        // When searching for vehicles with fuel below 20%
        List<Vehicle> lowFuelVehicles = vehicleRepository.findLowFuelVehicles(COMPANY_ID_1, 20.0);

        // Then no vehicles are returned
        assertEquals(0, lowFuelVehicles.size());
    }

    @Test
    void testFindLowFuelVehicles_OnlyICEAndHybrid() {
        // When searching for low fuel vehicles
        List<Vehicle> lowFuelVehicles = vehicleRepository.findLowFuelVehicles(COMPANY_ID_1, 30.0);

        // Then EV vehicles should not be included
        assertFalse(lowFuelVehicles.stream().anyMatch(v -> v.getFuelType() == FuelType.EV));
    }

    @Test
    void testFindLowFuelVehicles_DifferentCompany() {
        // When searching for company 2's low fuel vehicles
        // ICE002: 40/50 = 80%
        List<Vehicle> lowFuelVehicles = vehicleRepository.findLowFuelVehicles(COMPANY_ID_2, 50.0);

        // Then no vehicles are returned (all above threshold)
        assertEquals(0, lowFuelVehicles.size());
    }

    @Test
    void testCountByCompanyIdAndFuelType_EV() {
        // When counting EV vehicles for company 1
        long count = vehicleRepository.countByCompanyIdAndFuelType(COMPANY_ID_1, FuelType.EV);

        // Then 2 vehicles are counted
        assertEquals(2, count);
    }

    @Test
    void testCountByCompanyIdAndFuelType_ICE() {
        // When counting ICE vehicles for company 1
        long count = vehicleRepository.countByCompanyIdAndFuelType(COMPANY_ID_1, FuelType.ICE);

        // Then 1 vehicle is counted
        assertEquals(1, count);
    }

    @Test
    void testCountByCompanyIdAndFuelType_HYBRID() {
        // When counting HYBRID vehicles for company 1
        long count = vehicleRepository.countByCompanyIdAndFuelType(COMPANY_ID_1, FuelType.HYBRID);

        // Then 1 vehicle is counted
        assertEquals(1, count);
    }

    @Test
    void testCountByCompanyIdAndFuelType_NoResults() {
        // When counting EV vehicles for company 2
        long count = vehicleRepository.countByCompanyIdAndFuelType(COMPANY_ID_2, FuelType.EV);

        // Then 0 vehicles are counted
        assertEquals(0, count);
    }

    @Test
    void testMultiFuelQueryCombinations() {
        // Verify total vehicle count
        List<Vehicle> allVehicles = vehicleRepository.findAll();
        assertEquals(5, allVehicles.size());

        // Verify company 1 has 4 vehicles
        List<Vehicle> company1Vehicles = vehicleRepository.findByCompanyId(COMPANY_ID_1);
        assertEquals(4, company1Vehicles.size());

        // Verify fuel type distribution
        assertEquals(2, vehicleRepository.countByCompanyIdAndFuelType(COMPANY_ID_1, FuelType.EV));
        assertEquals(1, vehicleRepository.countByCompanyIdAndFuelType(COMPANY_ID_1, FuelType.ICE));
        assertEquals(1, vehicleRepository.countByCompanyIdAndFuelType(COMPANY_ID_1, FuelType.HYBRID));
    }

    @Test
    void testLowBatteryAndLowFuelSeparation() {
        // Low battery threshold: 20%
        List<Vehicle> lowBattery = vehicleRepository.findLowBatteryVehicles(COMPANY_ID_1, 20.0);
        
        // Low fuel threshold: 30%
        List<Vehicle> lowFuel = vehicleRepository.findLowFuelVehicles(COMPANY_ID_1, 30.0);

        // Verify separation - no overlap in vehicle types
        for (Vehicle v : lowBattery) {
            assertTrue(v.getFuelType() == FuelType.EV || v.getFuelType() == FuelType.HYBRID);
        }

        for (Vehicle v : lowFuel) {
            assertTrue(v.getFuelType() == FuelType.ICE || v.getFuelType() == FuelType.HYBRID);
        }
    }
}
