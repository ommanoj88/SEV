package com.evfleet.fleet.model;

import org.junit.jupiter.api.Test;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for Vehicle entity with multi-fuel support
 * Tests EV, ICE, and HYBRID vehicle configurations
 */
class VehicleTest {

    @Test
    void testCreateEVVehicle() {
        // Test creating an EV vehicle with required fields
        Vehicle vehicle = new Vehicle();
        vehicle.setId(1L);
        vehicle.setCompanyId(100L);
        vehicle.setVehicleNumber("EV-001");
        vehicle.setFuelType(FuelType.EV);
        vehicle.setMake("Tesla");
        vehicle.setModel("Model 3");
        vehicle.setYear(2023);
        vehicle.setBatteryCapacity(75.0);
        vehicle.setCurrentBatterySoc(80.0);
        vehicle.setStatus(Vehicle.VehicleStatus.ACTIVE);
        vehicle.setType(Vehicle.VehicleType.LCV);

        assertNotNull(vehicle);
        assertEquals(FuelType.EV, vehicle.getFuelType());
        assertEquals(75.0, vehicle.getBatteryCapacity());
        assertEquals(80.0, vehicle.getCurrentBatterySoc());
        assertNull(vehicle.getFuelTankCapacity(), "EV should not have fuel tank");
        assertNull(vehicle.getFuelLevel(), "EV should not have fuel level");
    }

    @Test
    void testCreateICEVehicle() {
        // Test creating an ICE vehicle with required fields
        Vehicle vehicle = new Vehicle();
        vehicle.setId(2L);
        vehicle.setCompanyId(100L);
        vehicle.setVehicleNumber("ICE-001");
        vehicle.setFuelType(FuelType.ICE);
        vehicle.setMake("Tata");
        vehicle.setModel("Ace");
        vehicle.setYear(2023);
        vehicle.setFuelTankCapacity(50.0);
        vehicle.setFuelLevel(40.0);
        vehicle.setEngineType("DIESEL");
        vehicle.setStatus(Vehicle.VehicleStatus.ACTIVE);
        vehicle.setType(Vehicle.VehicleType.LCV);

        assertNotNull(vehicle);
        assertEquals(FuelType.ICE, vehicle.getFuelType());
        assertEquals(50.0, vehicle.getFuelTankCapacity());
        assertEquals(40.0, vehicle.getFuelLevel());
        assertEquals("DIESEL", vehicle.getEngineType());
        assertNull(vehicle.getBatteryCapacity(), "ICE should not have battery capacity");
        assertNull(vehicle.getCurrentBatterySoc(), "ICE should not have battery SOC");
    }

    @Test
    void testCreateHybridVehicle() {
        // Test creating a HYBRID vehicle with both EV and ICE fields
        Vehicle vehicle = new Vehicle();
        vehicle.setId(3L);
        vehicle.setCompanyId(100L);
        vehicle.setVehicleNumber("HYB-001");
        vehicle.setFuelType(FuelType.HYBRID);
        vehicle.setMake("Toyota");
        vehicle.setModel("Prius");
        vehicle.setYear(2023);
        // EV fields
        vehicle.setBatteryCapacity(8.8);
        vehicle.setCurrentBatterySoc(75.0);
        vehicle.setDefaultChargerType("Type 2");
        // ICE fields
        vehicle.setFuelTankCapacity(45.0);
        vehicle.setFuelLevel(35.0);
        vehicle.setEngineType("PETROL");
        vehicle.setStatus(Vehicle.VehicleStatus.ACTIVE);
        vehicle.setType(Vehicle.VehicleType.LCV);

        assertNotNull(vehicle);
        assertEquals(FuelType.HYBRID, vehicle.getFuelType());
        // Verify EV fields
        assertEquals(8.8, vehicle.getBatteryCapacity());
        assertEquals(75.0, vehicle.getCurrentBatterySoc());
        assertEquals("Type 2", vehicle.getDefaultChargerType());
        // Verify ICE fields
        assertEquals(45.0, vehicle.getFuelTankCapacity());
        assertEquals(35.0, vehicle.getFuelLevel());
        assertEquals("PETROL", vehicle.getEngineType());
    }

