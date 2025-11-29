package com.evfleet.fleet.service;

import com.evfleet.common.event.EventPublisher;
import com.evfleet.common.exception.InvalidInputException;
import com.evfleet.common.exception.ResourceNotFoundException;
import com.evfleet.driver.repository.DriverRepository;
import com.evfleet.fleet.model.Trip;
import com.evfleet.fleet.model.TripLocationHistory;
import com.evfleet.fleet.repository.TripLocationHistoryRepository;
import com.evfleet.fleet.repository.TripRepository;
import com.evfleet.fleet.repository.VehicleRepository;
import com.evfleet.maintenance.service.MaintenanceService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

/**
 * Unit tests for TripService teleportation prevention
 * 
 * PR #6: Tests for impossible trip detection and location validation
 *
 * @author SEV Platform Team
 * @version 1.0.0
 */
@ExtendWith(MockitoExtension.class)
class TripServiceTeleportationTest {

    @Mock
    private TripRepository tripRepository;

    @Mock
    private TripLocationHistoryRepository locationHistoryRepository;

    @Mock
    private VehicleRepository vehicleRepository;

    @Mock
    private DriverRepository driverRepository;

    @Mock
    private EventPublisher eventPublisher;

    @Mock
    private MaintenanceService maintenanceService;

    private TripService tripService;

    @BeforeEach
    void setUp() {
        tripService = new TripService(
            tripRepository,
            locationHistoryRepository,
            vehicleRepository,
            driverRepository,
            eventPublisher,
            maintenanceService
        );
    }

    // ===== Coordinate Validation Tests =====

    @Test
    void testUpdateTripLocation_WithInvalidLatitude_TooLow_ThrowsException() {
        Trip trip = Trip.builder()
            .id(1L)
            .status(Trip.TripStatus.IN_PROGRESS)
            .build();

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        InvalidInputException exception = assertThrows(InvalidInputException.class,
            () -> tripService.updateTripLocation(1L, -91.0, 77.0));

        assertTrue(exception.getMessage().contains("latitude"));
        assertTrue(exception.getMessage().contains("-90"));
    }

    @Test
    void testUpdateTripLocation_WithInvalidLatitude_TooHigh_ThrowsException() {
        Trip trip = Trip.builder()
            .id(1L)
            .status(Trip.TripStatus.IN_PROGRESS)
            .build();

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        InvalidInputException exception = assertThrows(InvalidInputException.class,
            () -> tripService.updateTripLocation(1L, 91.0, 77.0));

        assertTrue(exception.getMessage().contains("latitude"));
        assertTrue(exception.getMessage().contains("90"));
    }

    @Test
    void testUpdateTripLocation_WithInvalidLongitude_TooLow_ThrowsException() {
        Trip trip = Trip.builder()
            .id(1L)
            .status(Trip.TripStatus.IN_PROGRESS)
            .build();

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        InvalidInputException exception = assertThrows(InvalidInputException.class,
            () -> tripService.updateTripLocation(1L, 28.0, -181.0));

        assertTrue(exception.getMessage().contains("longitude"));
        assertTrue(exception.getMessage().contains("-180"));
    }

    @Test
    void testUpdateTripLocation_WithInvalidLongitude_TooHigh_ThrowsException() {
        Trip trip = Trip.builder()
            .id(1L)
            .status(Trip.TripStatus.IN_PROGRESS)
            .build();

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        InvalidInputException exception = assertThrows(InvalidInputException.class,
            () -> tripService.updateTripLocation(1L, 28.0, 181.0));

        assertTrue(exception.getMessage().contains("longitude"));
        assertTrue(exception.getMessage().contains("180"));
    }

    @Test
    void testUpdateTripLocation_WithNullLatitude_ThrowsException() {
        InvalidInputException exception = assertThrows(InvalidInputException.class,
            () -> tripService.updateTripLocation(1L, null, 77.0));

        assertTrue(exception.getMessage().contains("latitude"));
        assertTrue(exception.getMessage().contains("required"));
    }

    @Test
    void testUpdateTripLocation_WithNullLongitude_ThrowsException() {
        InvalidInputException exception = assertThrows(InvalidInputException.class,
            () -> tripService.updateTripLocation(1L, 28.0, null));

        assertTrue(exception.getMessage().contains("longitude"));
        assertTrue(exception.getMessage().contains("required"));
    }

    // ===== Trip State Validation Tests =====

    @Test
    void testUpdateTripLocation_TripNotFound_ThrowsException() {
        when(tripRepository.findById(999L)).thenReturn(Optional.empty());

        ResourceNotFoundException exception = assertThrows(ResourceNotFoundException.class,
            () -> tripService.updateTripLocation(999L, 28.6139, 77.2090));

        assertTrue(exception.getMessage().contains("Trip"));
    }

