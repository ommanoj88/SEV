package com.evfleet.fleet.repository;

import com.evfleet.fleet.model.Trip;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for Trip entity
 */
@Repository
public interface TripRepository extends JpaRepository<Trip, Long> {

    /**
     * Find trips by vehicle ID
     */
    List<Trip> findByVehicleId(Long vehicleId, Pageable pageable);

    /**
     * Find trips by driver ID
     */
    List<Trip> findByDriverId(Long driverId, Pageable pageable);

    /**
     * Find trips by company ID
     */
    List<Trip> findByCompanyId(Long companyId, Pageable pageable);

    /**
     * Find trips by status
     */
    List<Trip> findByStatus(Trip.TripStatus status);

    /**
     * Find ongoing trip for a vehicle
     */
    Optional<Trip> findByVehicleIdAndStatus(Long vehicleId, Trip.TripStatus status);

    /**
     * Find trips within time range
     */
    @Query("SELECT t FROM Trip t WHERE t.companyId = :companyId " +
           "AND t.startTime BETWEEN :startTime AND :endTime " +
           "ORDER BY t.startTime DESC")
    List<Trip> findTripsByCompanyAndTimeRange(
            @Param("companyId") Long companyId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * Find trips by vehicle within time range
     */
    @Query("SELECT t FROM Trip t WHERE t.vehicleId = :vehicleId " +
           "AND t.startTime BETWEEN :startTime AND :endTime " +
           "ORDER BY t.startTime DESC")
    List<Trip> findTripsByVehicleAndTimeRange(
            @Param("vehicleId") Long vehicleId,
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );

    /**
     * Calculate total distance by vehicle
     */
    @Query("SELECT SUM(t.distance) FROM Trip t WHERE t.vehicleId = :vehicleId AND t.status = 'COMPLETED'")
    Double calculateTotalDistanceByVehicle(@Param("vehicleId") Long vehicleId);

    /**
     * Calculate total energy consumed by vehicle
     */
    @Query("SELECT SUM(t.energyConsumed) FROM Trip t WHERE t.vehicleId = :vehicleId AND t.status = 'COMPLETED'")
    Double calculateTotalEnergyByVehicle(@Param("vehicleId") Long vehicleId);

    /**
     * Count trips by vehicle and status
     */
    long countByVehicleIdAndStatus(Long vehicleId, Trip.TripStatus status);

    /**
     * Count trips by driver and status
     */
    long countByDriverIdAndStatus(Long driverId, Trip.TripStatus status);

    /**
     * Find recent completed trips
     */
    @Query("SELECT t FROM Trip t WHERE t.companyId = :companyId " +
           "AND t.status = 'COMPLETED' " +
           "ORDER BY t.endTime DESC")
    List<Trip> findRecentCompletedTrips(@Param("companyId") Long companyId, Pageable pageable);

    /**
     * Find ongoing trips by company
     */
    @Query("SELECT t FROM Trip t WHERE t.companyId = :companyId AND t.status = 'ONGOING'")
    List<Trip> findOngoingTripsByCompany(@Param("companyId") Long companyId);

    /**
     * Calculate average efficiency score by vehicle
     */
    @Query("SELECT AVG(t.efficiencyScore) FROM Trip t WHERE t.vehicleId = :vehicleId " +
           "AND t.status = 'COMPLETED' AND t.efficiencyScore IS NOT NULL")
    Double calculateAverageEfficiencyScore(@Param("vehicleId") Long vehicleId);

    /**
     * Find trips with low efficiency
     */
    @Query("SELECT t FROM Trip t WHERE t.companyId = :companyId " +
           "AND t.status = 'COMPLETED' " +
           "AND t.efficiencyScore < :threshold " +
           "ORDER BY t.efficiencyScore ASC")
    List<Trip> findLowEfficiencyTrips(@Param("companyId") Long companyId,
                                      @Param("threshold") Double threshold,
                                      Pageable pageable);
}