    @Test
    void testVehicleWithDefaultFuelType() {
        // Test backward compatibility - vehicles default to EV if not specified
        Vehicle vehicle = new Vehicle();
        vehicle.setId(4L);
        vehicle.setVehicleNumber("OLD-001");
        vehicle.setBatteryCapacity(60.0);
        
        // Note: In actual database migration, existing vehicles will have fuel_type = 'EV'
        // This test verifies that the field can be left as null in Java (database will have default)
        assertNull(vehicle.getFuelType(), "New vehicle without fuel type should be null in Java");
    }

    @Test
    void testVehicleLocationFields() {
        // Test location tracking fields
        Vehicle vehicle = new Vehicle();
        vehicle.setLatitude(28.7041);
        vehicle.setLongitude(77.1025);
        vehicle.setLastUpdated(LocalDateTime.now());

        assertEquals(28.7041, vehicle.getLatitude());
        assertEquals(77.1025, vehicle.getLongitude());
        assertNotNull(vehicle.getLastUpdated());
    }

    @Test
    void testVehicleStatusEnum() {
        // Test all vehicle statuses
        Vehicle vehicle = new Vehicle();
        
        vehicle.setStatus(Vehicle.VehicleStatus.ACTIVE);
        assertEquals(Vehicle.VehicleStatus.ACTIVE, vehicle.getStatus());
        
        vehicle.setStatus(Vehicle.VehicleStatus.CHARGING);
        assertEquals(Vehicle.VehicleStatus.CHARGING, vehicle.getStatus());
        
        vehicle.setStatus(Vehicle.VehicleStatus.MAINTENANCE);
        assertEquals(Vehicle.VehicleStatus.MAINTENANCE, vehicle.getStatus());
        
        vehicle.setStatus(Vehicle.VehicleStatus.IN_TRIP);
        assertEquals(Vehicle.VehicleStatus.IN_TRIP, vehicle.getStatus());
        
        vehicle.setStatus(Vehicle.VehicleStatus.INACTIVE);
        assertEquals(Vehicle.VehicleStatus.INACTIVE, vehicle.getStatus());
    }

    @Test
    void testVehicleTypeEnum() {
        // Test all vehicle types
        Vehicle vehicle = new Vehicle();
        
        vehicle.setType(Vehicle.VehicleType.TWO_WHEELER);
        assertEquals(Vehicle.VehicleType.TWO_WHEELER, vehicle.getType());
        
        vehicle.setType(Vehicle.VehicleType.THREE_WHEELER);
        assertEquals(Vehicle.VehicleType.THREE_WHEELER, vehicle.getType());
        
        vehicle.setType(Vehicle.VehicleType.LCV);
        assertEquals(Vehicle.VehicleType.LCV, vehicle.getType());
    }

    @Test
    void testTotalEnergyConsumed() {
        // Test energy consumption tracking for EV
        Vehicle vehicle = new Vehicle();
        vehicle.setFuelType(FuelType.EV);
        vehicle.setTotalEnergyConsumed(1500.0); // 1500 kWh

        assertEquals(1500.0, vehicle.getTotalEnergyConsumed());
    }

    @Test
    void testTotalFuelConsumed() {
        // Test fuel consumption tracking for ICE
        Vehicle vehicle = new Vehicle();
        vehicle.setFuelType(FuelType.ICE);
        vehicle.setTotalFuelConsumed(500.0); // 500 liters

        assertEquals(500.0, vehicle.getTotalFuelConsumed());
    }

    @Test
    void testHybridConsumptionTracking() {
        // Test both energy and fuel consumption for HYBRID
        Vehicle vehicle = new Vehicle();
        vehicle.setFuelType(FuelType.HYBRID);
        vehicle.setTotalEnergyConsumed(300.0);
        vehicle.setTotalFuelConsumed(200.0);

        assertEquals(300.0, vehicle.getTotalEnergyConsumed());
        assertEquals(200.0, vehicle.getTotalFuelConsumed());
    }