    @Test
    void testUpdateTripLocation_TripNotInProgress_ThrowsException() {
        Trip trip = Trip.builder()
            .id(1L)
            .status(Trip.TripStatus.COMPLETED)
            .build();

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));

        IllegalStateException exception = assertThrows(IllegalStateException.class,
            () -> tripService.updateTripLocation(1L, 28.6139, 77.2090));

        assertTrue(exception.getMessage().contains("not in progress"));
    }

    // ===== Teleportation Detection Tests =====

    @Test
    void testUpdateTripLocation_WithTeleportation_ThrowsException() {
        // Setup trip
        Trip trip = Trip.builder()
            .id(1L)
            .vehicleId(100L)
            .status(Trip.TripStatus.IN_PROGRESS)
            .build();

        // Previous location: Delhi
        TripLocationHistory lastLocation = TripLocationHistory.builder()
            .tripId(1L)
            .latitude(28.6139)  // Delhi
            .longitude(77.2090)
            .recordedAt(LocalDateTime.now().minusSeconds(10)) // 10 seconds ago
            .sequenceNumber(0)
            .cumulativeDistance(0.0)
            .build();

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(locationHistoryRepository.findFirstByTripIdOrderBySequenceNumberDesc(1L))
            .thenReturn(Optional.of(lastLocation));

        // Try to update to Mumbai (1400km away in 10 seconds = 504,000 km/h - impossible!)
        InvalidInputException exception = assertThrows(InvalidInputException.class,
            () -> tripService.updateTripLocation(1L, 19.0760, 72.8777));

        assertTrue(exception.getMessage().contains("Impossible speed"));
        assertTrue(exception.getMessage().contains("teleportation"));
    }

    @Test
    void testUpdateTripLocation_WithReasonableSpeed_Success() {
        // Setup trip
        Trip trip = Trip.builder()
            .id(1L)
            .vehicleId(100L)
            .status(Trip.TripStatus.IN_PROGRESS)
            .build();

        com.evfleet.fleet.model.Vehicle vehicle = com.evfleet.fleet.model.Vehicle.builder()
            .id(100L)
            .build();

        // Previous location: Starting point
        TripLocationHistory lastLocation = TripLocationHistory.builder()
            .tripId(1L)
            .latitude(28.6139)
            .longitude(77.2090)
            .recordedAt(LocalDateTime.now().minusMinutes(30)) // 30 minutes ago
            .sequenceNumber(0)
            .cumulativeDistance(0.0)
            .build();

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(locationHistoryRepository.findFirstByTripIdOrderBySequenceNumberDesc(1L))
            .thenReturn(Optional.of(lastLocation));
        when(vehicleRepository.findById(100L)).thenReturn(Optional.of(vehicle));
        when(locationHistoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Move 30km in 30 minutes = 60 km/h (reasonable)
        // ~30km north (about 0.27 degrees latitude)
        TripLocationHistory result = tripService.updateTripLocation(1L, 28.8839, 77.2090);

        assertNotNull(result);
        assertFalse(result.getTeleportationWarning());
        assertTrue(result.getSpeed() < 200.0);  // Below max speed
    }

    @Test
    void testUpdateTripLocation_TooRapidUpdate_ThrowsException() {
        // Setup trip
        Trip trip = Trip.builder()
            .id(1L)
            .vehicleId(100L)
            .status(Trip.TripStatus.IN_PROGRESS)
            .build();

        // Previous location: Just now (less than 1 second ago)
        TripLocationHistory lastLocation = TripLocationHistory.builder()
            .tripId(1L)
            .latitude(28.6139)
            .longitude(77.2090)
            .recordedAt(LocalDateTime.now().minusNanos(500_000_000)) // 0.5 seconds ago
            .sequenceNumber(0)
            .cumulativeDistance(0.0)
            .build();

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(locationHistoryRepository.findFirstByTripIdOrderBySequenceNumberDesc(1L))
            .thenReturn(Optional.of(lastLocation));

        InvalidInputException exception = assertThrows(InvalidInputException.class,
            () -> tripService.updateTripLocation(1L, 28.6140, 77.2091));

        assertTrue(exception.getMessage().contains("1 second apart"));
    }

    // ===== First Location Update (No Previous) =====

    @Test
    void testUpdateTripLocation_FirstUpdate_Success() {
        Trip trip = Trip.builder()
            .id(1L)
            .vehicleId(100L)
            .status(Trip.TripStatus.IN_PROGRESS)
            .build();

        com.evfleet.fleet.model.Vehicle vehicle = com.evfleet.fleet.model.Vehicle.builder()
            .id(100L)
            .build();

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(locationHistoryRepository.findFirstByTripIdOrderBySequenceNumberDesc(1L))
            .thenReturn(Optional.empty()); // No previous location
        when(vehicleRepository.findById(100L)).thenReturn(Optional.of(vehicle));
        when(locationHistoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        TripLocationHistory result = tripService.updateTripLocation(1L, 28.6139, 77.2090);

        assertNotNull(result);
        assertEquals(0, result.getSequenceNumber());
        assertEquals(0.0, result.getDistanceFromPrevious());
        assertEquals(0.0, result.getSpeed());
    }

    // ===== Path History Retrieval =====

    @Test
    void testGetTripPath_ReturnsOrderedHistory() {
        TripLocationHistory loc1 = TripLocationHistory.builder()
            .tripId(1L).sequenceNumber(0).latitude(28.0).longitude(77.0).build();
        TripLocationHistory loc2 = TripLocationHistory.builder()
            .tripId(1L).sequenceNumber(1).latitude(28.1).longitude(77.1).build();
        TripLocationHistory loc3 = TripLocationHistory.builder()
            .tripId(1L).sequenceNumber(2).latitude(28.2).longitude(77.2).build();

        when(locationHistoryRepository.findByTripIdOrderBySequenceNumberAsc(1L))
            .thenReturn(java.util.List.of(loc1, loc2, loc3));

        var path = tripService.getTripPath(1L);

        assertEquals(3, path.size());
        assertEquals(0, path.get(0).getSequenceNumber());
        assertEquals(1, path.get(1).getSequenceNumber());
        assertEquals(2, path.get(2).getSequenceNumber());
    }

    @Test
    void testGetCalculatedTripDistance_ReturnsSum() {
        when(locationHistoryRepository.getTotalDistanceByTripId(1L)).thenReturn(45.5);

        Double distance = tripService.getCalculatedTripDistance(1L);

        assertEquals(45.5, distance);
    }

    // ===== Edge Case: Boundary Coordinates =====

    @Test
    void testUpdateTripLocation_WithBoundaryCoordinates_Success() {
        Trip trip = Trip.builder()
            .id(1L)
            .vehicleId(100L)
            .status(Trip.TripStatus.IN_PROGRESS)
            .build();

        com.evfleet.fleet.model.Vehicle vehicle = com.evfleet.fleet.model.Vehicle.builder()
            .id(100L)
            .build();

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(locationHistoryRepository.findFirstByTripIdOrderBySequenceNumberDesc(1L))
            .thenReturn(Optional.empty());
        when(vehicleRepository.findById(100L)).thenReturn(Optional.of(vehicle));
        when(locationHistoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Test with exact boundary values
        assertDoesNotThrow(() -> tripService.updateTripLocation(1L, 90.0, 180.0));
        assertDoesNotThrow(() -> tripService.updateTripLocation(1L, -90.0, -180.0));
        assertDoesNotThrow(() -> tripService.updateTripLocation(1L, 0.0, 0.0));
    }

    // ===== Haversine Distance Calculation Test =====

    @Test
    void testHaversineDistanceCalculation_Accuracy() {
        // Delhi to Agra is about 206km
        // We'll verify the speed calculation is reasonable for a known route
        Trip trip = Trip.builder()
            .id(1L)
            .vehicleId(100L)
            .status(Trip.TripStatus.IN_PROGRESS)
            .build();

        com.evfleet.fleet.model.Vehicle vehicle = com.evfleet.fleet.model.Vehicle.builder()
            .id(100L)
            .build();

        // Start at Delhi
        TripLocationHistory lastLocation = TripLocationHistory.builder()
            .tripId(1L)
            .latitude(28.6139)
            .longitude(77.2090)
            .recordedAt(LocalDateTime.now().minusHours(2)) // 2 hours ago
            .sequenceNumber(0)
            .cumulativeDistance(0.0)
            .build();

        when(tripRepository.findById(1L)).thenReturn(Optional.of(trip));
        when(locationHistoryRepository.findFirstByTripIdOrderBySequenceNumberDesc(1L))
            .thenReturn(Optional.of(lastLocation));
        when(vehicleRepository.findById(100L)).thenReturn(Optional.of(vehicle));
        when(locationHistoryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        // Move to Agra (~200km in 2 hours = 100 km/h - reasonable highway speed)
        TripLocationHistory result = tripService.updateTripLocation(1L, 27.1767, 78.0081);

        assertNotNull(result);
        // Speed should be around 100 km/h (give or take for calculation precision)
        assertTrue(result.getSpeed() > 90 && result.getSpeed() < 120, 
            "Expected speed around 100 km/h, got: " + result.getSpeed());
        // Distance should be around 200 km
        assertTrue(result.getDistanceFromPrevious() > 180 && result.getDistanceFromPrevious() < 220,
            "Expected distance around 200 km, got: " + result.getDistanceFromPrevious());
    }
}
