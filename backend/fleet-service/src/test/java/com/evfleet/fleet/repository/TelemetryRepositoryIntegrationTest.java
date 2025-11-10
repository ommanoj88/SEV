package com.evfleet.fleet.repository;

import com.evfleet.fleet.model.TelemetryData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for TelemetryRepository ICE-specific query methods
 * 
 * Tests the new multi-fuel telemetry repository methods for ICE and HYBRID vehicles
 */
@DataJpaTest
@ActiveProfiles("test")
class TelemetryRepositoryIntegrationTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private TelemetryRepository telemetryRepository;

    private Long vehicleId;
    private Long tripId;

    @BeforeEach
    void setUp() {
        vehicleId = 1L;
        tripId = 100L;
        
        // Clear existing data
        telemetryRepository.deleteAll();
        entityManager.flush();
    }

    @Test
    void testFindLowFuelTelemetry() {
        // Given: Telemetry data with different fuel levels
        createICETelemetry(vehicleId, 50.0, 2000, 85.0, LocalDateTime.now().minusMinutes(10));
        createICETelemetry(vehicleId, 10.0, 2100, 88.0, LocalDateTime.now().minusMinutes(5));
        createICETelemetry(vehicleId, 5.0, 2200, 90.0, LocalDateTime.now());
        entityManager.flush();

        // When: Finding low fuel telemetry (threshold: 15 liters)
        Pageable pageable = PageRequest.of(0, 10);
        List<TelemetryData> lowFuelRecords = telemetryRepository.findLowFuelTelemetry(vehicleId, 15.0, pageable);

        // Then: Should return only records with fuel level < 15
        assertThat(lowFuelRecords).hasSize(2);
        assertThat(lowFuelRecords).allMatch(t -> t.getFuelLevel() < 15.0);
        assertThat(lowFuelRecords.get(0).getFuelLevel()).isEqualTo(5.0); // Most recent first
    }

    @Test
    void testFindHighEngineTemperature() {
        // Given: Telemetry data with different engine temperatures
        createICETelemetry(vehicleId, 40.0, 1800, 85.0, LocalDateTime.now().minusMinutes(10));
        createICETelemetry(vehicleId, 38.0, 2000, 105.0, LocalDateTime.now().minusMinutes(5));
        createICETelemetry(vehicleId, 35.0, 2200, 115.0, LocalDateTime.now());
        entityManager.flush();

        // When: Finding high engine temperature (threshold: 100°C)
        Pageable pageable = PageRequest.of(0, 10);
        List<TelemetryData> highTempRecords = telemetryRepository.findHighEngineTemperature(vehicleId, 100.0, pageable);

        // Then: Should return records with engine temp > 100
        assertThat(highTempRecords).hasSize(2);
        assertThat(highTempRecords).allMatch(t -> t.getEngineTemperature() > 100.0);
    }

    @Test
    void testCalculateAverageEngineRpmForTrip() {
        // Given: Telemetry data for a trip with various RPMs
        createICETelemetryWithTrip(vehicleId, tripId, 45.0, 1500, 85.0, 50.0, LocalDateTime.now().minusMinutes(30));
        createICETelemetryWithTrip(vehicleId, tripId, 42.0, 2000, 88.0, 60.0, LocalDateTime.now().minusMinutes(20));
        createICETelemetryWithTrip(vehicleId, tripId, 40.0, 2500, 90.0, 70.0, LocalDateTime.now().minusMinutes(10));
        createICETelemetryWithTrip(vehicleId, tripId, 38.0, 3000, 92.0, 80.0, LocalDateTime.now());
        entityManager.flush();

        // When: Calculating average engine RPM
        Double avgRpm = telemetryRepository.calculateAverageEngineRpmForTrip(tripId);

        // Then: Should return correct average (1500 + 2000 + 2500 + 3000) / 4 = 2250
        assertThat(avgRpm).isNotNull();
        assertThat(avgRpm).isEqualTo(2250.0);
    }

    @Test
    void testCalculateAverageEngineLoadForTrip() {
        // Given: Telemetry data with various engine loads
        createICETelemetryWithTrip(vehicleId, tripId, 45.0, 2000, 85.0, 40.0, LocalDateTime.now().minusMinutes(30));
        createICETelemetryWithTrip(vehicleId, tripId, 42.0, 2100, 88.0, 60.0, LocalDateTime.now().minusMinutes(20));
        createICETelemetryWithTrip(vehicleId, tripId, 40.0, 2200, 90.0, 80.0, LocalDateTime.now().minusMinutes(10));
        createICETelemetryWithTrip(vehicleId, tripId, 38.0, 2300, 92.0, 70.0, LocalDateTime.now());
        entityManager.flush();

        // When: Calculating average engine load
        Double avgLoad = telemetryRepository.calculateAverageEngineLoadForTrip(tripId);

        // Then: Should return correct average (40 + 60 + 80 + 70) / 4 = 62.5
        assertThat(avgLoad).isNotNull();
        assertThat(avgLoad).isEqualTo(62.5);
    }

    @Test
    void testFindLatestEngineTelemetry() {
        // Given: Multiple telemetry records
        createICETelemetry(vehicleId, 50.0, 1800, 85.0, LocalDateTime.now().minusMinutes(10));
        createICETelemetry(vehicleId, 48.0, 2000, 88.0, LocalDateTime.now().minusMinutes(5));
        TelemetryData latest = createICETelemetry(vehicleId, 45.0, 2200, 90.0, LocalDateTime.now());
        entityManager.flush();

        // When: Finding latest engine telemetry
        Pageable pageable = PageRequest.of(0, 1);
        List<TelemetryData> result = telemetryRepository.findLatestEngineTelemetry(vehicleId, pageable);

        // Then: Should return the most recent record
        assertThat(result).isNotEmpty();
        assertThat(result.get(0).getEngineRpm()).isEqualTo(latest.getEngineRpm());
        assertThat(result.get(0).getFuelLevel()).isEqualTo(45.0);
    }

    @Test
    void testFindEngineDiagnosticIssues() {
        // Given: Telemetry with normal and problematic engine data
        createICETelemetry(vehicleId, 50.0, 1800, 85.0, LocalDateTime.now().minusMinutes(15)); // Normal
        createICETelemetry(vehicleId, 48.0, 2500, 110.0, LocalDateTime.now().minusMinutes(10)); // High temp
        createICETelemetryWithLoad(vehicleId, 45.0, 3000, 88.0, 95.0, LocalDateTime.now().minusMinutes(5)); // High load
        createICETelemetry(vehicleId, 42.0, 2000, 120.0, LocalDateTime.now()); // High temp
        entityManager.flush();

        // When: Finding diagnostic issues (temp > 100°C or load > 90%)
        Pageable pageable = PageRequest.of(0, 10);
        List<TelemetryData> issues = telemetryRepository.findEngineDiagnosticIssues(
            vehicleId, 100.0, 90.0, pageable
        );

        // Then: Should return records with issues
        assertThat(issues).hasSize(3);
    }

    @Test
    void testCalculateFuelConsumed() {
        // Given: Telemetry data showing fuel consumption over time
        LocalDateTime startTime = LocalDateTime.now().minusHours(2);
        LocalDateTime endTime = LocalDateTime.now();
        
        createICETelemetry(vehicleId, 60.0, 1800, 85.0, startTime); // Start with 60L
        createICETelemetry(vehicleId, 55.0, 2000, 88.0, startTime.plusMinutes(30));
        createICETelemetry(vehicleId, 50.0, 2200, 90.0, startTime.plusMinutes(60));
        createICETelemetry(vehicleId, 45.0, 2100, 88.0, startTime.plusMinutes(90));
        createICETelemetry(vehicleId, 40.0, 1900, 85.0, endTime); // End with 40L
        entityManager.flush();

        // When: Calculating fuel consumed using start and end fuel levels
        Pageable pageable = PageRequest.of(0, 1);
        List<Double> startFuel = telemetryRepository.findStartingFuelLevel(vehicleId, startTime, pageable);
        List<Double> endFuel = telemetryRepository.findEndingFuelLevel(vehicleId, endTime, pageable);

        // Then: Should calculate 20 liters consumed (60 - 40)
        assertThat(startFuel).isNotEmpty();
        assertThat(endFuel).isNotEmpty();
        Double fuelConsumed = startFuel.get(0) - endFuel.get(0);
        assertThat(fuelConsumed).isEqualTo(20.0);
    }

    @Test
    void testFindLowFuelTelemetryReturnsEmptyForHighFuel() {
        // Given: Telemetry with high fuel levels only
        createICETelemetry(vehicleId, 50.0, 2000, 85.0, LocalDateTime.now());
        entityManager.flush();

        // When: Finding low fuel with threshold below all values
        Pageable pageable = PageRequest.of(0, 10);
        List<TelemetryData> lowFuelRecords = telemetryRepository.findLowFuelTelemetry(vehicleId, 10.0, pageable);

        // Then: Should return empty list
        assertThat(lowFuelRecords).isEmpty();
    }

    @Test
    void testCalculateAverageEngineRpmWithNoData() {
        // When: Calculating average RPM for non-existent trip
        Double avgRpm = telemetryRepository.calculateAverageEngineRpmForTrip(999L);

        // Then: Should return null
        assertThat(avgRpm).isNull();
    }

    // Helper methods to create test data
    
    private TelemetryData createICETelemetry(Long vehicleId, Double fuelLevel, 
                                             Integer engineRpm, Double engineTemp,
                                             LocalDateTime timestamp) {
        TelemetryData telemetry = new TelemetryData();
        telemetry.setVehicleId(vehicleId);
        telemetry.setLatitude(12.9716);
        telemetry.setLongitude(77.5946);
        telemetry.setSpeed(60.0);
        telemetry.setTimestamp(timestamp);
        telemetry.setFuelLevel(fuelLevel);
        telemetry.setEngineRpm(engineRpm);
        telemetry.setEngineTemperature(engineTemp);
        telemetry.setEngineLoad(50.0);
        telemetry.setEngineHours(100.0);
        return entityManager.persist(telemetry);
    }

    private TelemetryData createICETelemetryWithTrip(Long vehicleId, Long tripId, Double fuelLevel,
                                                     Integer engineRpm, Double engineTemp, Double engineLoad,
                                                     LocalDateTime timestamp) {
        TelemetryData telemetry = createICETelemetry(vehicleId, fuelLevel, engineRpm, engineTemp, timestamp);
        telemetry.setTripId(tripId);
        telemetry.setEngineLoad(engineLoad);
        return telemetry;
    }

    private TelemetryData createICETelemetryWithLoad(Long vehicleId, Double fuelLevel,
                                                     Integer engineRpm, Double engineTemp, Double engineLoad,
                                                     LocalDateTime timestamp) {
        TelemetryData telemetry = createICETelemetry(vehicleId, fuelLevel, engineRpm, engineTemp, timestamp);
        telemetry.setEngineLoad(engineLoad);
        return telemetry;
    }
}