    @Test
    void testVehicleIdentificationFields() {
        // Test VIN and license plate
        Vehicle vehicle = new Vehicle();
        vehicle.setVin("1HGBH41JXMN109186");
        vehicle.setLicensePlate("DL01AB1234");
        vehicle.setColor("White");

        assertEquals("1HGBH41JXMN109186", vehicle.getVin());
        assertEquals("DL01AB1234", vehicle.getLicensePlate());
        assertEquals("White", vehicle.getColor());
    }

    @Test
    void testDriverAssignment() {
        // Test current driver assignment
        Vehicle vehicle = new Vehicle();
        vehicle.setCurrentDriverId(999L);

        assertEquals(999L, vehicle.getCurrentDriverId());
    }

    @Test
    void testTotalDistance() {
        // Test odometer tracking
        Vehicle vehicle = new Vehicle();
        vehicle.setTotalDistance(50000.0); // 50,000 km

        assertEquals(50000.0, vehicle.getTotalDistance());
    }

    @Test
    void testTimestampFields() {
        // Test audit fields
        Vehicle vehicle = new Vehicle();
        LocalDateTime now = LocalDateTime.now();
        vehicle.setCreatedAt(now);
        vehicle.setUpdatedAt(now);

        assertEquals(now, vehicle.getCreatedAt());
        assertEquals(now, vehicle.getUpdatedAt());
    }

    @Test
    void testFuelPercentageCalculation() {
        // Test fuel percentage calculation for ICE vehicles
        Vehicle vehicle = new Vehicle();
        vehicle.setFuelTankCapacity(50.0);
        vehicle.setFuelLevel(25.0);

        double fuelPercentage = (vehicle.getFuelLevel() / vehicle.getFuelTankCapacity()) * 100;
        assertEquals(50.0, fuelPercentage, 0.01, "Fuel should be at 50%");
    }

    @Test
    void testBatteryPercentageAlreadyProvided() {
        // Test that battery SOC is already a percentage
        Vehicle vehicle = new Vehicle();
        vehicle.setCurrentBatterySoc(85.0);

        assertEquals(85.0, vehicle.getCurrentBatterySoc(), "Battery SOC is already a percentage");
    }

    @Test
    void testAllArgsConstructor() {
        // Test the all-args constructor from Lombok
        LocalDateTime now = LocalDateTime.now();
        Vehicle vehicle = new Vehicle(
            1L,                                    // id
            100L,                                  // companyId
            "TEST-001",                            // vehicleNumber
            Vehicle.VehicleType.LCV,               // type
            FuelType.HYBRID,                       // fuelType
            "Toyota",                              // make
            "Prius",                               // model
            2023,                                  // year
            8.8,                                   // batteryCapacity
            Vehicle.VehicleStatus.ACTIVE,          // status
            75.0,                                  // currentBatterySoc
            "Type 2",                              // defaultChargerType
            45.0,                                  // fuelTankCapacity
            35.0,                                  // fuelLevel
            "PETROL",                              // engineType
            28.7041,                               // latitude
            77.1025,                               // longitude
            now,                                   // lastUpdated
            now,                                   // createdAt
            now,                                   // updatedAt
            "1HGBH41JXMN109186",                   // vin
            "DL01AB1234",                          // licensePlate
            "White",                               // color
            999L,                                  // currentDriverId
            10000.0,                               // totalDistance
            500.0,                                 // totalEnergyConsumed
            200.0                                  // totalFuelConsumed
        );

        assertNotNull(vehicle);
        assertEquals("TEST-001", vehicle.getVehicleNumber());
        assertEquals(FuelType.HYBRID, vehicle.getFuelType());
        assertEquals("Toyota", vehicle.getMake());
    }
}
